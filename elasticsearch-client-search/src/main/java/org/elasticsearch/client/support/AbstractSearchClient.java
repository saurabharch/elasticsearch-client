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

package org.elasticsearch.client.support;

import org.elasticsearch.action.Action;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.count.CountAction;
import org.elasticsearch.action.count.CountRequest;
import org.elasticsearch.action.count.CountRequestBuilder;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.deletebyquery.DeleteByQueryAction;
import org.elasticsearch.action.deletebyquery.DeleteByQueryRequest;
import org.elasticsearch.action.deletebyquery.DeleteByQueryRequestBuilder;
import org.elasticsearch.action.deletebyquery.DeleteByQueryResponse;
import org.elasticsearch.action.explain.ExplainAction;
import org.elasticsearch.action.explain.ExplainRequest;
import org.elasticsearch.action.explain.ExplainRequestBuilder;
import org.elasticsearch.action.explain.ExplainResponse;
import org.elasticsearch.action.mlt.MoreLikeThisAction;
import org.elasticsearch.action.mlt.MoreLikeThisRequest;
import org.elasticsearch.action.mlt.MoreLikeThisRequestBuilder;
import org.elasticsearch.action.search.MultiSearchAction;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchRequestBuilder;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchAction;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollAction;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.search.SearchScrollRequestBuilder;
import org.elasticsearch.client.GenericClient;
import org.elasticsearch.client.SearchClient;

public abstract class AbstractSearchClient implements SearchClient {

    
    public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder>, SearchClient extends GenericClient> 
            RequestBuilder prepareExecute(final Action<Request, Response, RequestBuilder, SearchClient> action) {
        return action.newRequestBuilder((SearchClient)this);
    }

    
    public ActionFuture<DeleteByQueryResponse> deleteByQuery(final DeleteByQueryRequest request) {
        return execute(DeleteByQueryAction.INSTANCE, request);
    }

    
    public void deleteByQuery(final DeleteByQueryRequest request, final ActionListener<DeleteByQueryResponse> listener) {
        execute(DeleteByQueryAction.INSTANCE, request, listener);
    }

    
    public DeleteByQueryRequestBuilder prepareDeleteByQuery(String... indices) {
        return new DeleteByQueryRequestBuilder(this).setIndices(indices);
    }

    
    public ActionFuture<SearchResponse> search(final SearchRequest request) {
        return execute(SearchAction.INSTANCE, request);
    }

    
    public void search(final SearchRequest request, final ActionListener<SearchResponse> listener) {
        execute(SearchAction.INSTANCE, request, listener);
    }

    
    public SearchRequestBuilder prepareSearch(String... indices) {
        return new SearchRequestBuilder(this).setIndices(indices);
    }

    
    public ActionFuture<SearchResponse> searchScroll(final SearchScrollRequest request) {
        return execute(SearchScrollAction.INSTANCE, request);
    }

    
    public void searchScroll(final SearchScrollRequest request, final ActionListener<SearchResponse> listener) {
        execute(SearchScrollAction.INSTANCE, request, listener);
    }

    
    public SearchScrollRequestBuilder prepareSearchScroll(String scrollId) {
        return new SearchScrollRequestBuilder(this, scrollId);
    }

    
    public ActionFuture<MultiSearchResponse> multiSearch(MultiSearchRequest request) {
        return execute(MultiSearchAction.INSTANCE, request);
    }

    
    public void multiSearch(MultiSearchRequest request, ActionListener<MultiSearchResponse> listener) {
        execute(MultiSearchAction.INSTANCE, request, listener);
    }

    
    public MultiSearchRequestBuilder prepareMultiSearch() {
        return new MultiSearchRequestBuilder(this);
    }

    
    public ActionFuture<CountResponse> count(final CountRequest request) {
        return execute(CountAction.INSTANCE, request);
    }

    
    public void count(final CountRequest request, final ActionListener<CountResponse> listener) {
        execute(CountAction.INSTANCE, request, listener);
    }

    
    public CountRequestBuilder prepareCount(String... indices) {
        return new CountRequestBuilder(this).setIndices(indices);
    }

    
    public ActionFuture<SearchResponse> moreLikeThis(final MoreLikeThisRequest request) {
        return execute(MoreLikeThisAction.INSTANCE, request);
    }

    
    public void moreLikeThis(final MoreLikeThisRequest request, final ActionListener<SearchResponse> listener) {
        execute(MoreLikeThisAction.INSTANCE, request, listener);
    }

    
    public MoreLikeThisRequestBuilder prepareMoreLikeThis(String index, String type, String id) {
        return new MoreLikeThisRequestBuilder(this, index, type, id);
    }

    
    public ExplainRequestBuilder prepareExplain(String index, String type, String id) {
        return new ExplainRequestBuilder(this, index, type, id);
    }

    
    public ActionFuture<ExplainResponse> explain(ExplainRequest request) {
        return execute(ExplainAction.INSTANCE, request);
    }

    
    public void explain(ExplainRequest request, ActionListener<ExplainResponse> listener) {
        execute(ExplainAction.INSTANCE, request, listener);
    }
}
