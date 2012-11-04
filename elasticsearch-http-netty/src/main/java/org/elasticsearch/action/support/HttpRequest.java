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
import com.ning.http.client.Realm;
import com.ning.http.client.Realm.AuthScheme;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.providers.netty.NettyAsyncHttpProvider;
import com.ning.http.client.providers.netty.NettyAsyncHttpProviderConfig;

import org.elasticsearch.common.Strings;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;

public class HttpRequest {

    private final Settings settings;
    private final String method;
    private final RequestBuilder builder;
    private Realm.RealmBuilder realmBuilder;
    private AsyncHttpClient client;
    private URI uri;
    private String index;
    private String type;
    private String id;
    private String endpoint;

    public HttpRequest(Settings settings, String method, String endpoint) {
        this.settings = settings;
        this.method = method;
        this.endpoint = endpoint;
        this.builder = new RequestBuilder(method);
        this.realmBuilder = new Realm.RealmBuilder();
    }

    public HttpRequest setURI(URI fullUri) {
        if (fullUri.getUserInfo() != null) {
            String[] userInfo = fullUri.getUserInfo().split(":");
            realmBuilder = realmBuilder.setPrincipal(userInfo[0]).setPassword(userInfo[1]).setUsePreemptiveAuth(true).setScheme(AuthScheme.BASIC);
        }
        String authority = fullUri.getHost() + (fullUri.getPort() > 0 ? ":" + fullUri.getPort() : "");
        try {
            this.uri = new URI(fullUri.getScheme(), authority, fullUri.getPath(), fullUri.getQuery(), fullUri.getFragment());
        } catch (URISyntaxException ex) {
            // ignore
        }
        return this;
    }

    public String method() {
        return method;
    }

    public String endpoint() {
        return endpoint;
    }

    public URI getURI() {
        return uri;
    }

    public HttpRequest param(String name, BytesReference value) {
        if (value != null) {
            builder.addQueryParameter(name, value.toUtf8());
        }
        return this;
    }

    public HttpRequest param(String name, String value) {
        if (Strings.hasText(value)) {
            builder.addQueryParameter(name, value);
        }
        return this;
    }

    public HttpRequest param(String name, String[] value) {
        if (value != null) {
            // peculiar thing is, ES expects comma separated list, not repeated parameters
            builder.addQueryParameter(name, Strings.arrayToCommaDelimitedString(value));
        }
        return this;
    }

    public HttpRequest param(String name, Integer value) {
        if (value != null) {
            builder.addQueryParameter(name, value.toString());
        }
        return this;
    }

    public HttpRequest param(String name, Long value) {
        if (value != null) {
            builder.addQueryParameter(name, value.toString());
        }
        return this;
    }

    public HttpRequest param(String name, Double value) {
        if (value != null) {
            builder.addQueryParameter(name, value.toString());
        }
        return this;
    }

    public HttpRequest param(String name, Float value) {
        if (value != null) {
            builder.addQueryParameter(name, value.toString());
        }
        return this;
    }

    public HttpRequest param(String name, Boolean value) {
        if (value != null) {
            builder.addQueryParameter(name, value.toString());
        }
        return this;
    }

    public HttpRequest param(String name, TimeValue value) {
        if (value != null) {
            builder.addQueryParameter(name, value.format());
        }
        return this;
    }

    public HttpRequest header(String name, String value) {
        if (Strings.hasText(value)) {
            builder.addHeader(name, value);
        }
        return this;
    }

    public HttpRequest body(CharSequence body) {
        builder.setBody(body.toString());
        return this;
    }

    public HttpRequest body(BytesReference body) {
        builder.setBody(body.toUtf8());
        return this;
    }

    public HttpRequest index(String index) {
        this.index = index;
        return this;
    }

    public HttpRequest index(String[] index) {
        this.index = Strings.arrayToCommaDelimitedString(index);
        return this;
    }

    public HttpRequest type(String type) {
        this.type = type;
        return this;
    }

    public HttpRequest type(String[] type) {
        this.type = Strings.arrayToCommaDelimitedString(type);
        return this;
    }

    public HttpRequest id(String id) {
        this.id = id;
        return this;
    }

    private String buildPath(String url, String index, String type, String id) {
        StringBuilder sb = new StringBuilder(url);
        if (Strings.hasText(index)) {
            if (!url.endsWith("/")) {
                sb.append('/');
            }
            sb.append(index);
        }
        if (Strings.hasText(type)) {
            sb.append('/').append(type);
        }
        if (Strings.hasText(id)) {
            sb.append('/').append(id);
        }
        if (Strings.hasText(endpoint)) {
            sb.append('/').append(endpoint);
        }
        return sb.toString();
    }

    public Request buildRequest() {
        if (settings.get("http.user") != null) {
            realmBuilder = realmBuilder.setPrincipal(settings.get("http.user"));
        }
        if (settings.get("http.password") != null) {
            realmBuilder = realmBuilder.setPassword(settings.get("http.password"));
        }
        String url = this.uri != null
                ? this.uri.toASCIIString() : settings.get("http.connection.url", "http://localhost:9200");
        return builder.setUrl(buildPath(url, index, type, id)).setRealm(realmBuilder.build()).build();
    }

    public AsyncHttpClient buildClient() {
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
        this.client = new AsyncHttpClient(new NettyAsyncHttpProvider(config.build()));
        return client;
    }

    public void shutdown() {
        client.close();
    }
}
