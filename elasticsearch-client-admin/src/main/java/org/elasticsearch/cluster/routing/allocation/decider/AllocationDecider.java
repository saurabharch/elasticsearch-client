package org.elasticsearch.cluster.routing.allocation.decider;

import org.elasticsearch.cluster.routing.RoutingNode;
import org.elasticsearch.cluster.routing.ShardRouting;
import org.elasticsearch.cluster.routing.allocation.RoutingAllocation;
import org.elasticsearch.common.settings.Settings;


public interface AllocationDecider {

    public static enum Decision {
        YES {
            @Override
            public boolean allocate() {
                return true;
            }

            @Override
            public boolean allowed() {
                return true;
            }
        },
        NO {
            @Override
            public boolean allocate() {
                return false;
            }

            @Override
            public boolean allowed() {
                return false;
            }
        },
        THROTTLE {
            @Override
            public boolean allocate() {
                return false;
            }

            @Override
            public boolean allowed() {
                return true;
            }
        };

        /**
         * It can be allocated *now* on a node. Note, it might be {@link #allowed()} to be allocated
         * on a node, yet, allocate will be <tt>false</tt> since its being throttled for example.
         */
        public abstract boolean allocate();

        /**
         * Is allocation allowed on a node. Note, this does not mean that we should allocate *now*,
         * though, in extreme cases, we might "force" allocation.
         */
        public abstract boolean allowed();
    }


    boolean canRebalance(ShardRouting shardRouting, RoutingAllocation allocation);

    AllocationDecider.Decision canAllocate(ShardRouting shardRouting, RoutingNode node, RoutingAllocation allocation);

    /**
     * Can the provided shard routing remain on the node?
     */
    boolean canRemain(ShardRouting shardRouting, RoutingNode node, RoutingAllocation allocation);
}
