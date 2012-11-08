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

package org.elasticsearch.http.action.admin.indices.alias;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesResponse;
import org.elasticsearch.action.support.HttpAction;
import org.elasticsearch.action.support.HttpClient;
import org.elasticsearch.action.support.HttpRequest;
import org.elasticsearch.action.support.HttpResponse;
import org.elasticsearch.cluster.metadata.AliasAction;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentHelper;

import java.io.IOException;
import java.util.Map;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

import org.elasticsearch.ElasticSearchGenerationException;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;

public class HttpIndicesAliasesAction extends HttpAction<IndicesAliasesRequest, IndicesAliasesResponse> {

    public static final String NAME = "indices_aliases";
    private static final String METHOD = "POST";
    private static final String ENDPOINT = "_aliases";

    @Override
    protected void doExecute(HttpClient client, IndicesAliasesRequest request, ActionListener<IndicesAliasesResponse> listener) {
        try {
            HttpRequest httpRequest = new HttpRequest(METHOD, ENDPOINT)
                    .param("master_timeout", request.masterNodeTimeout());
            XContentBuilder builder = jsonBuilder();
            builder.startObject().startArray("aliases");
            for (AliasAction action : request.aliasActions()) {
                builder.startObject().startObject(action.actionType().name().toLowerCase())
                        .field("index", action.index())
                        .field("alias", action.alias());
                if (action.filter() != null) {
                        builder.field("filter", parse(action.filter()));
                }
                builder.endObject().endObject();
            }
            builder.endArray().endObject();
            httpRequest.body(builder.string());
            submit(client, httpRequest, listener);
        } catch (IOException e) {
            throw new ElasticSearchGenerationException("Failed to generate", e);
        }
    }

    @Override
    protected IndicesAliasesResponse toResponse(HttpResponse response) throws IOException {
        Map<String, Object> map = XContentHelper.convertToMap(response.getBody(), false).v2();
        logger.info("response = {}", map);
        return null;
    }
    
    private XContentBuilder parse(String s) throws IOException {
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
