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
import org.elasticsearch.action.bulk.BulkAction;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteAction;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteRequestBuilder;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetAction;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.get.MultiGetAction;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetRequestBuilder;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexAction;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.percolate.PercolateAction;
import org.elasticsearch.action.percolate.PercolateRequest;
import org.elasticsearch.action.percolate.PercolateRequestBuilder;
import org.elasticsearch.action.percolate.PercolateResponse;
import org.elasticsearch.action.update.UpdateAction;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.GenericClient;
import org.elasticsearch.client.IngestClient;
import org.elasticsearch.common.Nullable;

public abstract class AbstractIngestClient implements IngestClient {
    
    
    public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder>, IngestClient extends GenericClient> 
            RequestBuilder prepareExecute(final Action<Request, Response, RequestBuilder, IngestClient> action) {
        return action.newRequestBuilder((IngestClient)this);
    }

    
    public ActionFuture<IndexResponse> index(final IndexRequest request) {
        return execute(IndexAction.INSTANCE, request);
    }

    
    public void index(final IndexRequest request, final ActionListener<IndexResponse> listener) {
        execute(IndexAction.INSTANCE, request, listener);
    }

    
    public IndexRequestBuilder prepareIndex() {
        return new IndexRequestBuilder(this, null);
    }

    
    public IndexRequestBuilder prepareIndex(String index, String type) {
        return prepareIndex(index, type, null);
    }

    
    public IndexRequestBuilder prepareIndex(String index, String type, @Nullable String id) {
        return prepareIndex().setIndex(index).setType(type).setId(id);
    }
    
    
    public ActionFuture<UpdateResponse> update(final UpdateRequest request) {
        return execute(UpdateAction.INSTANCE, request);
    }

    
    public void update(final UpdateRequest request, final ActionListener<UpdateResponse> listener) {
        execute(UpdateAction.INSTANCE, request, listener);
    }

    
    public UpdateRequestBuilder prepareUpdate() {
        return new UpdateRequestBuilder(this, null, null, null);
    }

    
    public UpdateRequestBuilder prepareUpdate(String index, String type, String id) {
        return new UpdateRequestBuilder(this, index, type, id);
    }

    
    public ActionFuture<DeleteResponse> delete(final DeleteRequest request) {
        return execute(DeleteAction.INSTANCE, request);
    }

    
    public void delete(final DeleteRequest request, final ActionListener<DeleteResponse> listener) {
        execute(DeleteAction.INSTANCE, request, listener);
    }

    
    public DeleteRequestBuilder prepareDelete() {
        return new DeleteRequestBuilder(this, null);
    }

    
    public DeleteRequestBuilder prepareDelete(String index, String type, String id) {
        return prepareDelete().setIndex(index).setType(type).setId(id);
    }

    
    public ActionFuture<BulkResponse> bulk(final BulkRequest request) {
        return execute(BulkAction.INSTANCE, request);
    }

    
    public void bulk(final BulkRequest request, final ActionListener<BulkResponse> listener) {
        execute(BulkAction.INSTANCE, request, listener);
    }

    
    public BulkRequestBuilder prepareBulk() {
        return new BulkRequestBuilder(this);
    }

    
    public ActionFuture<GetResponse> get(final GetRequest request) {
        return execute(GetAction.INSTANCE, request);
    }

    
    public void get(final GetRequest request, final ActionListener<GetResponse> listener) {
        execute(GetAction.INSTANCE, request, listener);
    }

    
    public GetRequestBuilder prepareGet() {
        return new GetRequestBuilder(this, null);
    }

    
    public GetRequestBuilder prepareGet(String index, String type, String id) {
        return prepareGet().setIndex(index).setType(type).setId(id);
    }

    
    public ActionFuture<MultiGetResponse> multiGet(final MultiGetRequest request) {
        return execute(MultiGetAction.INSTANCE, request);
    }

    
    public void multiGet(final MultiGetRequest request, final ActionListener<MultiGetResponse> listener) {
        execute(MultiGetAction.INSTANCE, request, listener);
    }

    
    public MultiGetRequestBuilder prepareMultiGet() {
        return new MultiGetRequestBuilder(this);
    }

    
    public ActionFuture<PercolateResponse> percolate(final PercolateRequest request) {
        return execute(PercolateAction.INSTANCE, request);
    }

    
    public void percolate(final PercolateRequest request, final ActionListener<PercolateResponse> listener) {
        execute(PercolateAction.INSTANCE, request, listener);
    }

    
    public PercolateRequestBuilder preparePercolate(String index, String type) {
        return new PercolateRequestBuilder(this, index, type);
    }

}
