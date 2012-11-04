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

package org.elasticsearch.http.action.bulk;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.support.HttpAction;
import org.elasticsearch.action.support.HttpRequest;
import org.elasticsearch.action.support.HttpResponse;
import org.elasticsearch.client.http.HttpIngestClient;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.xcontent.XContentHelper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class HttpBulkAction extends HttpAction<HttpIngestClient, BulkRequest, BulkResponse> {

    public static final String NAME = "bulk";
    private static final String METHOD = "POST";
    private static final String ENDPOINT = "_bulk";

    @Override
    protected void doExecute(final HttpIngestClient client, final BulkRequest bulkRequest, final ActionListener<BulkResponse> listener) {
        try {
            StringBuilder out = new StringBuilder();
            for (ActionRequest request : bulkRequest.requests()) {
                if (request instanceof IndexRequest) {
                    IndexRequest indexRequest = (IndexRequest) request;
                    formatBulk(out, indexRequest);
                } else if (request instanceof DeleteRequest) {
                    DeleteRequest deleteRequest = (DeleteRequest) request;
                    formatBulk(out, deleteRequest);
                }
            }
            HttpRequest httpRequest = new HttpRequest(client.settings(), METHOD, ENDPOINT)
                    .param("replication", bulkRequest.replicationType().name().toLowerCase())
                    .param("consistency", bulkRequest.consistencyLevel().name().toLowerCase())
                    .param("refresh", Boolean.toString(bulkRequest.refresh()))
                    .body(out);
            submit(client, httpRequest, listener);
        } catch (IOException e) {
            listener.onFailure(e);
        }
    }

    @Override
    protected BulkResponse toResponse(HttpResponse response) {
        BulkResponse br = null;
        Map<String, Object> map = XContentHelper.convertToMap(response.getBody(), false).v2();
        Object o = map.get("items");
        if (o instanceof List) {
            List<Map<String, Object>> l = (List<Map<String, Object>>) o;
            BulkItemResponse[] items = new BulkItemResponse[l.size()];
            for (int i = 0; i < l.size(); i++) {
                Map<String, Object> m = l.get(i);
                String opType = m.keySet().iterator().next();
                Map<String, Object> itemMap = (Map<String, Object>) m.get(opType);
                if (itemMap.containsKey("error")) {
                    BulkItemResponse.Failure failure = new BulkItemResponse.Failure(
                            itemMap.get("_index").toString(),
                            itemMap.get("_type").toString(),
                            itemMap.get("_id").toString(),
                            itemMap.get("error").toString());
                    items[i] = new BulkItemResponse(i, opType, failure);
                } else if ("index".equals(opType) || "create".equals(opType)) {
                    IndexResponse indexResponse = new IndexResponse(
                            itemMap.get("_index").toString(),
                            itemMap.get("_type").toString(),
                            itemMap.get("_id").toString(),
                            Long.parseLong(itemMap.get("_version").toString()));
                    items[i] = new BulkItemResponse(i, opType, indexResponse);
                } else if ("delete".equals(opType)) {
                    DeleteResponse deleteResponse = new DeleteResponse(
                            itemMap.get("_index").toString(),
                            itemMap.get("_type").toString(),
                            itemMap.get("_id").toString(),
                            Long.parseLong(itemMap.get("_version").toString()),
                            !Boolean.parseBoolean(itemMap.get("found").toString()));
                    items[i] = new BulkItemResponse(i, opType, deleteResponse);
                }
            }
            br = new BulkResponse(items, Long.parseLong(map.get("took").toString()));
        }
        return br;
    }

    private void formatBulk(StringBuilder out, IndexRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"").append(request.opType().name().toLowerCase())
                .append("\":{\"_index\":\"")
                .append(request.index())
                .append("\",\"_type\":\"")
                .append(request.type())
                .append("\",\"_id\":\"")
                .append(request.id())
                .append("\"}}\n");
        formatBulk(out, sb, request.source());
    }

    private void formatBulk(StringBuilder out, DeleteRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"delete\":{\"_index\":\"")
                .append(request.index())
                .append("\",\"_type\":\"")
                .append(request.type())
                .append("\",\"_id\":\"")
                .append(request.id())
                .append("\"}}\n");
        formatBulk(out, sb, null);
    }

    private void formatBulk(StringBuilder out, StringBuilder action, BytesReference source) throws IOException {
        out.append(action);
        if (source != null) {
            out.append(source.toUtf8().replace('\n', ' ')).append("\n");
        }
    }
}
