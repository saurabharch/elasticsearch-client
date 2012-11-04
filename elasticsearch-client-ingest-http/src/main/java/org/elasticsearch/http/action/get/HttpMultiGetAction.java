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

package org.elasticsearch.http.action.get;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetRequest.Item;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.support.HttpAction;
import org.elasticsearch.action.support.HttpRequest;
import org.elasticsearch.action.support.HttpResponse;
import org.elasticsearch.client.http.HttpIngestClient;
import org.elasticsearch.common.io.stream.BytesStreamOutput;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.index.get.GetField;
import org.elasticsearch.index.get.GetResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class HttpMultiGetAction extends HttpAction<HttpIngestClient, MultiGetRequest, MultiGetResponse> {

    public static final String NAME = "mget";
    private static final String METHOD = "POST";
    private static final String ENDPOINT = "_mget";

    @Override
    protected void doExecute(final HttpIngestClient client, final MultiGetRequest request, final ActionListener<MultiGetResponse> listener) {
        HttpRequest httpRequest = new HttpRequest(client.settings(), METHOD, ENDPOINT);
        try {
            XContentBuilder builder = jsonBuilder().startObject().startArray("docs");
            for (Item item : request.items()) {
                builder.startObject()
                        .field("_index", item.index())
                        .field("_type", item.type())
                        .field("_id", item.id());
                if (item.fields() != null) {
                    builder.array("fields", item.fields());
                }
                builder.endObject();
            }
            builder.endArray().endObject();
            httpRequest.body(builder.string());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            listener.onFailure(e);
        }
        submit(client, httpRequest, listener);
    }

    @Override
    protected MultiGetResponse toResponse(HttpResponse response) throws IOException {
        Map<String, Object> map = XContentHelper.convertToMap(response.getBody(), false).v2();
        if (map.containsKey("docs")) {
            List<Map<String, Object>> docs = (List<Map<String, Object>>) map.get("docs");
            MultiGetItemResponse[] multiGetItems = new MultiGetItemResponse[docs.size()];
            for (int i = 0; i < docs.size(); i++) {
                Map<String, Object> doc = docs.get(i);
                BytesStreamOutput source = new BytesStreamOutput();
                if (map.containsKey("_source")) {
                    source.writeString(map.get("_source").toString());
                }
                Map<String, GetField> fields = null;
                if (doc.containsKey("fields")) {
                    fields = new HashMap();
                    Map<String, Object> fieldMap = (Map<String, Object>) map.get("fields");
                    for (String key : fieldMap.keySet()) {
                        Object o = fieldMap.get(key);
                        List<Object> l = new ArrayList();
                        if (o instanceof List) {
                            l.addAll((List) o);
                        } else {
                            l.add(o);
                        }
                        fields.put(key, new GetField(key, l));
                    }
                }
                MultiGetResponse.Failure failure = doc.containsKey("error") ?
                        new MultiGetResponse.Failure(
                        doc.get("_index").toString(),
                        doc.get("_type").toString(),
                        doc.get("_id").toString(),
                        doc.get("error").toString()
                        ) : null;
                GetResponse getResponse = failure == null ?
                        new GetResponse(new GetResult(
                        doc.get("_index").toString(),
                        doc.get("_type").toString(),
                        doc.get("_id").toString(),
                        Long.parseLong(doc.get("_version").toString()),
                        Boolean.parseBoolean(doc.get("exists").toString()),
                        source.bytes(),
                        fields)) : null;
                multiGetItems[i] = new MultiGetItemResponse(getResponse, failure);
            }
            return new MultiGetResponse(multiGetItems);
        }
        return null;
    }
}
