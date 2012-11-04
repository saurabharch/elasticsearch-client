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

package org.elasticsearch.http.action.count;

import java.io.IOException;
import java.util.Map;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.count.CountRequest;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.support.HttpAction;
import org.elasticsearch.action.support.HttpRequest;
import org.elasticsearch.action.support.HttpResponse;
import org.elasticsearch.client.http.HttpSearchClient;
import org.elasticsearch.common.xcontent.XContentHelper;

public class HttpCountAction extends HttpAction<HttpSearchClient, CountRequest, CountResponse> {

    public final static String NAME = "count";
    private final static String METHOD = "GET";
    private final static String ENDPOINT = "_count";
    
    @Override
    protected void doExecute(HttpSearchClient client, CountRequest request, ActionListener<CountResponse> listener) {
        HttpRequest httpRequest = new HttpRequest(client.settings(), METHOD, ENDPOINT)
                .param("operation_threading", request.operationThreading().name().toLowerCase())
                .param("routing", request.routing())
                .param("ignore_indices", request.indices())
                .param("query_hint", request.queryHint())
                .param("min_score", request.minScore())
                .body(request.querySource());
        submit(client, httpRequest, listener);        
    }

    @Override
    protected CountResponse toResponse(HttpResponse response) throws IOException {
        Map<String, Object> map = XContentHelper.convertToMap(response.getBody(), false).v2();
        logger.info("count response = ", map);
        return null;
    }
    
}
