package org.elasticsearch.cluster.routing.allocation;

import org.elasticsearch.cluster.routing.allocation.decider.AllocationDecider;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.cluster.routing.RoutingNodes;
import org.elasticsearch.cluster.routing.RoutingTable;
import org.elasticsearch.index.shard.ShardId;

public interface RoutingAllocation {
    
    AllocationDecider deciders();
    
    RoutingTable routingTable();

    RoutingNodes routingNodes();
    
    MetaData metaData();

    DiscoveryNodes nodes();

    AllocationExplanation explanation();

    void ignoreDisable(boolean ignoreDisable);
    
    boolean ignoreDisable();
    
    void addIgnoreShardForNode(ShardId shardId, String nodeId);
     
    boolean shouldIgnoreShardForNode(ShardId shardId, String nodeId);
}
