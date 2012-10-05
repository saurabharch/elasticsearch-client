package org.elasticsearch.cluster.routing.allocation;

import org.elasticsearch.cluster.routing.RoutingTable;

public class RoutingAllocationResult {

    private final boolean changed;
    private final RoutingTable routingTable;
    private final AllocationExplanation explanation;

    public RoutingAllocationResult(boolean changed, RoutingTable routingTable, AllocationExplanation explanation) {
        this.changed = changed;
        this.routingTable = routingTable;
        this.explanation = explanation;
    }

    public boolean changed() {
        return this.changed;
    }

    public RoutingTable routingTable() {
        return routingTable;
    }

    public AllocationExplanation explanation() {
        return explanation;
    }
}