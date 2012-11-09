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

package org.elasticsearch.index.query;
/**
 * Created by IntelliJ IDEA.
 * User: cedric
 * Date: 12/07/11
 * Time: 11:30
 */

import org.elasticsearch.common.xcontent.XContentBuilder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * A Filter builder which allows building a filter thanks to a JSON string or binary data. This is useful when you want
 * to use the Java Builder API but still have JSON filter strings at hand that you want to combine with other
 * query builders.
 */
public class WrapperFilterBuilder extends BaseFilterBuilder {

    public static final String NAME = "wrapper";
    private final byte[] source;
    private final int offset;
    private final int length;

    public WrapperFilterBuilder(String source) {
        this.source = createSource(source);
        this.offset = 0;
        this.length = this.source.length;
    }
    
    private static byte[] createSource(String source) {
        try {
            return source.getBytes("UTF-8");
        } catch (UnsupportedEncodingException ex) {
            return null;
        }        
    } 

    public WrapperFilterBuilder(byte[] source, int offset, int length) {
        this.source = source;
        this.offset = offset;
        this.length = length;
    }

    @Override
    protected void doXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(NAME);
        builder.field("filter", source, offset, length);
        builder.endObject();
    }
}
