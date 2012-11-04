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

package org.elasticsearch.http.action.admin.cluster.node.info;

import java.io.IOException;
import java.util.Map;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoRequest;
import org.elasticsearch.action.admin.cluster.node.info.NodesInfoResponse;
import org.elasticsearch.action.support.HttpAction;
import org.elasticsearch.action.support.HttpRequest;
import org.elasticsearch.action.support.HttpResponse;
import org.elasticsearch.client.http.HttpClusterAdminClient;
import org.elasticsearch.common.xcontent.XContentHelper;

public class HttpNodesInfoAction extends HttpAction<HttpClusterAdminClient, NodesInfoRequest, NodesInfoResponse> {

    public static final String NAME = "nodes_info";
    private static final String METHOD = "GET";
    private static final String ENDPOINT = "/_nodes";

    @Override
    protected void doExecute(HttpClusterAdminClient client, NodesInfoRequest request, ActionListener<NodesInfoResponse> listener) {
        String endpoint = ENDPOINT;
        if (request.settings()) {
            endpoint += "/settings";
        } else if (request.jvm()) {
            endpoint += "/jvm";
        } else if (request.network()) {
            endpoint += "/network";
        } else if (request.process()) {
            endpoint += "/process";
        } else if (request.os()) {
            endpoint += "/os";
        } else if (request.threadPool()) {
            endpoint += "/thread_pool";
        } else if (request.transport()) {
            endpoint += "/transport";
        } else if (request.http()) {
            endpoint += "/http";
        }

        HttpRequest httpRequest = new HttpRequest(client.settings(), METHOD, endpoint)
                .param("nodeId", request.nodesIds());
        submit(client, httpRequest, listener);
    }

    @Override
    protected NodesInfoResponse toResponse(HttpResponse response) throws IOException {
        Map<String, Object> map = XContentHelper.convertToMap(response.getBody(), false).v2();
        logger.info("response = {}", map);
        return null;
    }
}
