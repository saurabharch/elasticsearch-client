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

package org.elasticsearch.http.action.admin.cluster.health;

import java.io.IOException;
import java.util.Map;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.action.admin.cluster.HttpClusterAction;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.support.HttpAction;
import org.elasticsearch.action.support.HttpClient;
import org.elasticsearch.action.support.HttpRequest;
import org.elasticsearch.action.support.HttpResponse;
import org.elasticsearch.client.ClusterAdminClient;
import org.elasticsearch.common.xcontent.XContentHelper;


public class HttpClusterHealthAction extends HttpAction<ClusterHealthRequest, ClusterHealthResponse>{

    public static final String NAME = "cluster_health";
    
    private static final String METHOD = "GET";
    
    private static final String ENDPOINT = "_cluster/health";
    
    @Override
    protected void doExecute(HttpClient client, ClusterHealthRequest request, ActionListener<ClusterHealthResponse> listener) {
        HttpRequest httpRequest = new HttpRequest(METHOD, ENDPOINT)
                .index(request.indices())
                .param("master_timeout", request.masterNodeTimeout())
                .param("timeout", request.timeout())
                .param("wait_for_active_shards", request.waitForActiveShards())
                .param("wait_for_relocating_shards", request.waitForRelocatingShards())
                .param("wait_for_nodes", request.waitForNodes())
                ;
        if (request.waitForStatus() != null)
                httpRequest.param("wait_for_status", request.waitForStatus().name().toLowerCase());
        submit(client, httpRequest, listener);        
    }

    @Override
    protected ClusterHealthResponse toResponse(HttpResponse response) throws IOException {
        Map<String, Object> map = XContentHelper.convertToMap(response.getBody(), false).v2();
        logger.info("response = {}", map);
        return null;
    }
    
}
