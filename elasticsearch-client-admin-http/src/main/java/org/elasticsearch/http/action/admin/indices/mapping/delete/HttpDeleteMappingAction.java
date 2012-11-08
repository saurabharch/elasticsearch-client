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

package org.elasticsearch.http.action.admin.indices.mapping.delete;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.mapping.delete.DeleteMappingRequest;
import org.elasticsearch.action.admin.indices.mapping.delete.DeleteMappingResponse;
import org.elasticsearch.action.support.HttpAction;
import org.elasticsearch.action.support.HttpClient;
import org.elasticsearch.action.support.HttpRequest;
import org.elasticsearch.action.support.HttpResponse;
import org.elasticsearch.common.xcontent.XContentHelper;

import java.io.IOException;
import java.util.Map;
import org.elasticsearch.common.Strings;

public class HttpDeleteMappingAction extends HttpAction<DeleteMappingRequest, DeleteMappingResponse>{

    public static final String NAME = "mapping_delete";
    private static final String METHOD = "DELETE";
    private static final String ENDPOINT = "/_mapping";
    
    @Override
    protected void doExecute(HttpClient client, DeleteMappingRequest request, ActionListener<DeleteMappingResponse> listener) {
        HttpRequest httpRequest = new HttpRequest(METHOD, ENDPOINT)
                .index(Strings.arrayToCommaDelimitedString(request.indices()))
                .type(request.type());
        submit(client, httpRequest, listener);
    }

    @Override
    protected DeleteMappingResponse toResponse(HttpResponse response) throws IOException {
        Map<String, Object> map = XContentHelper.convertToMap(response.getBody(), false).v2();
        logger.info("response = {}", map);
        return null;
    }


}
