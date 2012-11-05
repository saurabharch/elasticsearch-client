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

import com.ning.http.client.AsyncHandler;
import com.ning.http.client.AsyncHandler.STATE;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.HttpResponseBodyPart;
import com.ning.http.client.HttpResponseHeaders;
import com.ning.http.client.HttpResponseStatus;

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

import static org.elasticsearch.action.support.PlainActionFuture.newFuture;

import java.io.IOException;

public abstract class HttpAction<Request extends ActionRequest, Response extends ActionResponse> {

    protected ESLogger logger = ESLoggerFactory.getLogger(HttpAction.class.getName());

    public ActionFuture<Response> execute(HttpClient client, Request request) throws ElasticSearchException {
        PlainActionFuture<Response> future = newFuture();
        request.listenerThreaded(false);
        execute(client, request, future);
        return future;
    }

    public void execute(HttpClient client, Request request, ActionListener<Response> listener) {
        ActionRequestValidationException validationException = request.validate();
        if (validationException != null) {
            listener.onFailure(validationException);
            return;
        }
        try {
            doExecute(client, request, listener);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            listener.onFailure(e);
        }
    }

    protected abstract void doExecute(HttpClient client, Request request, ActionListener<Response> listener);

    protected abstract Response toResponse(HttpResponse response) throws IOException;

    protected void submit(HttpClient client, HttpRequest request, ActionListener<Response> listener) {        
        AsyncHttpClient.BoundRequestBuilder builder = client.prepareRequest(request.buildRequest(client.settings()));
        if (logger.isDebugEnabled()) {
            logger.debug("submitting request = {}, body = {}", builder.build().toString(), builder.build().getStringData());
        }
        AsyncHandler<HttpResponse> handler = new WrapHandler(client.settings(), listener);
        try {
            builder.execute(handler);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            listener.onFailure(e);
        }
    }

    private class WrapHandler implements AsyncHandler<HttpResponse> {

        private final ActionListener<Response> listener;
        private final BytesStreamOutput body;
        private HttpResponseStatus statuscode;
        private HttpResponseHeaders headers;
        private String contentType;

        WrapHandler(Settings settings, ActionListener<Response> listener) {
            this.listener = listener;
            this.body = new BytesStreamOutput();
        }

        @Override
        public STATE onStatusReceived(HttpResponseStatus hrs) throws Exception {
            if (logger.isDebugEnabled()) {
                logger.debug("onStatusReceived {}", hrs);
            }
            this.statuscode = hrs;
            return STATE.CONTINUE;
        }

        @Override
        public STATE onHeadersReceived(HttpResponseHeaders hrh) throws Exception {
            if (logger.isDebugEnabled()) {
                logger.debug("onHeadersReceived {}", hrh);
            }
            this.headers = hrh;
            this.contentType = hrh.getHeaders().getFirstValue("Content-type");
            return STATE.CONTINUE;
        }

        @Override
        public STATE onBodyPartReceived(HttpResponseBodyPart hrbp) throws Exception {
            if (logger.isDebugEnabled()) {
                logger.debug("onBodyPartReceived {}", new String(hrbp.getBodyPartBytes()));
            }
            body.writeBytes(hrbp.getBodyPartBytes());
            return STATE.CONTINUE;
        }

        @Override
        public void onThrowable(Throwable t) {
            logger.error(t.getMessage(), t);
            listener.onFailure(t);
        }

        @Override
        public HttpResponse onCompleted() {
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
                    listener.onResponse(toResponse(response));
                } else {
                    throw new IOException("HTTP error " + response.getStatusCode() + " message: " + response.getBody().toUtf8());
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                listener.onFailure(e);
            }
            return response;
        }
    }
}
