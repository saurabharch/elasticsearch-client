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

import org.elasticsearch.common.io.ThrowableObjectOutputStream;
import org.elasticsearch.common.io.stream.BytesStreamOutput;
import org.elasticsearch.common.io.stream.BasicCachedStreamOutput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.io.stream.Streamable;
import org.elasticsearch.transport.NotSerializableTransportException;
import org.elasticsearch.transport.RemoteTransportException;
import org.elasticsearch.transport.TransportChannel;
import org.elasticsearch.transport.TransportResponseOptions;
import org.elasticsearch.transport.support.ClientTransportStreams;

import java.io.IOException;
import java.io.NotSerializableException;
import org.elasticsearch.transport.TransportRequest;
import org.elasticsearch.transport.TransportResponse;

/**
 *
 */
public class LocalClientTransportChannel implements TransportChannel {

    private final LocalClientTransport sourceTransport;

    // the transport we will *send to*
    private final LocalClientTransport targetTransport;

    private final String action;

    private final long requestId;

    public LocalClientTransportChannel(LocalClientTransport sourceTransport, LocalClientTransport targetTransport, String action, long requestId) {
        this.sourceTransport = sourceTransport;
        this.targetTransport = targetTransport;
        this.action = action;
        this.requestId = requestId;
    }

    public String action() {
        return action;
    }

    public void sendResponse(TransportResponse message) throws IOException {
        sendResponse(message, TransportResponseOptions.EMPTY);
    }

    public void sendResponse(TransportResponse message, TransportResponseOptions options) throws IOException {
        BasicCachedStreamOutput.Entry cachedEntry = BasicCachedStreamOutput.popEntry();
        try {
            StreamOutput stream = cachedEntry.handles();
            stream.writeLong(requestId);
            byte status = 0;
            status = ClientTransportStreams.statusSetResponse(status);
            stream.writeByte(status); // 0 for request, 1 for response.
            message.writeTo(stream);
            stream.close();
            final byte[] data = cachedEntry.bytes().bytes().copyBytesArray().toBytes();
            targetTransport.threadPool().generic().execute(new Runnable() {
                public void run() {
                    targetTransport.messageReceived(data, action, sourceTransport, null);
                }
            });
        } finally {
            BasicCachedStreamOutput.pushEntry(cachedEntry);
        }
    }

    public void sendResponse(Throwable error) throws IOException {
        BasicCachedStreamOutput.Entry cachedEntry = BasicCachedStreamOutput.popEntry();
        try {
            BytesStreamOutput stream;
            try {
                stream = cachedEntry.bytes();
                writeResponseExceptionHeader(stream);
                RemoteTransportException tx = new RemoteTransportException("", targetTransport.boundAddress().boundAddress(), action, error);
                ThrowableObjectOutputStream too = new ThrowableObjectOutputStream(stream);
                too.writeObject(tx);
                too.close();
            } catch (NotSerializableException e) {
                cachedEntry.reset();
                stream = cachedEntry.bytes();
                writeResponseExceptionHeader(stream);
                RemoteTransportException tx = new RemoteTransportException("", targetTransport.boundAddress().boundAddress(), action, new NotSerializableTransportException(error));
                ThrowableObjectOutputStream too = new ThrowableObjectOutputStream(stream);
                too.writeObject(tx);
                too.close();
            }
            final byte[] data = stream.bytes().copyBytesArray().toBytes();
            targetTransport.threadPool().generic().execute(new Runnable() {
                public void run() {
                    targetTransport.messageReceived(data, action, sourceTransport, null);
                }
            });
        } finally {
            BasicCachedStreamOutput.pushEntry(cachedEntry);
        }
    }

    private void writeResponseExceptionHeader(BytesStreamOutput stream) throws IOException {
        stream.writeLong(requestId);
        byte status = 0;
        status = ClientTransportStreams.statusSetResponse(status);
        status = ClientTransportStreams.statusSetError(status);
        stream.writeByte(status);
    }
}
