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

package org.elasticsearch.http.action.update;

import org.elasticsearch.action.support.HttpAction;
import org.elasticsearch.action.support.HttpRequest;
import org.elasticsearch.action.support.HttpResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.common.xcontent.XContentHelper;

import java.util.Map;

public class HttpUpdateAction extends HttpAction<UpdateRequest, UpdateResponse> {

    public static final String NAME = "update";
    private static final String METHOD = "POST";
    private static final String ENDPOINT = "_update";

    @Override
    protected HttpRequest toRequest(final UpdateRequest request) {
        HttpRequest httpRequest = new HttpRequest(METHOD, ENDPOINT)
                .index(request.index())
                .type(request.type())
                .id(request.id())
                .param("routing", request.routing())
                .param("parent", request.parent())
                .param("refresh", request.refresh())
                .param("percolate", request.percolate())
                .param("replication", request.replicationType().name().toLowerCase())
                .param("consistency", request.consistencyLevel().name().toLowerCase())
                .param("routing", request.routing())
                .param("script", request.script())
                .param("lang", request.scriptLang())
                .param("fields", request.fields())
                .param("retry_on_conflict", request.retryOnConflict());
        if (request.doc() != null) {
            httpRequest.body(request.doc().source());
        } else {
            if (request.scriptParams() != null) {
                for (Map.Entry<String, Object> me : request.scriptParams().entrySet()) {
                    httpRequest.param("sp_" + me.getKey(), me.getValue().toString());
                }
            }
        }
        return httpRequest;
    }

    @Override
    protected UpdateResponse toResponse(HttpResponse response) {
        Map<String, Object> map = XContentHelper.convertToMap(response.getBody(), false).v2();
        UpdateResponse updateResponse = new UpdateResponse(
                map.get("_index").toString(),
                map.get("_type").toString(),
                map.get("_id").toString(),
                Long.parseLong(map.get("_version").toString()));
        return updateResponse;
    }
}
