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
package org.elasticsearch.action;

import com.google.common.collect.Maps;
import java.util.Map;
import org.elasticsearch.action.admin.indices.IndicesAction;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesAction;
import org.elasticsearch.action.admin.indices.alias.IndicesGetAliasesAction;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeAction;
import org.elasticsearch.action.admin.indices.cache.clear.ClearIndicesCacheAction;
import org.elasticsearch.action.admin.indices.close.CloseIndexAction;
import org.elasticsearch.action.admin.indices.create.CreateIndexAction;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexAction;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsAction;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsAction;
import org.elasticsearch.action.admin.indices.flush.FlushAction;
import org.elasticsearch.action.admin.indices.gateway.snapshot.GatewaySnapshotAction;
import org.elasticsearch.action.admin.indices.mapping.delete.DeleteMappingAction;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingAction;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingAction;
import org.elasticsearch.action.admin.indices.open.OpenIndexAction;
import org.elasticsearch.action.admin.indices.optimize.OptimizeAction;
import org.elasticsearch.action.admin.indices.refresh.RefreshAction;
import org.elasticsearch.action.admin.indices.segments.IndicesSegmentsAction;
import org.elasticsearch.action.admin.indices.settings.GetSettingsAction;
import org.elasticsearch.action.admin.indices.settings.UpdateSettingsAction;
import org.elasticsearch.action.admin.indices.stats.IndicesStatsAction;
import org.elasticsearch.action.admin.indices.status.IndicesStatusAction;
import org.elasticsearch.action.admin.indices.template.delete.DeleteIndexTemplateAction;
import org.elasticsearch.action.admin.indices.template.get.GetIndexTemplateAction;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateAction;
import org.elasticsearch.action.admin.indices.validate.query.ValidateQueryAction;
import org.elasticsearch.action.admin.indices.warmer.delete.DeleteWarmerAction;
import org.elasticsearch.action.admin.indices.warmer.get.GetWarmerAction;
import org.elasticsearch.action.admin.indices.warmer.put.PutWarmerAction;
import org.elasticsearch.action.support.HttpAction;
import org.elasticsearch.http.action.admin.indices.alias.HttpIndicesAliasesAction;
import org.elasticsearch.http.action.admin.indices.alias.HttpIndicesGetAliasesAction;
import org.elasticsearch.http.action.admin.indices.analyze.HttpAnalyzeAction;
import org.elasticsearch.http.action.admin.indices.cache.clear.HttpClearIndicesCacheAction;
import org.elasticsearch.http.action.admin.indices.close.HttpCloseIndexAction;
import org.elasticsearch.http.action.admin.indices.create.HttpCreateIndexAction;
import org.elasticsearch.http.action.admin.indices.delete.HttpDeleteIndexAction;
import org.elasticsearch.http.action.admin.indices.exists.indices.HttpIndicesExistsAction;
import org.elasticsearch.http.action.admin.indices.exists.types.HttpTypesExistsAction;
import org.elasticsearch.http.action.admin.indices.flush.HttpFlushAction;
import org.elasticsearch.http.action.admin.indices.gateway.snapshot.HttpGatewaySnapshotAction;
import org.elasticsearch.http.action.admin.indices.mapping.delete.HttpDeleteMappingAction;
import org.elasticsearch.http.action.admin.indices.mapping.get.HttpGetMappingAction;
import org.elasticsearch.http.action.admin.indices.mapping.put.HttpPutMappingAction;
import org.elasticsearch.http.action.admin.indices.open.HttpOpenIndexAction;
import org.elasticsearch.http.action.admin.indices.optimize.HttpOptimizeAction;
import org.elasticsearch.http.action.admin.indices.refresh.HttpRefreshAction;
import org.elasticsearch.http.action.admin.indices.segments.HttpIndicesSegmentsAction;
import org.elasticsearch.http.action.admin.indices.settings.HttpGetSettingsAction;
import org.elasticsearch.http.action.admin.indices.settings.HttpUpdateSettingsAction;
import org.elasticsearch.http.action.admin.indices.stats.HttpIndicesStatsAction;
import org.elasticsearch.http.action.admin.indices.status.HttpIndicesStatusAction;
import org.elasticsearch.http.action.admin.indices.template.delete.HttpDeleteIndexTemplateAction;
import org.elasticsearch.http.action.admin.indices.template.get.HttpGetIndexTemplateAction;
import org.elasticsearch.http.action.admin.indices.template.put.HttpPutIndexTemplateAction;
import org.elasticsearch.http.action.admin.indices.validate.query.HttpValidateQueryAction;
import org.elasticsearch.http.action.admin.indices.warmer.delete.HttpDeleteWarmerAction;
import org.elasticsearch.http.action.admin.indices.warmer.get.HttpGetWarmerAction;
import org.elasticsearch.http.action.admin.indices.warmer.put.HttpPutWarmerAction;

