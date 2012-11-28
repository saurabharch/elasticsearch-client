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

import org.elasticsearch.action.ShardOperationFailedException;
import org.elasticsearch.action.count.CountRequest;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.support.HttpRequest;
import org.elasticsearch.action.support.HttpResponse;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.action.support.HttpBaseAction;

import java.io.IOException;
import java.util.List;

public class HttpCountAction extends HttpBaseAction<CountRequest, CountResponse> {

    public final static String NAME = "count";
    private final static String ENDPOINT = "_count";

    @Override
    protected HttpRequest toRequest(CountRequest request) {
        HttpRequest httpRequest = new HttpRequest(GET, ENDPOINT)
                .param("operation_threading", request.operationThreading().name().toLowerCase())
                .param("routing", request.routing())
                .param("ignore_indices", request.ignoreIndices().name().toLowerCase())
                .param("query_hint", request.queryHint())
                .body(request.querySource());
        if (request.minScore() != -1) {
            httpRequest.param("min_score", request.minScore());
        }
        return httpRequest;
    }

    @Override
    protected CountResponse toResponse(HttpResponse response) throws IOException {
        long count = -1;
        int totalShards = -1;
        int successfulShards = -1;
        int failedShards = -1;
        List<ShardOperationFailedException> failures = null;
        XContentParser parser = response.parser();
        XContentParser.Token token = parser.nextToken();
        String currentFieldName = null;
        while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
                if ("failures".equals(currentFieldName)) {
                    failures = parseShardFailures(parser);
                }
            } else if (token.isValue()) {
                if ("count".equals(currentFieldName)) {
                    count = parser.longValue();
                } else if ("total".equals(currentFieldName)) {
                    totalShards = parser.intValue();
                } else if ("successful".equals(currentFieldName)) {
                    successfulShards = parser.intValue();
                } else if ("failed".equals(currentFieldName)) {
                    failedShards = parser.intValue();
                }
            }
        }
        return new CountResponse(count, totalShards, successfulShards, failedShards, failures);
    }
}
