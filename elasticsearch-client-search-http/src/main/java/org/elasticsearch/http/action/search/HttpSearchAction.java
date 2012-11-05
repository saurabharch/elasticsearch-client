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

package org.elasticsearch.http.action.search;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.HttpAction;
import org.elasticsearch.action.support.HttpClient;
import org.elasticsearch.action.support.HttpRequest;
import org.elasticsearch.action.support.HttpResponse;
import org.elasticsearch.common.xcontent.XContentHelper;

import java.io.IOException;
import java.util.Map;

public class HttpSearchAction extends HttpAction<SearchRequest, SearchResponse> {

    public final static String NAME = "search";
    private final static String METHOD = "POST";
    private final static String ENDPOINT = "_search";
    
    @Override
    protected void doExecute(HttpClient client, SearchRequest request, ActionListener<SearchResponse> listener) {
        HttpRequest httpRequest = new HttpRequest(METHOD, ENDPOINT)
                .param("operation_threading", request.operationThreading().name().toLowerCase())
                .param("routing", request.routing())
                .param("ignore_indices", request.ignoreIndices().name().toLowerCase())
                .param("query_hint", request.queryHint())
                .param("search_type", request.searchType().name().toLowerCase())
                .param("scroll", request.scroll().keepAlive().format())
                .body(request.source());
        submit(client, httpRequest, listener);        
    }

    @Override
    protected SearchResponse toResponse(HttpResponse response) throws IOException {
        Map<String, Object> map = XContentHelper.convertToMap(response.getBody(), false).v2();
        logger.info("search response = {}", map);
        return null;
    }
    
}
