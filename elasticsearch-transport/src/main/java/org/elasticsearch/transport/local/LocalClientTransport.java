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

package org.elasticsearch.transport.local;

import org.elasticsearch.transport.TransportRequestOptions;
import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.io.ThrowableObjectInputStream;
import org.elasticsearch.common.io.stream.*;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.BoundTransportAddress;
import org.elasticsearch.common.transport.LocalTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.*;
import org.elasticsearch.transport.support.ClientTransportStreams;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.Loggers;

import static org.elasticsearch.common.util.concurrent.ConcurrentCollections.newConcurrentMap;

/**
 *
 */
public class LocalClientTransport implements Transport {

    private final Settings settings;
        
    private final ESLogger logger;

    private final ThreadPool threadPool;

    private volatile TransportServiceAdapter transportServiceAdapter;

    private volatile BoundTransportAddress boundAddress;

    private volatile LocalTransportAddress localAddress;

    private final static ConcurrentMap<TransportAddress, LocalClientTransport> transports = newConcurrentMap();

    private static final AtomicLong transportAddressIdGenerator = new AtomicLong();

    private final ConcurrentMap<DiscoveryNode, LocalClientTransport> connectedNodes = newConcurrentMap();

    public LocalClientTransport(ThreadPool threadPool) {
        this(ImmutableSettings.Builder.EMPTY_SETTINGS, threadPool);
    }

    public LocalClientTransport(Settings settings, ThreadPool threadPool) {
        this.settings = settings;
        this.logger = Loggers.getLogger(getClass(), settings);
        this.threadPool = threadPool;
    }

    
    public TransportAddress[] addressesFromString(String address) {
        return new TransportAddress[]{new LocalTransportAddress(address)};
    }

    
    public boolean addressSupported(Class<? extends TransportAddress> address) {
        return LocalTransportAddress.class.equals(address);
    }

//    
    public LocalClientTransport start() throws ElasticSearchException {
        localAddress = new LocalTransportAddress(Long.toString(transportAddressIdGenerator.incrementAndGet()));
        transports.put(localAddress, this);
        boundAddress = new BoundTransportAddress(localAddress, localAddress);
        return this;
    }

 //   
    public LocalClientTransport stop() throws ElasticSearchException {
        transports.remove(localAddress);
        // now, go over all the transports connected to me, and raise disconnected event
        for (final LocalClientTransport targetTransport : transports.values()) {
            for (final Map.Entry<DiscoveryNode, LocalClientTransport> entry : targetTransport.connectedNodes.entrySet()) {
                if (entry.getValue() == this) {
                    targetTransport.disconnectFromNode(entry.getKey());
                }
            }
        }
        return this;
    }

    //
    public void close() throws ElasticSearchException {
    }

    
    public void transportServiceAdapter(TransportServiceAdapter transportServiceAdapter) {
        this.transportServiceAdapter = transportServiceAdapter;
    }

    
    public BoundTransportAddress boundAddress() {
        return boundAddress;
    }

    
    public boolean nodeConnected(DiscoveryNode node) {
        return connectedNodes.containsKey(node);
    }

    
    public void connectToNodeLight(DiscoveryNode node) throws ConnectTransportException {
        connectToNode(node);
    }

    
    public void connectToNode(DiscoveryNode node) throws ConnectTransportException {
        synchronized (this) {
            if (connectedNodes.containsKey(node)) {
                return;
            }
            final LocalClientTransport targetTransport = transports.get(node.address());
            if (targetTransport == null) {
                throw new ConnectTransportException(node, "Failed to connect");
            }
            connectedNodes.put(node, targetTransport);
            transportServiceAdapter.raiseNodeConnected(node);
        }
    }

    
    public void disconnectFromNode(DiscoveryNode node) {
        synchronized (this) {
            LocalClientTransport removed = connectedNodes.remove(node);
            if (removed != null) {
                transportServiceAdapter.raiseNodeDisconnected(node);
            }
        }
    }

    
    public long serverOpen() {
        return 0;
    }

    
    public void sendRequest(final DiscoveryNode node, final long requestId, final String action, final TransportRequest message, TransportRequestOptions options) throws IOException, TransportException {
        BasicCachedStreamOutput.Entry cachedEntry = BasicCachedStreamOutput.popEntry();
        try {
            StreamOutput stream = cachedEntry.handles();

            stream.writeLong(requestId);
            byte status = 0;
            status = ClientTransportStreams.statusSetRequest(status);
            stream.writeByte(status); // 0 for request, 1 for response.

            stream.writeString(action);
            message.writeTo(stream);

            stream.close();

            final LocalClientTransport targetTransport = connectedNodes.get(node);
            if (targetTransport == null) {
                throw new NodeNotConnectedException(node, "Node not connected");
            }

            final byte[] data = cachedEntry.bytes().bytes().copyBytesArray().toBytes();

            transportServiceAdapter.sent(data.length);

            threadPool.generic().execute(new Runnable() {
                
                public void run() {
                    targetTransport.messageReceived(data, action, LocalClientTransport.this, requestId);
                }
            });
        } finally {
            BasicCachedStreamOutput.pushEntry(cachedEntry);
        }
    }

