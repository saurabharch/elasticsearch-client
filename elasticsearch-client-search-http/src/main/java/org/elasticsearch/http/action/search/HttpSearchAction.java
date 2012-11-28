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

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.HttpBaseAction;
import org.elasticsearch.action.support.HttpRequest;
import org.elasticsearch.action.support.HttpResponse;
import org.elasticsearch.common.xcontent.XContentHelper;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.elasticsearch.action.ShardOperationFailedException;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.search.facet.InternalFacets;
import org.elasticsearch.search.internal.InternalSearchHits;
import org.elasticsearch.search.internal.InternalSearchResponse;

public class HttpSearchAction extends HttpBaseAction<SearchRequest, SearchResponse> {

    public final static String NAME = "search";
    private final static String ENDPOINT = "_search";

    @Override
    protected HttpRequest toRequest(SearchRequest request) {
        HttpRequest httpRequest = new HttpRequest(POST, ENDPOINT)
                .param("routing", request.routing())
                .param("query_hint", request.queryHint())
                .body(request.source());
        if (request.operationThreading() != null) {
            httpRequest.param("operation_threading", request.operationThreading().name().toLowerCase());
        }
        if (request.ignoreIndices() != null) {
            httpRequest.param("ignore_indices", request.ignoreIndices().name().toLowerCase());
        }
        if (request.searchType() != null) {
            httpRequest.param("search_type", request.searchType().name().toLowerCase());
        }
        if (request.scroll() != null) {
            httpRequest.param("scroll", request.scroll().keepAlive().format());
        }
        return httpRequest;
    }

    @Override
    protected SearchResponse toResponse(HttpResponse response) throws IOException {
        logger.info("search response = {}", response);
        String scrollId = null;
        int totalShards = -1;
        int successfulShards = -1;
        int failedShards = -1;
        long tookInMillis = -1L;
        boolean timedOut = false;
        ShardSearchFailure[] shardFailures = null;

        InternalSearchHits hits = null;
        InternalFacets facets = null;

        //List<ShardOperationFailedException> failures = null;

        XContentParser parser = response.parser();
        XContentParser.Token token = parser.nextToken();
        String currentFieldName = null;

        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
                if ("_shards".equals(currentFieldName)) {
                    while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
                        if (token == XContentParser.Token.FIELD_NAME) {
                            currentFieldName = parser.currentName();
                            if ("failures".equals(currentFieldName)) {
                                //failures = parseShardFailures(parser);
                            }
                        } else if (token.isValue()) {
                            if ("total".equals(currentFieldName)) {
                                totalShards = parser.intValue();
                            } else if ("successful".equals(currentFieldName)) {
                                successfulShards = parser.intValue();
                            } else if ("failed".equals(currentFieldName)) {
                                failedShards = parser.intValue();
                            }
                        }
                    }
                } else if ("hits".equals(currentFieldName)) {
                    while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
                        if (token == XContentParser.Token.FIELD_NAME) {
                            currentFieldName = parser.currentName();
                        } else if (token.isValue()) {
                        }
                    }
                } else if (token.isValue()) {
                    if ("took".equals(currentFieldName)) {
                        tookInMillis = parser.longValue();
                    } else if ("timed_out".equals(currentFieldName)) {
                        timedOut = parser.booleanValue();
                    }
                }
            }
        }
            InternalSearchResponse ir = new InternalSearchResponse(hits, facets, timedOut);
            //Map<String, Object> map = XContentHelper.convertToMap(response.getBody(), false).v2();
            //logger.info("search response = {}", map);
            return new SearchResponse(ir, scrollId, totalShards, successfulShards, tookInMillis, shardFailures);
        
    }
}
