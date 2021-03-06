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

package org.elasticsearch.discovery.tao.ping.unicast;

import com.google.common.collect.Lists;
import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.ElasticSearchIllegalArgumentException;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.util.concurrent.ConcurrentCollections;
import org.elasticsearch.common.util.concurrent.ClientEsExecutors;
import org.elasticsearch.discovery.tao.ping.TaoPing;
import org.elasticsearch.threadpool.transport.TransportThreadPool;
import org.elasticsearch.transport.BaseTransportRequestHandler;
import org.elasticsearch.transport.BaseTransportResponseHandler;
import org.elasticsearch.transport.ConnectTransportException;
import org.elasticsearch.transport.TransportChannel;
import org.elasticsearch.transport.TransportException;
import org.elasticsearch.transport.TransportRequest;
import org.elasticsearch.transport.TransportRequestOptions;
import org.elasticsearch.transport.TransportResponse;
import org.elasticsearch.transport.client.ClientTransportService;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.collect.Lists.newArrayList;
import static org.elasticsearch.common.settings.ImmutableSettings.Builder.EMPTY_SETTINGS;
import static org.elasticsearch.common.unit.TimeValue.readTimeValue;
import static org.elasticsearch.common.util.concurrent.ConcurrentCollections.newConcurrentMap;
import static org.elasticsearch.discovery.tao.ping.TaoPing.PingResponse.readPingResponse;

/**
 *
 */
public class UnicastTaoPing implements TaoPing {

    private final ESLogger logger;
    private final Settings settings;
    public static final int LIMIT_PORTS_COUNT = 1;

    private final TransportThreadPool threadPool;

    private final ClientTransportService transportService;

    private final ClusterName clusterName;

    private final DiscoveryNode localNode;    
    
    private final int concurrentConnects;

    private final DiscoveryNode[] nodes;

    //private volatile DiscoveryNodesProvider nodesProvider;

    private final AtomicInteger pingIdGenerator = new AtomicInteger();

    private final Map<Integer, ConcurrentMap<DiscoveryNode, PingResponse>> receivedResponses = newConcurrentMap();

    // a list of temporal responses a node will return for a request (holds requests from other nodes)
    private final Queue<PingResponse> temporalResponses = ConcurrentCollections.newQueue();

    private final CopyOnWriteArrayList<UnicastHostsProvider> hostsProviders = new CopyOnWriteArrayList<UnicastHostsProvider>();

    public UnicastTaoPing(TransportThreadPool threadPool, ClientTransportService transportService, ClusterName clusterName, DiscoveryNode localNode) {
        this(EMPTY_SETTINGS, threadPool, transportService, clusterName, localNode, null);
    }

    public UnicastTaoPing(Settings settings, TransportThreadPool threadPool, ClientTransportService transportService, ClusterName clusterName, DiscoveryNode localNode,
            @Nullable Set<UnicastHostsProvider> unicastHostsProviders) {
        this.settings = settings;
        this.logger = Loggers.getLogger(getClass(), settings);
        this.threadPool = threadPool;
        this.transportService = transportService;
        this.clusterName = clusterName;
        this.localNode = localNode;

        if (unicastHostsProviders != null) {
            for (UnicastHostsProvider unicastHostsProvider : unicastHostsProviders) {
                addHostsProvider(unicastHostsProvider);
            }
        }

        this.concurrentConnects = settings.getAsInt("discovery.tao.ping.unicast.concurrent_connects", 10);
        String[] hostArr = settings.getAsArray("discovery.tao.ping.unicast.hosts");
        // trim the hosts
        for (int i = 0; i < hostArr.length; i++) {
            hostArr[i] = hostArr[i].trim();
        }
        List<String> hosts = Lists.newArrayList(hostArr);
        logger.debug("using initial hosts {}, with concurrent_connects [{}]", hosts, concurrentConnects);

        List<DiscoveryNode> nodes = Lists.newArrayList();
        int idCounter = 0;
        for (String host : hosts) {
            try {
                TransportAddress[] addresses = transportService.addressesFromString(host);
                // we only limit to 1 addresses, makes no sense to ping 100 ports
                for (int i = 0; (i < addresses.length && i < LIMIT_PORTS_COUNT); i++) {
                    nodes.add(new DiscoveryNode("#zen_unicast_" + (++idCounter) + "#", addresses[i]));
                }
            } catch (Exception e) {
                throw new ElasticSearchIllegalArgumentException("Failed to resolve address for [" + host + "]", e);
            }
        }
        this.nodes = nodes.toArray(new DiscoveryNode[nodes.size()]);

        transportService.registerHandler(UnicastPingRequestHandler.ACTION, new UnicastPingRequestHandler());
    }

