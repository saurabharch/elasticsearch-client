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
package org.elasticsearch.test.unit.discovery.tao.ping.multicast;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.logging.Loggers;
import static org.elasticsearch.common.settings.ImmutableSettings.Builder.EMPTY_SETTINGS;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.discovery.tao.ping.TaoPing;
import org.elasticsearch.discovery.tao.ping.multicast.MulticastTaoPing;
import org.elasticsearch.threadpool.TransportThreadPool;
import org.elasticsearch.transport.TransportService;
import org.elasticsearch.transport.local.LocalClientTransport;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 */
public class MulticastTaoPingTest extends Assert {

    @Test
    public void testSimplePings() {
        TransportThreadPool threadPool = new TransportThreadPool();
        ClusterName clusterName = new ClusterName("test");

        final TransportService transportServiceA = new TransportService(new LocalClientTransport(threadPool), threadPool).start();
        final DiscoveryNode nodeA = new DiscoveryNode("A", transportServiceA.boundAddress().publishAddress());

        final TransportService transportServiceB = new TransportService(new LocalClientTransport(threadPool), threadPool).start();
        final DiscoveryNode nodeB = new DiscoveryNode("B", transportServiceA.boundAddress().publishAddress());

        MulticastTaoPing taoPingA = new MulticastTaoPing(EMPTY_SETTINGS, threadPool, transportServiceA, clusterName, nodeA);
        taoPingA.start();

        MulticastTaoPing taoPingB = new MulticastTaoPing(EMPTY_SETTINGS, threadPool, transportServiceB, clusterName, nodeB);
        taoPingB.start();

        try {
            TaoPing.PingResponse[] pingResponses = taoPingA.pingAndWait(TimeValue.timeValueSeconds(1));
            assertEquals(pingResponses.length, 1);
            assertEquals(pingResponses[0].target().id(), "B");
        } finally {
            taoPingA.stop();
            taoPingB.stop();
            transportServiceA.close();
            transportServiceB.close();
            threadPool.shutdown();
        }
    }

    @Test
    public void testExternalPing() throws Exception {
        TransportThreadPool threadPool = new TransportThreadPool();
        ClusterName clusterName = new ClusterName("test");
        
        final TransportService transportServiceA = new TransportService(new LocalClientTransport(threadPool), threadPool).start();
        final DiscoveryNode nodeA = new DiscoveryNode("A", transportServiceA.boundAddress().publishAddress());

        MulticastTaoPing taoPingA = new MulticastTaoPing(EMPTY_SETTINGS, threadPool, transportServiceA, clusterName, nodeA);
        taoPingA.start();

        MulticastSocket multicastSocket = null;
        try {
            Loggers.getLogger(MulticastTaoPing.class).setLevel("TRACE");
            multicastSocket = new MulticastSocket(54328);
            multicastSocket.setReceiveBufferSize(2048);
            multicastSocket.setSendBufferSize(2048);
            multicastSocket.setSoTimeout(60000);

            DatagramPacket datagramPacket = new DatagramPacket(new byte[2048], 2048, InetAddress.getByName("224.2.2.4"), 54328);
            XContentBuilder builder = XContentFactory.jsonBuilder().startObject().startObject("request").field("cluster_name", "test").endObject().endObject();
            datagramPacket.setData(builder.bytes().toBytes());
            multicastSocket.send(datagramPacket);
            Thread.sleep(100);
        } finally {
            Loggers.getLogger(MulticastTaoPing.class).setLevel("INFO");
            if (multicastSocket != null) {
                multicastSocket.close();
            }
            taoPingA.stop();
            threadPool.shutdown();
        }
    }
}