public class HttpIndicesAdminActionModule {

    private final static Map<String, ActionEntry> actions = Maps.newHashMap();

    /**
     * Register our actions.
     */
    static {
        registerAction(IndicesAliasesAction.INSTANCE, new HttpIndicesAliasesAction());
        registerAction(IndicesGetAliasesAction.INSTANCE, new HttpIndicesGetAliasesAction());
        registerAction(AnalyzeAction.INSTANCE, new HttpAnalyzeAction());
        registerAction(ClearIndicesCacheAction.INSTANCE, new HttpClearIndicesCacheAction());
        registerAction(CloseIndexAction.INSTANCE, new HttpCloseIndexAction());
        registerAction(CreateIndexAction.INSTANCE, new HttpCreateIndexAction());
        registerAction(DeleteIndexAction.INSTANCE, new HttpDeleteIndexAction());
        registerAction(IndicesExistsAction.INSTANCE, new HttpIndicesExistsAction());
        registerAction(TypesExistsAction.INSTANCE, new HttpTypesExistsAction());
        registerAction(FlushAction.INSTANCE, new HttpFlushAction());
        registerAction(GatewaySnapshotAction.INSTANCE, new HttpGatewaySnapshotAction());
        registerAction(DeleteMappingAction.INSTANCE, new HttpDeleteMappingAction());
        registerAction(GetMappingAction.INSTANCE, new HttpGetMappingAction());
        registerAction(PutMappingAction.INSTANCE, new HttpPutMappingAction());
        registerAction(OpenIndexAction.INSTANCE, new HttpOpenIndexAction());
        registerAction(OptimizeAction.INSTANCE, new HttpOptimizeAction());
        registerAction(RefreshAction.INSTANCE, new HttpRefreshAction());
        registerAction(IndicesSegmentsAction.INSTANCE, new HttpIndicesSegmentsAction());
        registerAction(GetSettingsAction.INSTANCE, new HttpGetSettingsAction());
        registerAction(UpdateSettingsAction.INSTANCE, new HttpUpdateSettingsAction());
        registerAction(IndicesStatsAction.INSTANCE, new HttpIndicesStatsAction());
        registerAction(IndicesStatusAction.INSTANCE, new HttpIndicesStatusAction());
        registerAction(DeleteIndexTemplateAction.INSTANCE, new HttpDeleteIndexTemplateAction());
        registerAction(GetIndexTemplateAction.INSTANCE, new HttpGetIndexTemplateAction());
        registerAction(PutIndexTemplateAction.INSTANCE, new HttpPutIndexTemplateAction());
        registerAction(ValidateQueryAction.INSTANCE, new HttpValidateQueryAction());        
        registerAction(DeleteWarmerAction.INSTANCE, new HttpDeleteWarmerAction());
        registerAction(GetWarmerAction.INSTANCE, new HttpGetWarmerAction());
        registerAction(PutWarmerAction.INSTANCE, new HttpPutWarmerAction());         
    }

    /**
     * Registers an action.
     *
     * @param action The action type.
     * @param htppAction The HTTP action implementing the actual action.
     * @param supportHttpActions Any support actions that are needed by the HTTP
     * action.
     * @param <Request> The request type.
     * @param <Response> The response type.
     */
    private static <Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder>> 
            void registerAction(IndicesAction<Request, Response, RequestBuilder> action, HttpAction<Request, Response> httpAction, Class... supportHttpActions) {
        actions.put(action.name(), new ActionEntry(action, httpAction, supportHttpActions));
    }

    public <Request extends ActionRequest, Response extends ActionResponse> HttpAction<Request, Response> getAction(String action) {
        return actions.get(action).httpAction;
    }

    static class ActionEntry<Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder>> {

        public final IndicesAction<Request, Response, RequestBuilder> action;
        public final HttpAction<Request, Response> httpAction;
        public final Class[] supportHttpActions;

        ActionEntry(IndicesAction<Request, Response, RequestBuilder> action, HttpAction<Request, Response> httpAction, Class... supportHttpActions) {
            this.action = action;
            this.httpAction = httpAction;
            this.supportHttpActions = supportHttpActions;
        }
    }
}
