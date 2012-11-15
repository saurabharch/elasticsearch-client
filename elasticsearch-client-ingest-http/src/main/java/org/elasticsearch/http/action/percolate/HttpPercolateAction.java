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

package org.elasticsearch.http.action.percolate;

import org.elasticsearch.action.percolate.PercolateRequest;
import org.elasticsearch.action.percolate.PercolateResponse;
import org.elasticsearch.action.support.HttpAction;
import org.elasticsearch.action.support.HttpRequest;
import org.elasticsearch.action.support.HttpResponse;
import org.elasticsearch.common.xcontent.XContentHelper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class HttpPercolateAction extends HttpAction<PercolateRequest, PercolateResponse> {

    public static final String NAME = "percolate";
    private static final String ENDPOINT = "_percolate";

    @Override
    protected HttpRequest toRequest(PercolateRequest request) {
        HttpRequest httpRequest = new HttpRequest(POST, ENDPOINT)
                .index(request.index())
                .type(request.type())
                .param("prefer_local", Boolean.toString(request.preferLocalShard()))
                .body(request.source());
        return httpRequest;
    }

    @Override
    protected PercolateResponse toResponse(HttpResponse response) throws IOException {
        PercolateResponse percolateResponse = null;
        Map<String, Object> map = XContentHelper.convertToMap(response.getBody(), false).v2();
        Object o = map.get("matches");
        if (o instanceof List) {
            percolateResponse = new PercolateResponse((List<String>)o);
        }
        return percolateResponse;
    }
}
