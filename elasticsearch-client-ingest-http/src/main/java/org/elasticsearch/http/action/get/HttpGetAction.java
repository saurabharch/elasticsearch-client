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
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.support.HttpAction;
import org.elasticsearch.action.support.HttpRequest;
import org.elasticsearch.action.support.HttpResponse;
import org.elasticsearch.client.http.HttpIngestClient;
import org.elasticsearch.common.io.stream.BytesStreamOutput;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.index.get.GetField;
import org.elasticsearch.index.get.GetResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpGetAction extends HttpAction<HttpIngestClient, GetRequest, GetResponse> {

    public static final String NAME = "get";
    private static final String METHOD = "GET";

    @Override
    protected void doExecute(final HttpIngestClient client, final GetRequest request, final ActionListener<GetResponse> listener) {
        HttpRequest httpRequest = new HttpRequest(client.settings(), METHOD, null)
                .index(request.index())
                .type(request.type())
                .id(request.id())
                .param("fields", request.fields());
        submit(client, httpRequest, listener);
    }

    @Override
    protected GetResponse toResponse(HttpResponse response) throws IOException {
        Map<String, Object> map = XContentHelper.convertToMap(response.getBody(), false).v2();
        BytesStreamOutput source = new BytesStreamOutput();
        if (map.containsKey("_source")) {
            source.writeString(map.get("_source").toString());
        }
        Map<String, GetField> fields = null;
        if (map.containsKey("fields")) {
            fields = new HashMap();
            Map<String, Object> fieldMap = (Map<String, Object>) map.get("fields");
            for (String key : fieldMap.keySet()) {
                Object o = fieldMap.get(key);
                List<Object> l = new ArrayList();
                if (o instanceof Collection) {
                    l.addAll((Collection)o);
                }
                else {
                    l.add(o);
                }
                fields.put(key, new GetField(key, l));
            }
        }
        GetResult result = new GetResult(
                map.get("_index").toString(),
                map.get("_type").toString(),
                map.get("_id").toString(),
                Long.parseLong(map.get("_version").toString()),
                Boolean.parseBoolean(map.get("exists").toString()),
                source.bytes(),
                fields);
        return new GetResponse(result);
    }
}
