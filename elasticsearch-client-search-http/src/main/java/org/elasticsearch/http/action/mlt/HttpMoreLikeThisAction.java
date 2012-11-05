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

package org.elasticsearch.http.action.mlt;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.HttpAction;
import org.elasticsearch.action.support.HttpClient;
import org.elasticsearch.action.support.HttpRequest;
import org.elasticsearch.action.support.HttpResponse;
import org.elasticsearch.action.mlt.MoreLikeThisRequest;
import org.elasticsearch.common.xcontent.XContentHelper;

import java.io.IOException;
import java.util.Map;

public class HttpMoreLikeThisAction extends HttpAction<MoreLikeThisRequest, SearchResponse> {

    public final static String NAME = "mlt";
    private final static String METHOD = "POST";
    private final static String ENDPOINT = "_mlt";

    @Override
    protected void doExecute(HttpClient client, MoreLikeThisRequest request, ActionListener<SearchResponse> listener) {
        HttpRequest httpRequest = new HttpRequest(METHOD, ENDPOINT)
                .param("mlt_fields", request.fields())
                .param("percent_terms_to_match", request.percentTermsToMatch())
                .param("min_term_freq", request.minTermFreq())
                .param("max_query_terms", request.maxQueryTerms())
                .param("stop_words", request.stopWords())
                .param("min_doc_freq", request.minDocFreq())
                .param("max_doc_freq", request.maxDocFreq())
                .param("min_word_len", request.minWordLen())
                .param("boost_terms", request.boostTerms())
                .param("search_type", request.searchTypes())
                .param("search_indices", request.searchIndices())
                .param("search_types", request.searchTypes())
                .param("search_query_hint", request.searchQueryHint())
                .param("search_size", request.searchSize())
                .param("search_from", request.searchFrom());
        if (request.searchScroll() != null) {
            httpRequest.param("search_scroll", request.searchScroll().keepAlive().format());
        }
        if (request.searchSource() != null) {
            httpRequest.param("search_source", request.searchSource().toUtf8());
        }
        submit(client, httpRequest, listener);
    }

    @Override
    protected SearchResponse toResponse(HttpResponse response) throws IOException {
        Map<String, Object> map = XContentHelper.convertToMap(response.getBody(), false).v2();
        logger.info("search mlt response = {}", map);
        return null;
    }
}

/*

 */
