/*
 * Licensed to Elastic Search and Shay Banon under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Elastic Search licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.action.admin.indices.warmer.get;

import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.action.support.master.MasterNodeOperationRequest;
import org.elasticsearch.common.Nullable;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

import java.io.IOException;

/**
 * A request to get an index warmer.
 */
public class GetWarmerRequest extends MasterNodeOperationRequest<GetWarmerRequest> {

    private String name;

    private String[] indices = Strings.EMPTY_ARRAY;

    GetWarmerRequest() {
    }

    /**
     * Constructs a new get warmer request for the specified name.
     *
     * @param name: the name (or wildcard expression) of the warmer to match, null to get all.
     */
    public GetWarmerRequest(String name) {
        this.name = name;
    }

    @Override
    public ActionRequestValidationException validate() {
        ActionRequestValidationException validationException = null;
        return validationException;
    }

    /**
     * The name to get.
     */
    @Nullable
    public String name() {
        return name;
    }

    /**
     * The name (or wildcard expression) of the index warmer to get, or null
     * to get all warmers.
     */
    public GetWarmerRequest name(@Nullable String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the indices this put mapping operation will execute on.
     */
    public GetWarmerRequest indices(String[] indices) {
        this.indices = indices;
        return this;
    }

    /**
     * The indices the mappings will be put.
     */
    public String[] indices() {
        return indices;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        super.readFrom(in);
        name = in.readOptionalString();
        indices = in.readStringArray();
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeOptionalString(name);
        out.writeStringArrayNullable(indices);
    }
}