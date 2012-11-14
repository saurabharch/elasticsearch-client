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

package org.elasticsearch.client.http;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.Action;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.support.HttpClient;
import org.elasticsearch.client.GenericClient;
import org.elasticsearch.client.http.support.InternalHttpIngestClient;
import org.elasticsearch.client.support.AbstractIngestClient;
import org.elasticsearch.client.internal.InternalClientSettingsPreparer;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.compress.BasicCompressorFactory;
import org.elasticsearch.common.io.CachedStreams;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.util.concurrent.ThreadLocals;
import org.elasticsearch.env.ClientEnvironment;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.threadpool.client.ClientThreadPool;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.elasticsearch.common.settings.ImmutableSettings.settingsBuilder;

import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;


public class HttpIngestClient extends AbstractIngestClient {

    private final ESLogger logger = ESLoggerFactory.getLogger(getClass().getName());
        
    private final Settings settings;

    private final ClientEnvironment environment;

    private final ThreadPool threadPool;

    private final HttpClient internalClient;
    
    private Set<TransportAddress> addresses;
    
    public HttpIngestClient() throws ElasticSearchException {
        this(ImmutableSettings.Builder.EMPTY_SETTINGS, true);
    }

    public HttpIngestClient(Settings settings) {
        this(settings, true);
    }

    public HttpIngestClient(Settings.Builder settings) {
        this(settings.build(), true);
    }

    public HttpIngestClient(Settings.Builder settings, boolean loadConfigSettings) throws ElasticSearchException {
        this(settings.build(), loadConfigSettings);
    }

    public HttpIngestClient(Settings pSettings, boolean loadConfigSettings) throws ElasticSearchException {
        Tuple<Settings, ClientEnvironment> tuple = InternalClientSettingsPreparer.prepareSettings(pSettings, loadConfigSettings);
        // some defaults, not really needed, just for TransportClient compatibility
        this.settings = settingsBuilder().put(tuple.v1())
                .put("network.server", false)
                .put("node.client", true)
                .build();
        this.environment = tuple.v2();
        this.threadPool = new ClientThreadPool();
        this.addresses = Sets.newHashSet();
        this.internalClient = new InternalHttpIngestClient(settings);
        BasicCompressorFactory.configure(settings);
    }

    public ImmutableList<TransportAddress> transportAddresses() {
        return ImmutableList.copyOf(addresses);
    }

    public HttpIngestClient addTransportAddress(TransportAddress address) {
        addresses.add(address);
        return this;
    }

    public HttpIngestClient addTransportAddresses(TransportAddress... address) {
        addresses.addAll(Arrays.asList(address));
        return this;
    }

    public HttpIngestClient removeTransportAddress(TransportAddress address) {
        addresses.remove(address);
        return this;
    }

    public ThreadPool threadPool() {
        return threadPool;
    }
    
    public void close() {
        logger.debug("closing client...");
        internalClient.close();
        logger.debug("internal client closed");
        threadPool.shutdown();
        try {
            threadPool.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            // ignore
        }
        try {
            threadPool.shutdownNow();
        } catch (Exception e) {
            // ignore
        }
        CachedStreams.clear();
        ThreadLocals.clearReferencesThreadLocals();
        logger.debug("client closed");
    }

    public Settings settings() {
        return this.settings;
    }

    public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder>, Client extends GenericClient> 
            ActionFuture<Response> execute(Action<Request, Response, RequestBuilder, Client> action, Request request) {
        return internalClient.execute(action, request);
    }

    public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder>, SearchClient extends GenericClient> 
            void execute(Action<Request, Response, RequestBuilder, SearchClient> action, Request request, ActionListener<Response> listener) {
       internalClient.execute(action, request, listener);
    }
}
