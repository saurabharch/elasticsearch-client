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

package org.elasticsearch.action.support;

import java.io.IOException;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.common.bytes.BytesReference;

import java.util.List;
import java.util.Map;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;

public class HttpResponse extends ActionResponse {
    
    private final int statuscode;
    private final String contentType;
    private final Map<String, List<String>> headers;
    private final BytesReference body;
    
    public HttpResponse(int statuscode, String contentType, Map<String, List<String>> headers, BytesReference body) {
        this.statuscode = statuscode;
        this.contentType = contentType;
        this.headers = headers;
        this.body = body;
    }
    
    public int getStatusCode() {
        return statuscode;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public Map<String, List<String>> getHeader() {
        return headers;
    }
    
    public BytesReference getBody() {
        return body;
    }
    
    public XContentParser parser() throws IOException {
        return XContentFactory.xContent(XContentFactory.xContentType(body)).createParser(body.streamInput());
    }
    
    @Override
    public String toString() {
        return "[status="+statuscode+",contentType="+contentType+",body="+body.toUtf8()+"]";
    }
    
}
