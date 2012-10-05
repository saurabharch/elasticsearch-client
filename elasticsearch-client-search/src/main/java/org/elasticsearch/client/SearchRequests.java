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

import org.elasticsearch.action.count.CountRequest;
import org.elasticsearch.action.deletebyquery.DeleteByQueryRequest;
import org.elasticsearch.action.mlt.MoreLikeThisRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.common.xcontent.XContentType;

/**
 * A handy one stop shop for creating requests (make sure to import static this class).
 */
public class SearchRequests {

    /**
     * The content type used to generate request builders (query / search).
     */
    public static XContentType CONTENT_TYPE = XContentType.SMILE;

    /**
     * The default content type to use to generate source documents when indexing.
     */
    public static XContentType INDEX_CONTENT_TYPE = XContentType.JSON;

    /**
     * Creates a delete by query request. Note, the query itself must be set either by setting the JSON source
     * of the query, or by using a {@link org.elasticsearch.index.query.QueryBuilder} (using {@link org.elasticsearch.index.query.QueryBuilders}).
     *
     * @param indices The indices the delete by query against. Use <tt>null</tt> or <tt>_all</tt> to execute against all indices
     * @return The delete by query request
     * @see org.elasticsearch.client.Client#deleteByQuery(org.elasticsearch.action.deletebyquery.DeleteByQueryRequest)
     */
    public static DeleteByQueryRequest deleteByQueryRequest(String... indices) {
        return new DeleteByQueryRequest(indices);
    }

    /**
     * Creates a count request which counts the hits matched against a query. Note, the query itself must be set
     * either using the JSON source of the query, or using a {@link org.elasticsearch.index.query.QueryBuilder} (using {@link org.elasticsearch.index.query.QueryBuilders}).
     *
     * @param indices The indices to count matched documents against a query. Use <tt>null</tt> or <tt>_all</tt> to execute against all indices
     * @return The count request
     * @see org.elasticsearch.client.Client#count(org.elasticsearch.action.count.CountRequest)
     */
    public static CountRequest countRequest(String... indices) {
        return new CountRequest(indices);
    }

    /**
     * More like this request represents a request to search for documents that are "like" the provided (fetched)
     * document.
     *
     * @param index The index to load the document from
     * @return The more like this request
     * @see org.elasticsearch.client.Client#moreLikeThis(org.elasticsearch.action.mlt.MoreLikeThisRequest)
     */
    public static MoreLikeThisRequest moreLikeThisRequest(String index) {
        return new MoreLikeThisRequest(index);
    }

    /**
     * Creates a search request against one or more indices. Note, the search source must be set either using the
     * actual JSON search source, or the {@link org.elasticsearch.search.builder.SearchSourceBuilder}.
     *
     * @param indices The indices to search against. Use <tt>null</tt> or <tt>_all</tt> to execute against all indices
     * @return The search request
     * @see org.elasticsearch.client.Client#search(org.elasticsearch.action.search.SearchRequest)
     */
    public static SearchRequest searchRequest(String... indices) {
        return new SearchRequest(indices);
    }

    /**
     * Creates a search scroll request allowing to continue searching a previous search request.
     *
     * @param scrollId The scroll id representing the scrollable search
     * @return The search scroll request
     * @see org.elasticsearch.client.Client#searchScroll(org.elasticsearch.action.search.SearchScrollRequest)
     */
    public static SearchScrollRequest searchScrollRequest(String scrollId) {
        return new SearchScrollRequest(scrollId);
    }

}
