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

import org.elasticsearch.action.*;
import org.elasticsearch.action.admin.indices.IndicesAction;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesAction;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequestBuilder;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesResponse;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeAction;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeRequest;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeRequestBuilder;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.admin.indices.cache.clear.ClearIndicesCacheAction;
import org.elasticsearch.action.admin.indices.cache.clear.ClearIndicesCacheRequest;
import org.elasticsearch.action.admin.indices.cache.clear.ClearIndicesCacheRequestBuilder;
import org.elasticsearch.action.admin.indices.cache.clear.ClearIndicesCacheResponse;
import org.elasticsearch.action.admin.indices.close.CloseIndexAction;
import org.elasticsearch.action.admin.indices.close.CloseIndexRequest;
import org.elasticsearch.action.admin.indices.close.CloseIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.close.CloseIndexResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexAction;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexAction;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsAction;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequestBuilder;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsAction;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsRequestBuilder;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsResponse;
import org.elasticsearch.action.admin.indices.flush.FlushAction;
import org.elasticsearch.action.admin.indices.flush.FlushRequest;
import org.elasticsearch.action.admin.indices.flush.FlushRequestBuilder;
import org.elasticsearch.action.admin.indices.flush.FlushResponse;
import org.elasticsearch.action.admin.indices.gateway.snapshot.GatewaySnapshotAction;
import org.elasticsearch.action.admin.indices.gateway.snapshot.GatewaySnapshotRequest;
import org.elasticsearch.action.admin.indices.gateway.snapshot.GatewaySnapshotRequestBuilder;
import org.elasticsearch.action.admin.indices.gateway.snapshot.GatewaySnapshotResponse;
import org.elasticsearch.action.admin.indices.mapping.delete.DeleteMappingAction;
import org.elasticsearch.action.admin.indices.mapping.delete.DeleteMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.delete.DeleteMappingRequestBuilder;
import org.elasticsearch.action.admin.indices.mapping.delete.DeleteMappingResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingAction;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingRequestBuilder;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingAction;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequestBuilder;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingResponse;
import org.elasticsearch.action.admin.indices.open.OpenIndexAction;
import org.elasticsearch.action.admin.indices.open.OpenIndexRequest;
import org.elasticsearch.action.admin.indices.open.OpenIndexRequestBuilder;
import org.elasticsearch.action.admin.indices.open.OpenIndexResponse;
import org.elasticsearch.action.admin.indices.optimize.OptimizeAction;
import org.elasticsearch.action.admin.indices.optimize.OptimizeRequest;
import org.elasticsearch.action.admin.indices.optimize.OptimizeRequestBuilder;
import org.elasticsearch.action.admin.indices.optimize.OptimizeResponse;
import org.elasticsearch.action.admin.indices.refresh.RefreshAction;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequestBuilder;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.action.admin.indices.segments.IndicesSegmentResponse;
import org.elasticsearch.action.admin.indices.segments.IndicesSegmentsAction;
import org.elasticsearch.action.admin.indices.segments.IndicesSegmentsRequest;
import org.elasticsearch.action.admin.indices.segments.IndicesSegmentsRequestBuilder;
import org.elasticsearch.action.admin.indices.settings.GetSettingsAction;
import org.elasticsearch.action.admin.indices.settings.GetSettingsRequest;
import org.elasticsearch.action.admin.indices.settings.GetSettingsRequestBuilder;
import org.elasticsearch.action.admin.indices.settings.GetSettingsResponse;
import org.elasticsearch.action.admin.indices.settings.UpdateSettingsAction;
import org.elasticsearch.action.admin.indices.settings.UpdateSettingsRequest;
import org.elasticsearch.action.admin.indices.settings.UpdateSettingsRequestBuilder;
import org.elasticsearch.action.admin.indices.settings.UpdateSettingsResponse;
import org.elasticsearch.action.admin.indices.stats.IndicesStats;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsAction;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsRequest;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsRequestBuilder;
import org.elasticsearch.action.admin.indices.status.IndicesStatusAction;
import org.elasticsearch.action.admin.indices.status.IndicesStatusRequest;
import org.elasticsearch.action.admin.indices.status.IndicesStatusRequestBuilder;
import org.elasticsearch.action.admin.indices.status.IndicesStatusResponse;
import org.elasticsearch.action.admin.indices.template.delete.DeleteIndexTemplateAction;
import org.elasticsearch.action.admin.indices.template.delete.DeleteIndexTemplateRequest;
import org.elasticsearch.action.admin.indices.template.delete.DeleteIndexTemplateRequestBuilder;
import org.elasticsearch.action.admin.indices.template.delete.DeleteIndexTemplateResponse;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateAction;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequest;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequestBuilder;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateResponse;
import org.elasticsearch.action.admin.indices.validate.query.ValidateQueryAction;
import org.elasticsearch.action.admin.indices.validate.query.ValidateQueryRequest;
import org.elasticsearch.action.admin.indices.validate.query.ValidateQueryRequestBuilder;
import org.elasticsearch.action.admin.indices.validate.query.ValidateQueryResponse;
import org.elasticsearch.action.admin.indices.warmer.delete.DeleteWarmerAction;
import org.elasticsearch.action.admin.indices.warmer.delete.DeleteWarmerRequest;
import org.elasticsearch.action.admin.indices.warmer.delete.DeleteWarmerRequestBuilder;
import org.elasticsearch.action.admin.indices.warmer.delete.DeleteWarmerResponse;
import org.elasticsearch.action.admin.indices.warmer.put.PutWarmerAction;
import org.elasticsearch.action.admin.indices.warmer.put.PutWarmerRequest;
import org.elasticsearch.action.admin.indices.warmer.put.PutWarmerRequestBuilder;
import org.elasticsearch.action.admin.indices.warmer.put.PutWarmerResponse;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.common.Nullable;

