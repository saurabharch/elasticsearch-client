package org.elasticsearch.discovery.tao.ping.multicast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.ElasticSearchIllegalStateException;
import org.elasticsearch.ExceptionsHelper;
import org.elasticsearch.Version;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.node.DiscoveryNode;
import static org.elasticsearch.cluster.node.DiscoveryNode.readNode;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.io.stream.BytesStreamInput;
import org.elasticsearch.common.io.stream.BasicCachedStreamInput;
import org.elasticsearch.common.io.stream.BasicCachedStreamOutput;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;
import org.elasticsearch.common.network.TransportNetworkService;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.util.concurrent.ConcurrentCollections;
import static org.elasticsearch.common.util.concurrent.ConcurrentCollections.newConcurrentMap;
import static org.elasticsearch.common.util.concurrent.ClientEsExecutors.daemonThreadFactory;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.discovery.tao.ping.TaoPing;
import org.elasticsearch.discovery.tao.ping.TaoPing.PingResponse;
import org.elasticsearch.threadpool.transport.TransportThreadPool;
import org.elasticsearch.transport.BaseTransportRequestHandler;
import org.elasticsearch.transport.EmptyTransportResponseHandler;
import org.elasticsearch.transport.TransportChannel;
import org.elasticsearch.transport.TransportException;
import org.elasticsearch.transport.TransportRequest;
import org.elasticsearch.transport.TransportResponse;
import org.elasticsearch.transport.TransportResponseHandler;
import org.elasticsearch.transport.client.ClientTransportService;

public class MulticastTaoPing implements TaoPing {

    private final ESLogger logger;
    private final Settings settings;
    private final TransportNetworkService networkService;
    private final TransportThreadPool threadPool;
    private final ClientTransportService transportService;
    private final DiscoveryNode localNode;
    private final ClusterName clusterName;
    private static final byte[] INTERNAL_HEADER = new byte[]{1, 9, 8, 4};
    private final String address;
    private final int port;
    private final String group;
    private final int bufferSize;
    private final int ttl;
    private final boolean pingEnabled;
    private volatile Receiver receiver;
    private volatile Thread receiverThread;
    private MulticastSocket multicastSocket;
    private DatagramPacket datagramPacketSend;
    private DatagramPacket datagramPacketReceive;
    private final AtomicInteger pingIdGenerator = new AtomicInteger();
    private final Map<Integer, ConcurrentMap<DiscoveryNode, PingResponse>> receivedResponses = newConcurrentMap();
    private final Object sendMutex = new Object();
    private final Object receiveMutex = new Object();

    public MulticastTaoPing(Settings settings,
            TransportThreadPool threadPool, 
            ClientTransportService transportService,
            ClusterName clusterName, DiscoveryNode localNode) {
        this.settings = settings;
        this.logger = Loggers.getLogger(getClass(), settings);
        this.threadPool = threadPool;
        this.transportService = transportService;
        this.localNode = localNode;
        this.clusterName = clusterName;
        this.networkService = new TransportNetworkService(settings);

        this.address = settings.get("address");
        this.port = settings.getAsInt("port", 54328);
        this.group = settings.get("group", "224.2.2.4");
        this.bufferSize = settings.getAsInt("buffer_size", 2048);
        this.ttl = settings.getAsInt("ttl", 3);

        this.pingEnabled = settings.getAsBoolean("ping.enabled", true);

        logger.debug("using group [{}], with port [{}], ttl [{}], and address [{}]", group, port, ttl, address);

        this.transportService.registerHandler(MulticastTaoPingResponseRequestHandler.ACTION, new MulticastTaoPingResponseRequestHandler());
    }

