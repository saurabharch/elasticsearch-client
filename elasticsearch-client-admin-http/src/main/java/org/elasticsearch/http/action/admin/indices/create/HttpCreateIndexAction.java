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
package org.elasticsearch.http.action.admin.indices.create;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.support.HttpAction;
import org.elasticsearch.action.support.HttpRequest;
import org.elasticsearch.action.support.HttpResponse;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.util.Map;

public class HttpCreateIndexAction extends HttpAction<CreateIndexRequest, CreateIndexResponse> {

    public static final String NAME = "index_create";

    @Override
    protected HttpRequest toRequest(CreateIndexRequest request) throws IOException {
        HttpRequest httpRequest = new HttpRequest(PUT, null)
                .index(request.index())
                .body(toBody(request.settings(), request.mappings()));
        return httpRequest;
    }

    @Override
    protected CreateIndexResponse toResponse(HttpResponse response) throws IOException {
        Map<String, Object> map = XContentHelper.convertToMap(response.getBody(), false).v2();
        logger.info("response = {}", map);
        return null;
    }

    private String toBody(Settings settings, Map<String, String> mappings) throws IOException {
        XContentBuilder builder = XContentFactory.contentBuilder(XContentType.JSON);
        builder.startObject().startObject("settings");
        for (Map.Entry<String, String> me : settings.getAsMap().entrySet()) {
            builder.field(me.getKey(), me.getValue());
        }
        builder.endObject().startObject().field("mappings");
        parseMappings(builder, mappings);
        builder.endObject().endObject();
        return builder.string();
    }

    private void parseMappings(XContentBuilder builder, Map<String, String> mappings) throws IOException {
        for (String type : mappings.keySet()) {
            builder.field(type, parseSource(mappings.get(type)));
        }
    }

    private XContentBuilder parseSource(String s) throws IOException {
        XContentParser parser = null;
        try {
            parser = XContentFactory.xContent(s).createParser(s);
            parser.nextToken();
            XContentBuilder builder = XContentFactory.jsonBuilder();
            builder.copyCurrentStructure(parser);
            return builder;
        } finally {
            if (parser != null) {
                parser.close();
            }
        }
    }
}