/**
 *
 */
public abstract class AbstractIndicesAdminClient implements IndicesAdminClient {

    public <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder>> RequestBuilder prepareExecute(final IndicesAction<Request, Response, RequestBuilder> action) {
        return action.newRequestBuilder(this);
    }

    public ActionFuture<IndicesExistsResponse> exists(final IndicesExistsRequest request) {
        return execute(IndicesExistsAction.INSTANCE, request);
    }

    public void exists(final IndicesExistsRequest request, final ActionListener<IndicesExistsResponse> listener) {
        execute(IndicesExistsAction.INSTANCE, request, listener);
    }

    public IndicesExistsRequestBuilder prepareExists(String... indices) {
        return new IndicesExistsRequestBuilder(this, indices);
    }

    public ActionFuture<TypesExistsResponse> typesExists(TypesExistsRequest request) {
        return execute(TypesExistsAction.INSTANCE, request);
    }

    public void typesExists(TypesExistsRequest request, ActionListener<TypesExistsResponse> listener) {
        execute(TypesExistsAction.INSTANCE, request, listener);
    }

    public TypesExistsRequestBuilder prepareTypesExists(String... index) {
        return new TypesExistsRequestBuilder(this, index);
    }

    public ActionFuture<IndicesAliasesResponse> aliases(final IndicesAliasesRequest request) {
        return execute(IndicesAliasesAction.INSTANCE, request);
    }

    public void aliases(final IndicesAliasesRequest request, final ActionListener<IndicesAliasesResponse> listener) {
        execute(IndicesAliasesAction.INSTANCE, request, listener);
    }

    public IndicesAliasesRequestBuilder prepareAliases() {
        return new IndicesAliasesRequestBuilder(this);
    }

    public ActionFuture<ClearIndicesCacheResponse> clearCache(final ClearIndicesCacheRequest request) {
        return execute(ClearIndicesCacheAction.INSTANCE, request);
    }

    public void clearCache(final ClearIndicesCacheRequest request, final ActionListener<ClearIndicesCacheResponse> listener) {
        execute(ClearIndicesCacheAction.INSTANCE, request, listener);
    }

    public ClearIndicesCacheRequestBuilder prepareClearCache(String... indices) {
        return new ClearIndicesCacheRequestBuilder(this).setIndices(indices);
    }

    public ActionFuture<CreateIndexResponse> create(final CreateIndexRequest request) {
        return execute(CreateIndexAction.INSTANCE, request);
    }

    public void create(final CreateIndexRequest request, final ActionListener<CreateIndexResponse> listener) {
        execute(CreateIndexAction.INSTANCE, request, listener);
    }

    public CreateIndexRequestBuilder prepareCreate(String index) {
        return new CreateIndexRequestBuilder(this, index);
    }

    public ActionFuture<DeleteIndexResponse> delete(final DeleteIndexRequest request) {
        return execute(DeleteIndexAction.INSTANCE, request);
    }

    public void delete(final DeleteIndexRequest request, final ActionListener<DeleteIndexResponse> listener) {
        execute(DeleteIndexAction.INSTANCE, request, listener);
    }

    public DeleteIndexRequestBuilder prepareDelete(String... indices) {
        return new DeleteIndexRequestBuilder(this, indices);
    }

    public ActionFuture<CloseIndexResponse> close(final CloseIndexRequest request) {
        return execute(CloseIndexAction.INSTANCE, request);
    }

    public void close(final CloseIndexRequest request, final ActionListener<CloseIndexResponse> listener) {
        execute(CloseIndexAction.INSTANCE, request, listener);
    }

    public CloseIndexRequestBuilder prepareClose(String index) {
        return new CloseIndexRequestBuilder(this, index);
    }

    public ActionFuture<OpenIndexResponse> open(final OpenIndexRequest request) {
        return execute(OpenIndexAction.INSTANCE, request);
    }

