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

package org.elasticsearch.test.unit.discovery.tao.ping.unicast;

import org.elasticsearch.cluster.ClusterName;
import org.elasticsearch.cluster.node.DiscoveryNode;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.discovery.tao.ping.TaoPing;
import org.elasticsearch.discovery.tao.ping.unicast.UnicastTaoPing;
import org.elasticsearch.threadpool.TransportThreadPool;
import org.elasticsearch.transport.TransportService;
import org.elasticsearch.transport.local.LocalClientTransport;
import org.elasticsearch.transport.netty.NettyTransport;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 *
 */
public class UnicastTaoPingTest extends Assert {

    @Test
    public void testSimplePings() {
        TransportThreadPool threadPool = new TransportThreadPool();
        ClusterName clusterName = new ClusterName("test");

        NettyTransport transportA = new NettyTransport(threadPool);
        final TransportService transportServiceA = new TransportService(transportA, threadPool).start();
        final DiscoveryNode nodeA = new DiscoveryNode("A", transportServiceA.boundAddress().publishAddress());
        InetSocketTransportAddress addressA = (InetSocketTransportAddress) transportA.boundAddress().publishAddress();

        NettyTransport transportB = new NettyTransport(threadPool);
        final TransportService transportServiceB = new TransportService(transportB, threadPool).start();
        final DiscoveryNode nodeB = new DiscoveryNode("B", transportServiceA.boundAddress().publishAddress());
        InetSocketTransportAddress addressB = (InetSocketTransportAddress) transportB.boundAddress().publishAddress();

        Settings hostsSettings = ImmutableSettings.settingsBuilder().putArray("discovery.tao.ping.unicast.hosts",
                addressA.address().getAddress().getHostAddress() + ":" + addressA.address().getPort(),
                addressB.address().getAddress().getHostAddress() + ":" + addressB.address().getPort())
                .build();

        UnicastTaoPing taoPingA = new UnicastTaoPing(hostsSettings, threadPool, transportServiceA, clusterName, nodeA, null);
        
        taoPingA.start();

        UnicastTaoPing taoPingB = new UnicastTaoPing(hostsSettings, threadPool, transportServiceB, clusterName, nodeB, null);
        
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
}
