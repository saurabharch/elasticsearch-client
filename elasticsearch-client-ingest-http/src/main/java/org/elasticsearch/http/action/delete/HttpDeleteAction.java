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

package org.elasticsearch.http.action.delete;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.support.HttpAction;
import org.elasticsearch.action.support.HttpRequest;
import org.elasticsearch.action.support.HttpResponse;
import org.elasticsearch.client.http.HttpIngestClient;
import org.elasticsearch.common.xcontent.XContentHelper;

import java.io.IOException;
import java.util.Map;

public class HttpDeleteAction extends HttpAction<HttpIngestClient, DeleteRequest, DeleteResponse> {

    public static final String NAME = "delete";
    private static final String METHOD = "DELETE";

    @Override
    protected void doExecute(final HttpIngestClient client, final DeleteRequest request, final ActionListener<DeleteResponse> listener) {
        HttpRequest httpRequest = new HttpRequest(client.settings(), METHOD, null)
                .index(request.index())
                .type(request.type())
                .id(request.id())
                .param("routing", request.routing())
                .param("parent", request.parent())
                .param("refresh", request.refresh())
                ;
        submit(client, httpRequest, listener);
    }

    @Override
    protected DeleteResponse toResponse(HttpResponse response) throws IOException {
        Map<String, Object> map = XContentHelper.convertToMap(response.getBody(), false).v2();
        DeleteResponse deleteResponse = new DeleteResponse(
                map.get("_index").toString(),
                map.get("_type").toString(),
                map.get("_id").toString(),
                Long.parseLong(map.get("_version").toString()),
                !Boolean.parseBoolean(map.get("found").toString()));
        return deleteResponse;
    }
}