    public void open(final OpenIndexRequest request, final ActionListener<OpenIndexResponse> listener) {
        execute(OpenIndexAction.INSTANCE, request, listener);
    }

    public OpenIndexRequestBuilder prepareOpen(String index) {
        return new OpenIndexRequestBuilder(this, index);
    }

    public ActionFuture<FlushResponse> flush(final FlushRequest request) {
        return execute(FlushAction.INSTANCE, request);
    }

    public void flush(final FlushRequest request, final ActionListener<FlushResponse> listener) {
        execute(FlushAction.INSTANCE, request, listener);
    }

    public FlushRequestBuilder prepareFlush(String... indices) {
        return new FlushRequestBuilder(this).setIndices(indices);
    }

    public ActionFuture<GatewaySnapshotResponse> gatewaySnapshot(final GatewaySnapshotRequest request) {
        return execute(GatewaySnapshotAction.INSTANCE, request);
    }

    public void gatewaySnapshot(final GatewaySnapshotRequest request, final ActionListener<GatewaySnapshotResponse> listener) {
        execute(GatewaySnapshotAction.INSTANCE, request, listener);
    }

    public GatewaySnapshotRequestBuilder prepareGatewaySnapshot(String... indices) {
        return new GatewaySnapshotRequestBuilder(this).setIndices(indices);
    }

    public ActionFuture<GetMappingResponse> getMapping(final GetMappingRequest request) {
        return execute(GetMappingAction.INSTANCE, request);
    }

    public void getMapping(final GetMappingRequest request, final ActionListener<GetMappingResponse> listener) {
        execute(GetMappingAction.INSTANCE, request, listener);
    }

    public GetMappingRequestBuilder prepareGetMapping(String... indices) {
        return new GetMappingRequestBuilder(this).setIndices(indices);
    }

    public ActionFuture<PutMappingResponse> putMapping(final PutMappingRequest request) {
        return execute(PutMappingAction.INSTANCE, request);
    }

    public void putMapping(final PutMappingRequest request, final ActionListener<PutMappingResponse> listener) {
        execute(PutMappingAction.INSTANCE, request, listener);
    }

    public PutMappingRequestBuilder preparePutMapping(String... indices) {
        return new PutMappingRequestBuilder(this).setIndices(indices);
    }

    public ActionFuture<DeleteMappingResponse> deleteMapping(final DeleteMappingRequest request) {
        return execute(DeleteMappingAction.INSTANCE, request);
    }

    public void deleteMapping(final DeleteMappingRequest request, final ActionListener<DeleteMappingResponse> listener) {
        execute(DeleteMappingAction.INSTANCE, request, listener);
    }

    public DeleteMappingRequestBuilder prepareDeleteMapping(String... indices) {
        return new DeleteMappingRequestBuilder(this).setIndices(indices);
    }

    public ActionFuture<OptimizeResponse> optimize(final OptimizeRequest request) {
        return execute(OptimizeAction.INSTANCE, request);
    }

    public void optimize(final OptimizeRequest request, final ActionListener<OptimizeResponse> listener) {
        execute(OptimizeAction.INSTANCE, request, listener);
    }

    public OptimizeRequestBuilder prepareOptimize(String... indices) {
        return new OptimizeRequestBuilder(this).setIndices(indices);
    }

    public ActionFuture<RefreshResponse> refresh(final RefreshRequest request) {
        return execute(RefreshAction.INSTANCE, request);
    }

    public void refresh(final RefreshRequest request, final ActionListener<RefreshResponse> listener) {
        execute(RefreshAction.INSTANCE, request, listener);
    }

    public RefreshRequestBuilder prepareRefresh(String... indices) {
        return new RefreshRequestBuilder(this).setIndices(indices);
    }

    public ActionFuture<IndicesStats> stats(final IndicesStatsRequest request) {
        return execute(IndicesStatsAction.INSTANCE, request);
    }

    public void stats(final IndicesStatsRequest request, final ActionListener<IndicesStats> listener) {
        execute(IndicesStatsAction.INSTANCE, request, listener);
    }

    public IndicesStatsRequestBuilder prepareStats(String... indices) {
        return new IndicesStatsRequestBuilder(this).setIndices(indices);
    }

    public ActionFuture<IndicesStatusResponse> status(final IndicesStatusRequest request) {
        return execute(IndicesStatusAction.INSTANCE, request);
    }

    public void status(final IndicesStatusRequest request, final ActionListener<IndicesStatusResponse> listener) {
        execute(IndicesStatusAction.INSTANCE, request, listener);
    }

    public IndicesStatusRequestBuilder prepareStatus(String... indices) {
        return new IndicesStatusRequestBuilder(this).setIndices(indices);
    }

