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
package org.elasticsearch.threadpool.client;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentBuilderString;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.elasticsearch.threadpool.ThreadPoolInfo;
import org.elasticsearch.threadpool.ThreadPoolInfoElement;

public class ClientThreadPoolInfo implements ThreadPoolInfo {

    private List<ThreadPoolInfoElement> infos;

    ClientThreadPoolInfo() {
    }

    public ClientThreadPoolInfo(List<ThreadPoolInfoElement> infos) {
        this.infos = infos;
    }

    
    public Iterator<ThreadPoolInfoElement> iterator() {
        return infos.iterator();
    }

    public static ThreadPoolInfo readThreadPoolInfo(StreamInput in) throws IOException {
        ClientThreadPoolInfo info = new ClientThreadPoolInfo();
        info.readFrom(in);
        return info;
    }

    
    public void readFrom(StreamInput in) throws IOException {
        int size = in.readVInt();
        infos = new ArrayList(size);
        for (int i = 0; i < size; i++) {
            ThreadPoolInfoElement info = new ClientThreadPoolInfoElement();
            info.readFrom(in);
            infos.add(info);
        }
    }

    
    public void writeTo(StreamOutput out) throws IOException {
        out.writeVInt(infos.size());
        for (ThreadPoolInfoElement info : infos) {
            info.writeTo(out);
        }
    }

    static final class Fields {

        static final XContentBuilderString THREAD_POOL = new XContentBuilderString("thread_pool");
    }

    
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        builder.startObject(Fields.THREAD_POOL);
        for (ThreadPoolInfoElement info : infos) {
            info.toXContent(builder, params);
        }
        builder.endObject();
        return builder;
    }

}