    ThreadPool threadPool() {
        return this.threadPool;
    }

    void messageReceived(byte[] data, String action, LocalClientTransport sourceTransport, @Nullable final Long sendRequestId) {
        transportServiceAdapter.received(data.length);
        StreamInput stream = new BytesStreamInput(data, false);
        stream = BasicCachedStreamInput.cachedHandles(stream);

        try {
            long requestId = stream.readLong();
            byte status = stream.readByte();
            boolean isRequest = ClientTransportStreams.statusIsRequest(status);

            if (isRequest) {
                handleRequest(stream, requestId, sourceTransport);
            } else {
                final TransportResponseHandler handler = transportServiceAdapter.remove(requestId);
                // ignore if its null, the adapter logs it
                if (handler != null) {
                    if (ClientTransportStreams.statusIsError(status)) {
                        handlerResponseError(stream, handler);
                    } else {
                        handleResponse(stream, handler);
                    }
                }
            }
        } catch (Exception e) {
            if (sendRequestId != null) {
                TransportResponseHandler handler = transportServiceAdapter.remove(sendRequestId);
                if (handler != null) {
                    handler.handleException(new RemoteTransportException("", localAddress, action, e));
                }
            } else {
                logger.warn("Failed to receive message for action [" + action + "]", e);
            }
        }
    }

    private void handleRequest(StreamInput stream, long requestId, LocalClientTransport sourceTransport) throws Exception {
        final String action = stream.readString();
        final LocalClientTransportChannel transportChannel = new LocalClientTransportChannel(this, sourceTransport, action, requestId);
        try {
            final TransportRequestHandler handler = transportServiceAdapter.handler(action);
            if (handler == null) {
                throw new ActionNotFoundTransportException("Action [" + action + "] not found");
            }
            final TransportRequest streamable = handler.newInstance();
            streamable.readFrom(stream);
            handler.messageReceived(streamable, transportChannel);
        } catch (Exception e) {
            try {
                transportChannel.sendResponse(e);
            } catch (IOException e1) {
                logger.warn("Failed to send error message back to client for action [" + action + "]", e);
                logger.warn("Actual Exception", e1);
            }
        }
    }


    private void handleResponse(StreamInput buffer, final TransportResponseHandler handler) {
        final TransportResponse streamable = handler.newInstance();
        try {
            streamable.readFrom(buffer);
        } catch (Exception e) {
            handleException(handler, new TransportSerializationException("Failed to deserialize response of type [" + streamable.getClass().getName() + "]", e));
            return;
        }
        threadPool.executor(handler.executor()).execute(new Runnable() {
            @SuppressWarnings({"unchecked"})
            
            public void run() {
                try {
                    handler.handleResponse(streamable);
                } catch (Exception e) {
                    handleException(handler, new ResponseHandlerFailureTransportException(e));
                }
            }
        });
    }

    private void handlerResponseError(StreamInput buffer, final TransportResponseHandler handler) {
        Throwable error;
        try {
            ThrowableObjectInputStream ois = new ThrowableObjectInputStream(buffer, settings.getClassLoader());
            error = (Throwable) ois.readObject();
        } catch (Exception e) {
            error = new TransportSerializationException("Failed to deserialize exception response from stream", e);
        }
        handleException(handler, error);
    }

    private void handleException(final TransportResponseHandler handler, Throwable error) {
        if (!(error instanceof RemoteTransportException)) {
            error = new RemoteTransportException("None remote transport exception", error);
        }
        final RemoteTransportException rtx = (RemoteTransportException) error;
        handler.handleException(rtx);
    }
}