    public ActionFuture<IndicesSegmentResponse> segments(final IndicesSegmentsRequest request) {
        return execute(IndicesSegmentsAction.INSTANCE, request);
    }

    public void segments(final IndicesSegmentsRequest request, final ActionListener<IndicesSegmentResponse> listener) {
        execute(IndicesSegmentsAction.INSTANCE, request, listener);
    }

    public IndicesSegmentsRequestBuilder prepareSegments(String... indices) {
        return new IndicesSegmentsRequestBuilder(this).setIndices(indices);
    }

    public ActionFuture<GetSettingsResponse> getSettings(final GetSettingsRequest request) {
        return execute(GetSettingsAction.INSTANCE, request);
    }

    public void getSettings(final GetSettingsRequest request, final ActionListener<GetSettingsResponse> listener) {
        execute(GetSettingsAction.INSTANCE, request, listener);
    }

    public GetSettingsRequestBuilder prepareGetSettings(String... indices) {
        return new GetSettingsRequestBuilder(this).setIndices(indices);
    }

    public ActionFuture<UpdateSettingsResponse> updateSettings(final UpdateSettingsRequest request) {
        return execute(UpdateSettingsAction.INSTANCE, request);
    }

    public void updateSettings(final UpdateSettingsRequest request, final ActionListener<UpdateSettingsResponse> listener) {
        execute(UpdateSettingsAction.INSTANCE, request, listener);
    }

    public UpdateSettingsRequestBuilder prepareUpdateSettings(String... indices) {
        return new UpdateSettingsRequestBuilder(this).setIndices(indices);
    }

    public ActionFuture<AnalyzeResponse> analyze(final AnalyzeRequest request) {
        return execute(AnalyzeAction.INSTANCE, request);
    }

    public void analyze(final AnalyzeRequest request, final ActionListener<AnalyzeResponse> listener) {
        execute(AnalyzeAction.INSTANCE, request, listener);
    }

    public AnalyzeRequestBuilder prepareAnalyze(@Nullable String index, String text) {
        return new AnalyzeRequestBuilder(this, index, text);
    }

    public AnalyzeRequestBuilder prepareAnalyze(String text) {
        return new AnalyzeRequestBuilder(this, null, text);
    }

    public ActionFuture<PutIndexTemplateResponse> putTemplate(final PutIndexTemplateRequest request) {
        return execute(PutIndexTemplateAction.INSTANCE, request);
    }

    public void putTemplate(final PutIndexTemplateRequest request, final ActionListener<PutIndexTemplateResponse> listener) {
        execute(PutIndexTemplateAction.INSTANCE, request, listener);
    }

    public PutIndexTemplateRequestBuilder preparePutTemplate(String name) {
        return new PutIndexTemplateRequestBuilder(this, name);
    }

    public ActionFuture<DeleteIndexTemplateResponse> deleteTemplate(final DeleteIndexTemplateRequest request) {
        return execute(DeleteIndexTemplateAction.INSTANCE, request);
    }

    public void deleteTemplate(final DeleteIndexTemplateRequest request, final ActionListener<DeleteIndexTemplateResponse> listener) {
        execute(DeleteIndexTemplateAction.INSTANCE, request, listener);
    }

    public DeleteIndexTemplateRequestBuilder prepareDeleteTemplate(String name) {
        return new DeleteIndexTemplateRequestBuilder(this, name);
    }

    public ActionFuture<ValidateQueryResponse> validateQuery(final ValidateQueryRequest request) {
        return execute(ValidateQueryAction.INSTANCE, request);
    }

    public void validateQuery(final ValidateQueryRequest request, final ActionListener<ValidateQueryResponse> listener) {
        execute(ValidateQueryAction.INSTANCE, request, listener);
    }

    public ValidateQueryRequestBuilder prepareValidateQuery(String... indices) {
        return new ValidateQueryRequestBuilder(this).setIndices(indices);
    }

    public ActionFuture<PutWarmerResponse> putWarmer(PutWarmerRequest request) {
        return execute(PutWarmerAction.INSTANCE, request);
    }

    public void putWarmer(PutWarmerRequest request, ActionListener<PutWarmerResponse> listener) {
        execute(PutWarmerAction.INSTANCE, request, listener);
    }

    public PutWarmerRequestBuilder preparePutWarmer(String name) {
        return new PutWarmerRequestBuilder(this, name);
    }

    public ActionFuture<DeleteWarmerResponse> deleteWarmer(DeleteWarmerRequest request) {
        return execute(DeleteWarmerAction.INSTANCE, request);
    }

    public void deleteWarmer(DeleteWarmerRequest request, ActionListener<DeleteWarmerResponse> listener) {
        execute(DeleteWarmerAction.INSTANCE, request, listener);
    }

    public DeleteWarmerRequestBuilder prepareDeleteWarmer() {
        return new DeleteWarmerRequestBuilder(this);
    }
}
