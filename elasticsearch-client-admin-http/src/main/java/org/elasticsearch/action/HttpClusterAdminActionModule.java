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
//import org.elasticsearch.action.admin.cluster.HttpClusterAction;
import org.elasticsearch.action.admin.cluster.ClusterAction;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthAction;
import org.elasticsearch.action.admin.cluster.node.hotthreads.NodesHotThreadsAction;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoAction;
import org.elasticsearch.action.admin.cluster.node.restart.NodesRestartAction;
import org.elasticsearch.action.admin.cluster.node.shutdown.NodesShutdownAction;
import org.elasticsearch.action.admin.cluster.node.stats.NodesStatsAction;
import org.elasticsearch.action.admin.cluster.reroute.ClusterRerouteAction;
import org.elasticsearch.action.admin.cluster.settings.ClusterGetSettingsAction;
import org.elasticsearch.action.admin.cluster.settings.ClusterUpdateSettingsAction;
import org.elasticsearch.action.admin.cluster.state.ClusterStateAction;
import org.elasticsearch.action.support.HttpAction;
import org.elasticsearch.http.action.admin.cluster.health.HttpClusterHealthAction;
import org.elasticsearch.http.action.admin.cluster.node.hotthreads.HttpNodesHotThreadsAction;
import org.elasticsearch.http.action.admin.cluster.node.info.HttpNodesInfoAction;
import org.elasticsearch.http.action.admin.cluster.node.restart.HttpNodesRestartAction;
import org.elasticsearch.http.action.admin.cluster.node.shutdown.HttpNodesShutdownAction;
import org.elasticsearch.http.action.admin.cluster.node.stats.HttpNodesStatsAction;
import org.elasticsearch.http.action.admin.cluster.reroute.HttpClusterRerouteAction;
import org.elasticsearch.http.action.admin.cluster.settings.HttpClusterGetSettingsAction;
import org.elasticsearch.http.action.admin.cluster.settings.HttpClusterUpdateSettingsAction;
import org.elasticsearch.http.action.admin.cluster.state.HttpClusterStateAction;

public class HttpClusterAdminActionModule {

    private final static Map<String, ActionEntry> actions = Maps.newHashMap();

    /**
     * Register our actions.
     */
    static {
        registerAction(ClusterHealthAction.INSTANCE, new HttpClusterHealthAction());
        registerAction(NodesHotThreadsAction.INSTANCE, new HttpNodesHotThreadsAction());
   /*     registerAction(NodesInfoAction.INSTANCE, new HttpNodesInfoAction());
        registerAction(NodesRestartAction.INSTANCE, new HttpNodesRestartAction());
        registerAction(NodesShutdownAction.INSTANCE, new HttpNodesShutdownAction());
        registerAction(NodesStatsAction.INSTANCE, new HttpNodesStatsAction());
        registerAction(ClusterRerouteAction.INSTANCE, new HttpClusterRerouteAction());
        registerAction(ClusterGetSettingsAction.INSTANCE, new HttpClusterGetSettingsAction());
        registerAction(ClusterUpdateSettingsAction.INSTANCE, new HttpClusterUpdateSettingsAction());
        registerAction(ClusterStateAction.INSTANCE, new HttpClusterStateAction());*/
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
            void registerAction(ClusterAction<Request, Response, RequestBuilder> action, HttpAction<Request, Response> httpAction, Class... supportHttpActions) {
        actions.put(action.name(), new ActionEntry(action, httpAction, supportHttpActions));
    }

    public <Request extends ActionRequest, Response extends ActionResponse> 
            HttpAction<Request, Response> getAction(String action) {
        return actions.get(action).httpAction;
    }

    static class ActionEntry<Request extends ActionRequest, Response extends ActionResponse, RequestBuilder extends ActionRequestBuilder<Request, Response, RequestBuilder>> {

        public final ClusterAction<Request, Response, RequestBuilder> action;
        public final HttpAction<Request, Response> httpAction;
        public final Class[] supportHttpActions;

        ActionEntry(ClusterAction<Request, Response, RequestBuilder> action, HttpAction<Request, Response> httpAction, Class... supportHttpActions) {
            this.action = action;
            this.httpAction = httpAction;
            this.supportHttpActions = supportHttpActions;
        }
    }
}