    public UnicastTaoPing start() throws ElasticSearchException {
        return this;
    }

    public void stop() throws ElasticSearchException {
    }

    public void close() throws ElasticSearchException {
        transportService.removeHandler(UnicastPingRequestHandler.ACTION);
    }

    public void addHostsProvider(UnicastHostsProvider provider) {
        hostsProviders.add(provider);
    }

    public void removeHostsProvider(UnicastHostsProvider provider) {
        hostsProviders.remove(provider);
    }

    public PingResponse[] pingAndWait(TimeValue timeout) {
        final AtomicReference<PingResponse[]> response = new AtomicReference<PingResponse[]>();
        final CountDownLatch latch = new CountDownLatch(1);
        ping(new PingListener() {
            
            public void onPing(PingResponse[] pings) {
                response.set(pings);
                latch.countDown();
            }
        }, timeout);
        try {
            latch.await();
            return response.get();
        } catch (InterruptedException e) {
            return null;
        }
    }

    
    public void ping(final PingListener listener, final TimeValue timeout) throws ElasticSearchException {
        final SendPingsHandler sendPingsHandler = new SendPingsHandler(pingIdGenerator.incrementAndGet());
        receivedResponses.put(sendPingsHandler.id(), ConcurrentCollections.<DiscoveryNode, PingResponse>newConcurrentMap());
        sendPings(timeout, null, sendPingsHandler);
        threadPool.schedule(TimeValue.timeValueMillis(timeout.millis() / 2), TransportThreadPool.Names.GENERIC, new Runnable() {
            
            public void run() {
                sendPings(timeout, null, sendPingsHandler);
                threadPool.schedule(TimeValue.timeValueMillis(timeout.millis() / 2), TransportThreadPool.Names.GENERIC, new Runnable() {
                    
                    public void run() {
                        sendPings(timeout, TimeValue.timeValueMillis(timeout.millis() / 2), sendPingsHandler);
                        ConcurrentMap<DiscoveryNode, PingResponse> responses = receivedResponses.remove(sendPingsHandler.id());
                        listener.onPing(responses.values().toArray(new PingResponse[responses.size()]));
                        for (DiscoveryNode node : sendPingsHandler.nodeToDisconnect) {
                            logger.trace("[{}] disconnecting from {}", sendPingsHandler.id(), node);
                            transportService.disconnectFromNode(node);
                        }
                        sendPingsHandler.close();
                    }
                });
            }
        });
    }

    class SendPingsHandler {
        private final int id;
        private volatile ExecutorService executor;
        private final Set<DiscoveryNode> nodeToDisconnect = ConcurrentCollections.newConcurrentSet();
        private volatile boolean closed;

        SendPingsHandler(int id) {
            this.id = id;
        }

        public int id() {
            return this.id;
        }

        public boolean isClosed() {
            return this.closed;
        }

        public Executor executor() {
            if (executor == null) {
                ThreadFactory threadFactory = ClientEsExecutors.daemonThreadFactory(settings, "[unicast_connect]");
                executor = ClientEsExecutors.newScalingExecutorService(0, concurrentConnects, 60, TimeUnit.SECONDS, threadFactory);
            }
            return executor;
        }

        public void close() {
            closed = true;
            if (executor != null) {
                executor.shutdownNow();
                executor = null;
            }
            nodeToDisconnect.clear();
        }
    }

