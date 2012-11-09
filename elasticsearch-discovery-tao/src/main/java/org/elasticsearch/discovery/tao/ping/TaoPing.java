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

package org.elasticsearch.discovery.tao.ping;

import org.elasticsearch.ElasticSearchException;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.io.stream.Streamable;
import org.elasticsearch.common.unit.TimeValue;

import java.io.IOException;

import static org.elasticsearch.cluster.ClusterName.readClusterName;
import static org.elasticsearch.cluster.node.DiscoveryNode.readNode;

/**
 *
 */
public interface TaoPing {

    // ZenPing also has a NodeService provider method
   // void setNodesProvider(DiscoveryNodesProvider nodesProvider);

    void ping(PingListener listener, TimeValue timeout) throws ElasticSearchException;

    public interface PingListener {

        void onPing(PingResponse[] pings);
    }

    public class PingResponse implements Streamable {

        private ClusterName clusterName;

        private DiscoveryNode target;

        private PingResponse() {
        }

        public PingResponse(DiscoveryNode target, ClusterName clusterName) {
            this.target = target;
            this.clusterName = clusterName;
        }

        public ClusterName clusterName() {
            return this.clusterName;
        }

        public DiscoveryNode target() {
            return target;
        }

        public static PingResponse readPingResponse(StreamInput in) throws IOException {
            PingResponse response = new PingResponse();
            response.readFrom(in);
            return response;
        }

        public void readFrom(StreamInput in) throws IOException {
            clusterName = readClusterName(in);
            target = readNode(in);
            if (in.readBoolean()) {
                readNode(in); // skip master node info
            }
        }

        public void writeTo(StreamOutput out) throws IOException {
            clusterName.writeTo(out);
            target.writeTo(out);
            out.writeBoolean(false);
        }

        @Override
        public String toString() {
            return "ping_response{target [" + target + "],  cluster_name[" + clusterName.value() + "]}";
        }
    }
}
