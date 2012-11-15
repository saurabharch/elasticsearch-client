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
package org.elasticsearch.http.action.index;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequest.OpType;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.HttpAction;
import org.elasticsearch.action.support.HttpRequest;
import org.elasticsearch.action.support.HttpResponse;
import org.elasticsearch.common.xcontent.XContentHelper;

import java.util.Map;

public class HttpIndexAction extends HttpAction<IndexRequest, IndexResponse> {

    public static final String NAME = "index";

    @Override
    protected HttpRequest toRequest(final IndexRequest request) {
        HttpRequest httpRequest = new HttpRequest(PUT, request.opType().equals(OpType.CREATE) ? "_create" : null)
                .index(request.index())
                .type(request.type())
                .id(request.id())
                .param("routing", request.routing())
                .param("parent", request.parent())
                .param("timestamp", request.timestamp())
                .param("refresh", request.refresh())
                .param("version", request.version())
                .param("version_type", request.versionType().name().toLowerCase())
                .param("percolate", request.percolate())
                .param("op_type", request.opType().name().toLowerCase())
                .param("replication", request.replicationType().name().toLowerCase())
                .param("consistency", request.consistencyLevel().name().toLowerCase())
                .body(request.source());
        if (request.ttl() > 0) {
            httpRequest.param("ttl", request.ttl());
        }
        return httpRequest;
    }

    @Override
    protected IndexResponse toResponse(HttpResponse response) {
        if (logger.isDebugEnabled()) {
            logger.debug("toResponse = {}", response);
        }
        Map<String, Object> map = XContentHelper.convertToMap(response.getBody(), false).v2();
        IndexResponse indexResponse = new IndexResponse(
                map.get("_index").toString(),
                map.get("_type").toString(),
                map.get("_id").toString(),
                Long.parseLong(map.get("_version").toString()));
        return indexResponse;
    }
}
