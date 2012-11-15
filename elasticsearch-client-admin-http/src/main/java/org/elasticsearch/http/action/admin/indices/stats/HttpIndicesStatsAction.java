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
package org.elasticsearch.http.action.admin.indices.stats;

import org.elasticsearch.action.admin.indices.stats.IndicesStatsRequest;
import org.elasticsearch.action.admin.indices.stats.IndicesStats;
import org.elasticsearch.action.support.HttpAction;
import org.elasticsearch.action.support.HttpRequest;
import org.elasticsearch.action.support.HttpResponse;
import org.elasticsearch.common.xcontent.XContentHelper;

import java.io.IOException;
import java.util.Map;

public class HttpIndicesStatsAction extends HttpAction<IndicesStatsRequest, IndicesStats> {

    public static final String NAME = "indices_stats";
    private static final String ENDPOINT = "_stats";

    @Override
    protected HttpRequest toRequest(IndicesStatsRequest request) {
        String endpoint = ENDPOINT;
        if (!request.isAll() && !request.isAll()) {
            if (request.docs()) {
                endpoint += "/docs";
            } else if (request.store()) {
                endpoint += "/store";
            } else if (request.indexing()) {
                endpoint += "/indexing";
            } else if (request.search()) {
                endpoint += "/search";
            } else if (request.get()) {
                endpoint += "/get";
            } else if (request.refresh()) {
                endpoint += "/refresh";
            } else if (request.merge()) {
                endpoint += "/merge";
            } else if (request.flush()) {
                endpoint += "/flush";
            } else if (request.warmer()) {
                endpoint += "/warner";
            }
        }
        HttpRequest httpRequest = new HttpRequest(GET, endpoint)
                .index(request.indices())
                .type(request.types())
                .param("clear", request.isClear())
                .param("all", request.isAll());
        if (request.ignoreIndices() != null) {
            httpRequest.param("ignore_indices", request.ignoreIndices().name().toLowerCase());
        }
        if (request.groups() != null) {
            httpRequest.param("groups", request.groups());
        }
        return httpRequest;
    }

    @Override
    protected IndicesStats toResponse(HttpResponse response) throws IOException {
        Map<String, Object> map = XContentHelper.convertToMap(response.getBody(), false).v2();
        logger.info("response = {}", map);
        return null;
    }
}
