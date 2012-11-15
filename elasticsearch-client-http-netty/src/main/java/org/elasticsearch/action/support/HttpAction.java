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

import static org.elasticsearch.action.support.HttpActionFuture.newFuture;

import com.ning.http.client.AsyncHandler;
import com.ning.http.client.AsyncHandler.STATE;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.HttpResponseBodyPart;
import com.ning.http.client.HttpResponseHeaders;
import com.ning.http.client.HttpResponseStatus;
import java.io.IOException;
import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.common.io.stream.BytesStreamOutput;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.settings.Settings;

public abstract class HttpAction<Request extends ActionRequest, Response extends ActionResponse> {

    protected ESLogger logger = ESLoggerFactory.getLogger(HttpAction.class.getName());
    protected final static String GET = "GET";
    protected final static String HEAD = "HEAD";
    protected final static String PUT = "PUT";
    protected final static String POST = "POST";
    protected final static String DELETE = "DELETE";

    protected abstract HttpRequest toRequest(Request request) throws IOException;

    protected abstract Response toResponse(HttpResponse response) throws IOException;

    public ActionFuture<Response> execute(HttpClient client, Request request, ActionListener<Response> listener)
            throws ElasticSearchException, ActionRequestValidationException {
        request.listenerThreaded(false);
        ActionRequestValidationException validationException = request.validate();
        if (validationException != null) {
            throw validationException;
        }
        try {
            HttpRequest httpRequest = toRequest(request);
            AsyncHttpClient.BoundRequestBuilder builder = client.prepareRequest(httpRequest.buildRequest(client.settings()));
            WrapHandler handler = new WrapHandler(client.settings());
            if (logger.isDebugEnabled()) {
                logger.debug("submitting request = {}, body = {}", builder.build().toString(), builder.build().getStringData());
            }
            HttpActionFuture<Response> future = newFuture(builder.execute(handler));
            future.listener(listener);
            handler.listener(future);
            if (logger.isDebugEnabled()) {
                logger.debug("submission completed, future = {}", future);
            }
            return future;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new ElasticSearchException(e.getMessage(), e);
        }
    }

    private class WrapHandler implements AsyncHandler<Response> {

        private ActionListener<Response> listener;
        private final BytesStreamOutput body;
        private HttpResponseStatus statuscode;
        private HttpResponseHeaders headers;
        private String contentType;

        WrapHandler(Settings settings) {
            this.body = new BytesStreamOutput();
        }

        void listener(ActionListener<Response> listener) {
            this.listener = listener;
        }

        public STATE onStatusReceived(HttpResponseStatus hrs) throws Exception {
            if (logger.isDebugEnabled()) {
                logger.debug("onStatusReceived {}", hrs.getStatusCode());
            }
            this.statuscode = hrs;
            return STATE.CONTINUE;
        }

        public STATE onHeadersReceived(HttpResponseHeaders hrh) throws Exception {
            if (logger.isDebugEnabled()) {
                logger.debug("onHeadersReceived {}", hrh.getHeaders());
            }
            this.headers = hrh;
            this.contentType = hrh.getHeaders().getFirstValue("Content-type");
            return STATE.CONTINUE;
        }

        public STATE onBodyPartReceived(HttpResponseBodyPart hrbp) throws Exception {
            if (logger.isDebugEnabled()) {
                logger.debug("onBodyPartReceived {}", new String(hrbp.getBodyPartBytes()));
            }
            body.writeBytes(hrbp.getBodyPartBytes());
            return STATE.CONTINUE;
        }

        public void onThrowable(Throwable t) {
            logger.error(t.getMessage(), t);
            if (listener != null) {
                listener.onFailure(t);
            }
        }

        public Response onCompleted() {
            HttpResponse response = new HttpResponse(
                    statuscode != null ? statuscode.getStatusCode() : -1,
                    contentType,
                    headers != null ? headers.getHeaders() : null,
                    body.bytes());
            if (logger.isDebugEnabled()) {
                logger.debug("onCompleted {}", response);
            }
            try {
                if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("onCompleted calling onResponse");
                    }
                    if (listener != null) {
                        listener.onResponse(toResponse(response));
                    }
                } else {
                    throw new IOException("HTTP error " + response.getStatusCode() + " message: " + response.getBody().toUtf8());
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                if (listener != null) {
                    listener.onFailure(e);
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("onCompleted done");
            }
            return (Response) response;
        }
    }
}
