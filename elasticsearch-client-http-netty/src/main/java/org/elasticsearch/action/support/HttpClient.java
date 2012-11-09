/*
 * Licensed to ElasticSearch and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. ElasticSearch licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.action.support;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.ProxyServer;
import com.ning.http.client.providers.netty.NettyAsyncHttpProvider;
import com.ning.http.client.providers.netty.NettyAsyncHttpProviderConfig;
import java.util.concurrent.Executors;
import org.elasticsearch.client.GenericClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.threadpool.ThreadPool;

public abstract class HttpClient extends AsyncHttpClient implements GenericClient {
    
    private Settings settings;
    
    public HttpClient(Settings settings) {
        super(provide(settings));
        this.settings = settings;
    }
    
    public Settings settings() {
        return settings;
    }
    
    public ThreadPool threadPool() {
        throw new UnsupportedOperationException("Not supported");
    }
    
    private static NettyAsyncHttpProvider provide(Settings settings) {
        NettyAsyncHttpProviderConfig providerConfig = new NettyAsyncHttpProviderConfig();
        providerConfig.addProperty(NettyAsyncHttpProviderConfig.EXECUTE_ASYNC_CONNECT, "true");
        providerConfig.addProperty(NettyAsyncHttpProviderConfig.USE_BLOCKING_IO, "false");
        AsyncHttpClientConfig.Builder config = new AsyncHttpClientConfig.Builder()
                .setAsyncHttpClientProviderConfig(providerConfig)
                .setExecutorService(Executors.newFixedThreadPool(settings.getAsInt("http.connection.poolsize", 4)))
                .setAllowPoolingConnection(settings.getAsBoolean("http.connection.pooling", Boolean.TRUE))
                .setConnectionTimeoutInMs(settings.getAsInt("http.connection.timeout", 3000))
                .setMaximumConnectionsTotal(settings.getAsInt("http.connection.max", 4))
                .setRequestTimeoutInMs((int) settings.getAsTime("http.request.timeout", TimeValue.timeValueSeconds(30L)).getMillis())
                .setFollowRedirects(settings.getAsBoolean("http.connection.followredirect", Boolean.TRUE))
                .setMaxRequestRetry(settings.getAsInt("http.request.maxretries", 3))
                .setCompressionEnabled(settings.getAsBoolean("http.compression.enabled", Boolean.TRUE));
        if (settings.get("http.proxy.host") != null && settings.getAsInt("http.proxy.port", -1) != -1) {
            config.setProxyServer(new ProxyServer(settings.get("http.proxy.host"), settings.getAsInt("http.proxy.port", -1)));
        }
        return new NettyAsyncHttpProvider(config.build());
    }

}
