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

package org.elasticsearch.http.action.deletebyquery;

import java.io.IOException;
import java.util.Map;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.deletebyquery.DeleteByQueryRequest;
import org.elasticsearch.action.deletebyquery.DeleteByQueryResponse;
import org.elasticsearch.action.support.HttpAction;
import org.elasticsearch.action.support.HttpClient;
import org.elasticsearch.action.support.HttpRequest;
import org.elasticsearch.action.support.HttpResponse;
import org.elasticsearch.common.xcontent.XContentHelper;

public class HttpDeleteByQueryAction extends HttpAction<DeleteByQueryRequest, DeleteByQueryResponse> {
    
    public final static String NAME = "deletebyquery";
    private final static String METHOD = "DELETE";
    private final static String ENDPOINT = "_query";
    
    @Override
    protected void doExecute(HttpClient client, DeleteByQueryRequest request, ActionListener<DeleteByQueryResponse> listener) {
        HttpRequest httpRequest = new HttpRequest(METHOD, ENDPOINT)
                .param("routing", request.routing())
                .param("consistencylevel", request.consistencyLevel().name().toLowerCase())
                .param("replication", request.replicationType().name().toLowerCase())
                .body(request.querySource());
        submit(client, httpRequest, listener);           
    }

    @Override
    protected DeleteByQueryResponse toResponse(HttpResponse response) throws IOException {
         Map<String, Object> map = XContentHelper.convertToMap(response.getBody(), false).v2();
        logger.info("delete-by-query response = {}", map);
        return null;
    }
    
}
