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
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.HttpClusterAdminActionModule;
import org.elasticsearch.action.admin.indices.IndicesAction;
import org.elasticsearch.action.support.HttpAction;
import org.elasticsearch.action.support.HttpClient;
import org.elasticsearch.client.http.support.InternalHttpClusterAdminClient;
import org.elasticsearch.client.internal.InternalClientSettingsPreparer;
import org.elasticsearch.client.support.AbstractIndicesAdminClient;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.compress.BasicCompressorFactory;
import org.elasticsearch.common.io.CachedStreams;
import org.elasticsearch.common.settings.ImmutableSettings;
import static org.elasticsearch.common.settings.ImmutableSettings.settingsBuilder;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.util.concurrent.ThreadLocals;
import org.elasticsearch.env.ClientEnvironment;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.threadpool.client.ClientThreadPool;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.elasticsearch.action.HttpIndicesAdminActionModule;
import org.elasticsearch.client.http.support.InternalHttpIndicesAdminClient;

public class HttpIndicesAdminClient extends AbstractIndicesAdminClient {

    private final Settings settings;

    private final ClientEnvironment environment;

    private final ThreadPool threadPool;

    private final HttpIndicesAdminActionModule actions = new HttpIndicesAdminActionModule();
    
    private final HttpClient internalClient;
    
    private Set<TransportAddress> addresses;
    
    public HttpIndicesAdminClient() throws ElasticSearchException {
        this(ImmutableSettings.Builder.EMPTY_SETTINGS, true);
    }

    public HttpIndicesAdminClient(Settings settings) {
        this(settings, true);
    }

    public HttpIndicesAdminClient(Settings.Builder settings) {
        this(settings.build(), true);
    }

    public HttpIndicesAdminClient(Settings.Builder settings, boolean loadConfigSettings) throws ElasticSearchException {
        this(settings.build(), loadConfigSettings);
    }

    public HttpIndicesAdminClient(Settings pSettings, boolean loadConfigSettings) throws ElasticSearchException {
        Tuple<Settings, ClientEnvironment> tuple = InternalClientSettingsPreparer.prepareSettings(pSettings, loadConfigSettings);
        // some defaults, not really needed, just for TransportClient compatibility
        this.settings = settingsBuilder().put(tuple.v1())
                .put("network.server", false)
                .put("node.client", true)
                .build();
        this.environment = tuple.v2();
        this.threadPool = new ClientThreadPool();
        this.addresses = Sets.newHashSet();
        this.internalClient = new InternalHttpIndicesAdminClient(settings, actions);
        BasicCompressorFactory.configure(settings);
    }

    public ImmutableList<TransportAddress> transportAddresses() {
        return ImmutableList.copyOf(addresses);
    }

    public HttpIndicesAdminClient addTransportAddress(TransportAddress address) {
        addresses.add(address);
        return this;
    }

    public HttpIndicesAdminClient addTransportAddresses(TransportAddress... address) {
        addresses.addAll(Arrays.asList(address));
        return this;
    }

    public HttpIndicesAdminClient removeTransportAddress(TransportAddress address) {
        addresses.remove(address);
        return this;
    }

    @Override
    public ThreadPool threadPool() {
        return threadPool;
    }
    
    @Override
    public void close() {
        internalClient.close();
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
    }

    @Override
    public Settings settings() {
        return this.settings;
    }

    @Override
    public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder>> 
            ActionFuture<Response> execute(IndicesAction<Request, Response, RequestBuilder> action, Request request) {
        HttpAction<Request,Response> httpAction = actions.getAction(action.name());
        return httpAction.execute(internalClient, request);
    }

    @Override
    public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder>> 
            void execute(IndicesAction<Request, Response, RequestBuilder> action, Request request, ActionListener<Response> listener) {
        HttpAction<Request,Response> httpAction = actions.getAction(action.name());
        httpAction.execute(internalClient, request);
    }
}