    public void start() {
        try {
            this.datagramPacketReceive = new DatagramPacket(new byte[bufferSize], bufferSize);
            this.datagramPacketSend = new DatagramPacket(new byte[bufferSize], bufferSize, InetAddress.getByName(group), port);
        } catch (Exception e) {
            logger.warn("disabled, failed to setup multicast (datagram) discovery : {}", e.getMessage());
            if (logger.isDebugEnabled()) {
                logger.debug("disabled, failed to setup multicast (datagram) discovery", e);
            }
            return;
        }

        InetAddress multicastInterface = null;
        try {
            MulticastSocket multicastSocket;
            multicastSocket = new MulticastSocket(port);

            multicastSocket.setTimeToLive(ttl);

            // set the send interface
            multicastInterface = networkService.resolvePublishHostAddress(address);
            multicastSocket.setInterface(multicastInterface);
            multicastSocket.joinGroup(InetAddress.getByName(group));

            multicastSocket.setReceiveBufferSize(bufferSize);
            multicastSocket.setSendBufferSize(bufferSize);
            multicastSocket.setSoTimeout(60000);

            this.multicastSocket = multicastSocket;

            this.receiver = new Receiver();
            this.receiverThread = daemonThreadFactory(settings, "discovery#multicast#receiver").newThread(receiver);
            this.receiverThread.start();
        } catch (Exception e) {
            datagramPacketReceive = null;
            datagramPacketSend = null;
            if (multicastSocket != null) {
                multicastSocket.close();
                multicastSocket = null;
            }
            logger.warn("disabled, failed to setup multicast discovery on port [{}], [{}]: {}", port, multicastInterface, e.getMessage());
            if (logger.isDebugEnabled()) {
                logger.debug("disabled, failed to setup multicast discovery on {}", e, multicastInterface);
            }
        }
    }

    public void stop() throws ElasticSearchException {
        if (receiver != null) {
            receiver.stop();
        }
        if (receiverThread != null) {
            receiverThread.interrupt();
        }
        if (multicastSocket != null) {
            multicastSocket.close();
            multicastSocket = null;
        }
    }

