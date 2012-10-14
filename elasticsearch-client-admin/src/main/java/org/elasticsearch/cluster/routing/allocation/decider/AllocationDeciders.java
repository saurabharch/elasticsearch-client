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

package org.elasticsearch.cluster.routing.allocation.decider;

import java.util.HashSet;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.Set;
import org.elasticsearch.cluster.routing.RoutingNode;
import org.elasticsearch.cluster.routing.ShardRouting;
import org.elasticsearch.cluster.routing.allocation.RoutingAllocation;

/**
 * Holds several {@link AllocationDecider}s and combines them into a single allocation decision.
 */
public class AllocationDeciders implements AllocationDecider {

    private final static Set<AllocationDecider> allocationDeciders = new HashSet();

    static {
        ServiceLoader<AllocationDecider> loader = ServiceLoader.load(AllocationDecider.class);
        Iterator<AllocationDecider> it = loader.iterator();
        while (it.hasNext()) {
            AllocationDecider decider = it.next();
            allocationDeciders.add(decider);
        }
    }

    @Override
    public boolean canRebalance(ShardRouting shardRouting, RoutingAllocation allocation) {
        for (AllocationDecider allocation1 : allocationDeciders) {
            if (!allocation1.canRebalance(shardRouting, allocation)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Decision canAllocate(ShardRouting shardRouting, RoutingNode node, RoutingAllocation allocation) {
        Decision ret = Decision.YES;
        // first, check if its in the ignored, if so, return NO
        if (allocation.shouldIgnoreShardForNode(shardRouting.shardId(), node.nodeId())) {
            return Decision.NO;
        }
        // now, go over the registered allocationDeciders
        for (AllocationDecider allocation1 : allocationDeciders) {
            Decision decision = allocation1.canAllocate(shardRouting, node, allocation);
            if (decision == Decision.NO) {
                return Decision.NO;
            } else if (decision == Decision.THROTTLE) {
                ret = Decision.THROTTLE;
            }
        }
        return ret;
    }

    @Override
    public boolean canRemain(ShardRouting shardRouting, RoutingNode node, RoutingAllocation allocation) {
        if (allocation.shouldIgnoreShardForNode(shardRouting.shardId(), node.nodeId())) {
            return false;
        }
        for (AllocationDecider allocation1 : allocationDeciders) {
            if (!allocation1.canRemain(shardRouting, node, allocation)) {
                return false;
            }
        }
        return true;
    }
}
