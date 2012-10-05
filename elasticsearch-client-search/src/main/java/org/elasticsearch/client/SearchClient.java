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

package org.elasticsearch.client;

import org.elasticsearch.action.Action;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.count.CountRequest;
import org.elasticsearch.action.count.CountRequestBuilder;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.action.deletebyquery.DeleteByQueryRequest;
import org.elasticsearch.action.deletebyquery.DeleteByQueryRequestBuilder;
import org.elasticsearch.action.deletebyquery.DeleteByQueryResponse;
import org.elasticsearch.action.explain.ExplainRequest;
import org.elasticsearch.action.explain.ExplainRequestBuilder;
import org.elasticsearch.action.explain.ExplainResponse;
import org.elasticsearch.action.mlt.MoreLikeThisRequest;
import org.elasticsearch.action.mlt.MoreLikeThisRequestBuilder;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchRequestBuilder;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.action.search.SearchScrollRequestBuilder;

/**
 * A client provides a one stop interface for performing actions/operations against the cluster.
 * <p/>
 * <p>All operations performed are asynchronous by nature. Each action/operation has two flavors, the first
 * simply returns an {@link org.elasticsearch.action.ActionFuture}, while the second accepts an
 * {@link org.elasticsearch.action.ActionListener}.
 */
public interface SearchClient extends GenericClient {

    /**
     * Executes a generic action, denoted by an {@link Action}.
     *
     * @param action           The action type to execute.
     * @param request          The action request.
     * @param <Request>        Teh request type.
     * @param <Response>       the response type.
     * @param <RequestBuilder> The request builder type.
     * @return A future allowing to get back the response.
     */
    <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder>, SearchClient extends GenericClient> 
            ActionFuture<Response> execute(final Action<Request, Response, RequestBuilder, SearchClient> action, final Request request);

    /**
     * Executes a generic action, denoted by an {@link Action}.
     *
     * @param action           The action type to execute.
     * @param request          Teh action request.
     * @param listener         The listener to receive the response back.
     * @param <Request>        The request type.
     * @param <Response>       The response type.
     * @param <RequestBuilder> The request builder type.
     */
    <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder>, SearchClient extends GenericClient> 
            void execute(final Action<Request, Response, RequestBuilder, SearchClient> action, final Request request, ActionListener<Response> listener);

    /**
     * Prepares a request builder to execute, specified by {@link Action}.
     *
     * @param action           The action type to execute.
     * @param <Request>        The request type.
     * @param <Response>       The response type.
     * @param <RequestBuilder> The request builder.
     * @return The request builder, that can, at a later stage, execute the request.
     */
    <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder>, SearchClient extends GenericClient> 
            RequestBuilder prepareExecute(final Action<Request, Response, RequestBuilder, SearchClient> action);

    /**
     * Deletes all documents from one or more indices based on a query.
     *
     * @param request The delete by query request
     * @return The result future
     * @see Requests#deleteByQueryRequest(String...)
     */
    ActionFuture<DeleteByQueryResponse> deleteByQuery(DeleteByQueryRequest request);

    /**
     * Deletes all documents from one or more indices based on a query.
     *
     * @param request  The delete by query request
     * @param listener A listener to be notified with a result
     * @see Requests#deleteByQueryRequest(String...)
     */
    void deleteByQuery(DeleteByQueryRequest request, ActionListener<DeleteByQueryResponse> listener);

    /**
     * Deletes all documents from one or more indices based on a query.
     */
    DeleteByQueryRequestBuilder prepareDeleteByQuery(String... indices);

    /**
     * A count of all the documents matching a specific query.
     *
     * @param request The count request
     * @return The result future
     * @see Requests#countRequest(String...)
     */
    ActionFuture<CountResponse> count(CountRequest request);

    /**
     * A count of all the documents matching a specific query.
     *
     * @param request  The count request
     * @param listener A listener to be notified of the result
     * @see Requests#countRequest(String...)
     */
    void count(CountRequest request, ActionListener<CountResponse> listener);

    /**
     * A count of all the documents matching a specific query.
     */
    CountRequestBuilder prepareCount(String... indices);

    /**
     * Search across one or more indices and one or more types with a query.
     *
     * @param request The search request
     * @return The result future
     * @see Requests#searchRequest(String...)
     */
    ActionFuture<SearchResponse> search(SearchRequest request);

    /**
     * Search across one or more indices and one or more types with a query.
     *
     * @param request  The search request
     * @param listener A listener to be notified of the result
     * @see Requests#searchRequest(String...)
     */
    void search(SearchRequest request, ActionListener<SearchResponse> listener);

    /**
     * Search across one or more indices and one or more types with a query.
     */
    SearchRequestBuilder prepareSearch(String... indices);

    /**
     * A search scroll request to continue searching a previous scrollable search request.
     *
     * @param request The search scroll request
     * @return The result future
     * @see Requests#searchScrollRequest(String)
     */
    ActionFuture<SearchResponse> searchScroll(SearchScrollRequest request);

    /**
     * A search scroll request to continue searching a previous scrollable search request.
     *
     * @param request  The search scroll request
     * @param listener A listener to be notified of the result
     * @see Requests#searchScrollRequest(String)
     */
    void searchScroll(SearchScrollRequest request, ActionListener<SearchResponse> listener);

    /**
     * A search scroll request to continue searching a previous scrollable search request.
     */
    SearchScrollRequestBuilder prepareSearchScroll(String scrollId);

    /**
     * Performs multiple search requests.
     */
    ActionFuture<MultiSearchResponse> multiSearch(MultiSearchRequest request);

    /**
     * Performs multiple search requests.
     */
    void multiSearch(MultiSearchRequest request, ActionListener<MultiSearchResponse> listener);

    /**
     * Performs multiple search requests.
     */
    MultiSearchRequestBuilder prepareMultiSearch();

    /**
     * A more like this action to search for documents that are "like" a specific document.
     *
     * @param request The more like this request
     * @return The response future
     */
    ActionFuture<SearchResponse> moreLikeThis(MoreLikeThisRequest request);

    /**
     * A more like this action to search for documents that are "like" a specific document.
     *
     * @param request  The more like this request
     * @param listener A listener to be notified of the result
     */
    void moreLikeThis(MoreLikeThisRequest request, ActionListener<SearchResponse> listener);

    /**
     * A more like this action to search for documents that are "like" a specific document.
     *
     * @param index The index to load the document from
     * @param type  The type of the document
     * @param id    The id of the document
     */
    MoreLikeThisRequestBuilder prepareMoreLikeThis(String index, String type, String id);

    /**
     * Computes a score explanation for the specified request.
     *
     * @param index The index this explain is targeted for
     * @param type The type this explain is targeted for
     * @param id The document identifier this explain is targeted for
     */
    ExplainRequestBuilder prepareExplain(String index, String type, String id);

    /**
     * Computes a score explanation for the specified request.
     *
     * @param request The request encapsulating the query and document identifier to compute a score explanation for
     */
    ActionFuture<ExplainResponse> explain(ExplainRequest request);

    /**
     * Computes a score explanation for the specified request.
     *
     * @param request The request encapsulating the query and document identifier to compute a score explanation for
     * @param listener  A listener to be notified of the result
     */
    void explain(ExplainRequest request, ActionListener<ExplainResponse> listener);

}