    public PingResponse[] pingAndWait(TimeValue timeout) {
        final AtomicReference<PingResponse[]> response = new AtomicReference<PingResponse[]>();
        final CountDownLatch latch = new CountDownLatch(1);
        ping(new PingListener() {
            @Override
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

    @Override
    public void ping(final PingListener listener, final TimeValue timeout) {
        if (!pingEnabled) {
            threadPool.generic().execute(new Runnable() {
                @Override
                public void run() {
                    listener.onPing(new PingResponse[0]);
                }
            });
            return;
        }
        final int id = pingIdGenerator.incrementAndGet();
        receivedResponses.put(id, ConcurrentCollections.<DiscoveryNode, PingResponse>newConcurrentMap());
        sendPingRequest(id);
        // try and send another ping request halfway through (just in case someone woke up during it...)
        // this can be a good trade-off to nailing the initial lookup or un-delivered messages
        threadPool.schedule(TimeValue.timeValueMillis(timeout.millis() / 2), TransportThreadPool.Names.GENERIC, new Runnable() {
            @Override
            public void run() {
                try {
                    sendPingRequest(id);
                } catch (Exception e) {
                    logger.warn("[{}] failed to send second ping request", e, id);
                }
            }
        });
        threadPool.schedule(timeout, TransportThreadPool.Names.GENERIC, new Runnable() {
            @Override
            public void run() {
                ConcurrentMap<DiscoveryNode, PingResponse> responses = receivedResponses.remove(id);
                listener.onPing(responses.values().toArray(new PingResponse[responses.size()]));
            }
        });
    }

    private void sendPingRequest(int id) {
        if (multicastSocket == null) {
            return;
        }
        synchronized (sendMutex) {
            BasicCachedStreamOutput.Entry cachedEntry = BasicCachedStreamOutput.popEntry();
            try {
                StreamOutput out = cachedEntry.handles();
                out.writeBytes(INTERNAL_HEADER);
                Version.writeVersion(Version.CURRENT, out);
                out.writeInt(id);
                clusterName.writeTo(out);
                localNode.writeTo(out);
                out.close();
                datagramPacketSend.setData(cachedEntry.bytes().bytes().copyBytesArray().toBytes());
                multicastSocket.send(datagramPacketSend);
                if (logger.isTraceEnabled()) {
                    logger.trace("[{}] sending ping request", id);
                }
            } catch (Exception e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("failed to send multicast ping request", e);
                } else {
                    logger.warn("failed to send multicast ping request: {}", ExceptionsHelper.detailedMessage(e));
                }
            } finally {
                BasicCachedStreamOutput.pushEntry(cachedEntry);
            }
        }
    }

    public class MulticastTaoPingResponseRequestHandler extends BaseTransportRequestHandler<MulticastPingResponse> {

        public static final String ACTION = "discovery/tao/multicast";

        @Override
        public MulticastPingResponse newInstance() {
            return new MulticastPingResponse();
        }

        @Override
        public void messageReceived(MulticastPingResponse request, TransportChannel channel) throws Exception {
            if (logger.isTraceEnabled()) {
                logger.trace("[{}] received {}", request.id, request.pingResponse);
            }
            ConcurrentMap<DiscoveryNode, PingResponse> responses = receivedResponses.get(request.id);
            if (responses == null) {
                logger.warn("received ping response {} with no matching id [{}]", request.pingResponse, request.id);
            } else {
                responses.put(request.pingResponse.target(), request.pingResponse);
            }
            channel.sendResponse(TransportResponse.Empty.INSTANCE);
        }

        @Override
        public String executor() {
            return TransportThreadPool.Names.SAME;
        }
    }

    static class MulticastPingResponse extends TransportRequest {

        int id;
        PingResponse pingResponse;

        MulticastPingResponse() {
        }

        @Override
        public void readFrom(StreamInput in) throws IOException {
            id = in.readInt();
            pingResponse = PingResponse.readPingResponse(in);
        }

        @Override
        public void writeTo(StreamOutput out) throws IOException {
            out.writeInt(id);
            pingResponse.writeTo(out);
        }
    }

    private class Receiver implements Runnable {

        private volatile boolean running = true;

        public void stop() {
            running = false;
        }

        @Override
        public void run() {
            while (running) {
                try {
                    int id = -1;
                    DiscoveryNode requestingNodeX = null;
                    ClusterName clusterName = null;

                    Map<String, Object> externalPingData = null;
                    XContentType xContentType = null;

                    synchronized (receiveMutex) {
                        try {
                            multicastSocket.receive(datagramPacketReceive);
                        } catch (SocketTimeoutException ignore) {
                            continue;
                        } catch (Exception e) {
                            if (running) {
                                logger.warn("failed to receive packet", e);
                            }
                            continue;
                        }
                        try {
                            boolean internal = false;
                            if (datagramPacketReceive.getLength() > 4) {
                                int counter = 0;
                                for (; counter < INTERNAL_HEADER.length; counter++) {
                                    if (datagramPacketReceive.getData()[datagramPacketReceive.getOffset() + counter] != INTERNAL_HEADER[counter]) {
                                        break;
                                    }
                                }
                                if (counter == INTERNAL_HEADER.length) {
                                    internal = true;
                                }
                            }
                            if (internal) {
                                StreamInput input = BasicCachedStreamInput.cachedHandles(new BytesStreamInput(datagramPacketReceive.getData(), datagramPacketReceive.getOffset() + INTERNAL_HEADER.length, datagramPacketReceive.getLength(), true));
                                Version version = Version.readVersion(input);
                                id = input.readInt();
                                clusterName = ClusterName.readClusterName(input);
                                requestingNodeX = readNode(input);
                            } else {
                                xContentType = XContentFactory.xContentType(datagramPacketReceive.getData(), datagramPacketReceive.getOffset(), datagramPacketReceive.getLength());
                                if (xContentType != null) {
                                    // an external ping
                                    externalPingData = XContentFactory.xContent(xContentType)
                                            .createParser(datagramPacketReceive.getData(), datagramPacketReceive.getOffset(), datagramPacketReceive.getLength())
                                            .mapAndClose();
                                } else {
                                    throw new ElasticSearchIllegalStateException("failed multicast message, probably message from previous version");
                                }
                            }
                        } catch (Exception e) {
                            logger.warn("failed to read requesting data from {}", e, datagramPacketReceive.getSocketAddress());
                            continue;
                        }
                    }
                    if (externalPingData != null) {
                        handleExternalPingRequest(externalPingData, xContentType, datagramPacketReceive.getSocketAddress());
                    } else {
                        handleNodePingRequest(id, requestingNodeX, clusterName);
                    }
                } catch (Exception e) {
                    logger.warn("unexpected exception in multicast receiver", e);
                }
            }
        }

        @SuppressWarnings("unchecked")
        private void handleExternalPingRequest(Map<String, Object> externalPingData, XContentType contentType, SocketAddress remoteAddress) {
            if (externalPingData.containsKey("response")) {
                // ignoring responses sent over the multicast channel
                logger.trace("got an external ping response (ignoring) from {}, content {}", remoteAddress, externalPingData);
                return;
            }

            if (multicastSocket == null) {
                logger.debug("can't send ping response, no socket, from {}, content {}", remoteAddress, externalPingData);
                return;
            }

            Map<String, Object> request = (Map<String, Object>) externalPingData.get("request");
            if (request == null) {
                logger.warn("malformed external ping request, no 'request' element from {}, content {}", remoteAddress, externalPingData);
                return;
            }

            String clusterName = request.containsKey("cluster_name") ? request.get("cluster_name").toString() : request.containsKey("clusterName") ? request.get("clusterName").toString() : null;
            if (clusterName == null) {
                logger.warn("malformed external ping request, missing 'cluster_name' element within request, from {}, content {}", remoteAddress, externalPingData);
                return;
            }

            if (!clusterName.equals(MulticastTaoPing.this.clusterName.value())) {
                logger.trace("got request for cluster_name {}, but our cluster_name is {}, from {}, content {}", clusterName, MulticastTaoPing.this.clusterName.value(), remoteAddress, externalPingData);
                return;
            }
            if (logger.isTraceEnabled()) {
                logger.trace("got external ping request from {}, content {}", remoteAddress, externalPingData);
            }

            try {

                XContentBuilder builder = XContentFactory.contentBuilder(contentType);
                builder.startObject().startObject("response");
                builder.field("cluster_name", MulticastTaoPing.this.clusterName.value());
                builder.startObject("version").field("number", Version.CURRENT.number()).field("snapshot_build", Version.CURRENT.snapshot).endObject();
                builder.field("transport_address", localNode.address().toString());

                builder.startObject("attributes");
                // no attributes
                builder.endObject();

                builder.endObject().endObject();
                synchronized (sendMutex) {
                    BytesReference bytes = builder.bytes();
                    datagramPacketSend.setData(bytes.array(), bytes.arrayOffset(), bytes.length());
                    multicastSocket.send(datagramPacketSend);
                    if (logger.isTraceEnabled()) {
                        logger.trace("sending external ping response {}", builder.string());
                    }
                }
            } catch (Exception e) {
                logger.warn("failed to send external multicast response", e);
            }
        }

        private void handleNodePingRequest(int id, DiscoveryNode requestingNodeX, ClusterName clusterName) {
            if (!pingEnabled) {
                return;
            }
            //DiscoveryNodes discoveryNodes = nodesProvider.nodes();
            final DiscoveryNode requestingNode = requestingNodeX;
            if (requestingNode.id().equals(localNode.id())) {
                // that's me, ignore
                return;
            }
            if (!clusterName.equals(MulticastTaoPing.this.clusterName)) {
                if (logger.isTraceEnabled()) {
                    logger.trace("[{}] received ping_request from [{}], but wrong cluster_name [{}], expected [{}], ignoring", id, requestingNode, clusterName, MulticastTaoPing.this.clusterName);
                }
                return;
            }
            // don't connect between two client nodes, no need for that...
            if (!localNode.shouldConnectTo(requestingNode)) {
                if (logger.isTraceEnabled()) {
                    logger.trace("[{}] received ping_request from [{}], both are client nodes, ignoring", id, requestingNode, clusterName);
                }
                return;
            }
            final MulticastPingResponse multicastPingResponse = new MulticastPingResponse();
            multicastPingResponse.id = id;
            multicastPingResponse.pingResponse = new PingResponse(localNode, clusterName);

            if (logger.isTraceEnabled()) {
                logger.trace("[{}] received ping_request from [{}], sending {}", id, requestingNode, multicastPingResponse.pingResponse);
            }

            if (!transportService.nodeConnected(requestingNode)) {
                // do the connect and send on a thread pool
                threadPool.generic().execute(new Runnable() {
                    @Override
                    public void run() {
                        // connect to the node if possible
                        try {
                            transportService.connectToNode(requestingNode);
                            transportService.sendRequest(requestingNode, MulticastTaoPingResponseRequestHandler.ACTION, multicastPingResponse, new EmptyTransportResponseHandler(TransportThreadPool.Names.SAME) {
                                @Override
                                public void handleException(TransportException exp) {
                                    logger.warn("failed to receive confirmation on sent ping response to [{}]", exp, requestingNode);
                                }
                            });
                        } catch (Exception e) {
                            logger.warn("failed to connect to requesting node {}", e, requestingNode);
                        }
                    }
                });
            } else {
                transportService.sendRequest(requestingNode, MulticastTaoPingResponseRequestHandler.ACTION, multicastPingResponse, new EmptyTransportResponseHandler(TransportThreadPool.Names.SAME) {
                    @Override
                    public void handleException(TransportException exp) {
                        logger.warn("failed to receive confirmation on sent ping response to [{}]", exp, requestingNode);
                    }
                });
            }
        }
    }
}