    void sendPings(final TimeValue timeout, @Nullable TimeValue waitTime, final SendPingsHandler sendPingsHandler) {
        final UnicastPingRequest pingRequest = new UnicastPingRequest();
        pingRequest.id = sendPingsHandler.id();
        pingRequest.timeout = timeout;
        //DiscoveryNodes discoNodes = nodesProvider.nodes();
        pingRequest.pingResponse = new PingResponse(localNode, /*discoNodes.masterNode(),*/ clusterName);

        List<DiscoveryNode> nodesToPing = newArrayList(nodes);
        for (UnicastHostsProvider provider : hostsProviders) {
            nodesToPing.addAll(provider.buildDynamicNodes());
        }

        final CountDownLatch latch = new CountDownLatch(nodesToPing.size());
        for (final DiscoveryNode node : nodesToPing) {
            // make sure we are connected
            /*boolean nodeFoundByAddressX;
            DiscoveryNode nodeToSendX = discoNodes.findByAddress(node.address());
            if (nodeToSendX != null) {
                nodeFoundByAddressX = true;
            } else {
                nodeToSendX = node;
                nodeFoundByAddressX = false;
            }
            final DiscoveryNode nodeToSend = nodeToSendX;
            **/
            
            // connect "light"
            boolean nodeFoundByAddressX = false;
            final DiscoveryNode nodeToSend = node;

            final boolean nodeFoundByAddress = nodeFoundByAddressX;
            if (!transportService.nodeConnected(nodeToSend)) {
                if (sendPingsHandler.isClosed()) {
                    return;
                }
                sendPingsHandler.nodeToDisconnect.add(nodeToSend);
                // fork the connection to another thread
                sendPingsHandler.executor().execute(new Runnable() {
                    
                    public void run() {
                        try {
                            // connect to the node, see if we manage to do it, if not, bail
                            if (!nodeFoundByAddress) {
                                logger.trace("[{}] connecting (light) to {}", sendPingsHandler.id(), nodeToSend);
                                transportService.connectToNodeLight(nodeToSend);
                            } else {
                                logger.trace("[{}] connecting to {}", sendPingsHandler.id(), nodeToSend);
                                transportService.connectToNode(nodeToSend);
                            }
                            logger.trace("[{}] connected to {}", sendPingsHandler.id(), node);
                            if (receivedResponses.containsKey(sendPingsHandler.id())) {
                                // we are connected and still in progress, send the ping request
                                sendPingRequestToNode(sendPingsHandler.id(), timeout, pingRequest, latch, node, nodeToSend);
                            } else {
                                // connect took too long, just log it and bail
                                latch.countDown();
                                logger.trace("[{}] connect to {} was too long outside of ping window, bailing", sendPingsHandler.id(), node);
                            }
                        } catch (ConnectTransportException e) {
                            // can't connect to the node
                            logger.trace("[{}] failed to connect to {}", e, sendPingsHandler.id(), nodeToSend);
                            latch.countDown();
                        }
                    }
                });
            } else {
                sendPingRequestToNode(sendPingsHandler.id(), timeout, pingRequest, latch, node, nodeToSend);
            }
        }
        if (waitTime != null) {
            try {
                latch.await(waitTime.millis(), TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }

    private void sendPingRequestToNode(final int id, TimeValue timeout, UnicastPingRequest pingRequest, final CountDownLatch latch, final DiscoveryNode node, final DiscoveryNode nodeToSend) {
        logger.trace("[{}] sending to {}", id, nodeToSend);
        transportService.sendRequest(nodeToSend, UnicastPingRequestHandler.ACTION, pingRequest, TransportRequestOptions.options().withTimeout((long) (timeout.millis() * 1.25)), new BaseTransportResponseHandler<UnicastPingResponse>() {

            
            public UnicastPingResponse newInstance() {
                return new UnicastPingResponse();
            }

            
            public String executor() {
                return TransportThreadPool.Names.SAME;
            }

            
            public void handleResponse(UnicastPingResponse response) {
                logger.trace("[{}] received response from {}: {}", id, nodeToSend, Arrays.toString(response.pingResponses));
                try {
                    //DiscoveryNodes discoveryNodes = nodesProvider.nodes();
                    for (PingResponse pingResponse : response.pingResponses) {
                        if (pingResponse.target().id().equals(localNode.id())) {
                            // that's us, ignore
                            continue;
                        }
                        if (!pingResponse.clusterName().equals(clusterName)) {
                            // not part of the cluster
                            logger.debug("[{}] filtering out response from {}, not same cluster_name [{}]", id, pingResponse.target(), pingResponse.clusterName().value());
                            continue;
                        }
                        ConcurrentMap<DiscoveryNode, PingResponse> responses = receivedResponses.get(response.id);
                        if (responses == null) {
                            logger.warn("received ping response {} with no matching id [{}]", pingResponse, response.id);
                        } else {
                            responses.put(pingResponse.target(), pingResponse);
                        }
                    }
                } finally {
                    latch.countDown();
                }
            }

            
            public void handleException(TransportException exp) {
                latch.countDown();
                if (exp instanceof ConnectTransportException) {
                    // ok, not connected...
                    logger.trace("failed to connect to {}", exp, nodeToSend);
                } else {
                    logger.warn("failed to send ping to [{}]", exp, node);
                }
            }
        });
    }

    private UnicastPingResponse handlePingRequest(final UnicastPingRequest request) {
        //if (lifecycle.stoppedOrClosed()) {
        //    throw new ElasticSearchIllegalStateException("received ping request while stopped/closed");
        //}
        temporalResponses.add(request.pingResponse);
        threadPool.schedule(TimeValue.timeValueMillis(request.timeout.millis() * 2), TransportThreadPool.Names.SAME, new Runnable() {
            
            public void run() {
                temporalResponses.remove(request.pingResponse);
            }
        });

        List<PingResponse> pingResponses = newArrayList(temporalResponses);
        //DiscoveryNodes discoNodes = nodesProvider.nodes();
        pingResponses.add(new PingResponse(localNode, /*discoNodes.masterNode(),*/ clusterName));


        UnicastPingResponse unicastPingResponse = new UnicastPingResponse();
        unicastPingResponse.id = request.id;
        unicastPingResponse.pingResponses = pingResponses.toArray(new PingResponse[pingResponses.size()]);

        return unicastPingResponse;
    }

    class UnicastPingRequestHandler extends BaseTransportRequestHandler<UnicastPingRequest> {

        static final String ACTION = "discovery/zen/unicast";

        
        public UnicastPingRequest newInstance() {
            return new UnicastPingRequest();
        }

        
        public String executor() {
            return TransportThreadPool.Names.SAME;
        }

        
        public void messageReceived(UnicastPingRequest request, TransportChannel channel) throws Exception {
            channel.sendResponse(handlePingRequest(request));
        }
    }

    static class UnicastPingRequest extends TransportRequest {

        int id;

        TimeValue timeout;

        PingResponse pingResponse;

        UnicastPingRequest() {
        }

        
        public void readFrom(StreamInput in) throws IOException {
            super.readFrom(in);
            id = in.readInt();
            timeout = readTimeValue(in);
            pingResponse = readPingResponse(in);
        }

        
        public void writeTo(StreamOutput out) throws IOException {
            super.writeTo(out);
            out.writeInt(id);
            timeout.writeTo(out);
            pingResponse.writeTo(out);
        }
    }

    static class UnicastPingResponse extends TransportResponse {

        int id;

        PingResponse[] pingResponses;

        UnicastPingResponse() {
        }

        
        public void readFrom(StreamInput in) throws IOException {
            super.readFrom(in);
            id = in.readInt();
            pingResponses = new PingResponse[in.readVInt()];
            for (int i = 0; i < pingResponses.length; i++) {
                pingResponses[i] = readPingResponse(in);
            }
        }

        
        public void writeTo(StreamOutput out) throws IOException {
            super.writeTo(out);
            out.writeInt(id);
            out.writeVInt(pingResponses.length);
            for (PingResponse pingResponse : pingResponses) {
                pingResponse.writeTo(out);
            }
        }
    }    
    
}
