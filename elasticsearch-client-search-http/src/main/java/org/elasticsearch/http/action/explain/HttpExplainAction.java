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

package org.elasticsearch.http.action.explain;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.explain.ExplainRequest;
import org.elasticsearch.action.explain.ExplainResponse;
import org.elasticsearch.action.support.HttpAction;
import org.elasticsearch.action.support.HttpRequest;
import org.elasticsearch.action.support.HttpResponse;
import org.elasticsearch.client.http.HttpSearchClient;
import org.elasticsearch.common.xcontent.XContentHelper;

import java.io.IOException;
import java.util.Map;

public class HttpExplainAction extends HttpAction<HttpSearchClient, ExplainRequest, ExplainResponse> {

    public final static String NAME = "explain";
    private final static String METHOD = "GET";
    private final static String ENDPOINT = "_explain";
    
    @Override
    protected void doExecute(HttpSearchClient client, ExplainRequest request, ActionListener<ExplainResponse> listener) {
        HttpRequest httpRequest = new HttpRequest(client.settings(), METHOD, ENDPOINT)
                .param("routing", request.routing())
                .param("parent", request.parent())
                .param("preference", request.preference())
                .body(request.source());
        submit(client, httpRequest, listener);        
    }

    @Override
    protected ExplainResponse toResponse(HttpResponse response) throws IOException {
        Map<String, Object> map = XContentHelper.convertToMap(response.getBody(), false).v2();
        logger.info("explain response = {}", map);
        return null;
    }
    
}
