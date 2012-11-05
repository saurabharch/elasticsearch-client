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
package org.elasticsearch.http.action.search;

import org.elasticsearch.ElasticSearchGenerationException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.support.HttpAction;
import org.elasticsearch.action.support.HttpClient;
import org.elasticsearch.action.support.HttpRequest;
import org.elasticsearch.action.support.HttpResponse;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentHelper;

import java.io.IOException;
import java.util.Map;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

public class HttpMultiSearchAction extends HttpAction<MultiSearchRequest, MultiSearchResponse> {

    public final static String NAME = "msearch";
    private final static String METHOD = "POST";
    private final static String ENDPOINT = "_msearch";

    @Override
    protected void doExecute(HttpClient client, MultiSearchRequest request, ActionListener<MultiSearchResponse> listener) {
        HttpRequest httpRequest = new HttpRequest(METHOD, ENDPOINT)
                .param("ignore_indices", request.ignoreIndices().name().toLowerCase());
        StringBuilder sb = new StringBuilder();
        for (SearchRequest sr : request.requests()) {
            format(sb, sr);
        }
        httpRequest.body(sb);
        submit(client, httpRequest, listener);
    }

    @Override
    protected MultiSearchResponse toResponse(HttpResponse response) throws IOException {
        Map<String, Object> map = XContentHelper.convertToMap(response.getBody(), false).v2();
        logger.info("search response = {}", map);
        return null;
    }

    private void format(StringBuilder sb, SearchRequest sr) {
        try {
            XContentBuilder builder = jsonBuilder();
            builder.startObject()
                    .field("index", Strings.arrayToCommaDelimitedString(sr.indices()))
                    .field("type", Strings.arrayToCommaDelimitedString(sr.types()))
                    .field("search_type", sr.searchType().name().toLowerCase())
                    .field("preference", sr.preference())
                    .field("routing", sr.routing())
                    .field("ignore_indices", sr.ignoreIndices())
                    .field("query_hint", sr.queryHint())
                    .field("operation_threading", sr.operationThreading());
            if (sr.scroll() != null) {
                builder.field("scroll", sr.scroll().keepAlive().format());
            }
            builder.endObject();
            sb.append(builder.string()).append("\n").append(sr.source().toUtf8().replace('\n', ' ')).append("\n");
        } catch (IOException e) {
            throw new ElasticSearchGenerationException("Failed to generate", e);
        }
    }
}
