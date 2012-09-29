Elasticsearch Client
====================

Elasticsearch Client is a project for generating a modularized codebase for clients that are accessing Elasticsearch from remote.

Existing client implementations in Elasticsearch are

- TCP (NodeClient, TransportClient) clients, connecting to server default ports 9300-9400
- HTTP (REST, Netty-based) clients, connecting to server default ports 9200-9300

While the node client is using a discovery mechanism to connect to a cluster by using given network interfaces, the transport client can connect to specified network addresses. Both clients initialize a full node - with a node name, node services, modules, even plugins can be used. Such a client needs to rendezvous with a discovered Elasticsearch cluster.

In designing middleware, this approach is similar to a two-tier architecture, where NodeClient and TransportClient are "fat" clients, they carry all the dependencies of the server. This can lead to tedious work if more client implementations need to be added.

A three-tier approach would introduce an additional layer between Elasticsearch NodeClient/TransportClient and the server code. The advantage is separation of concerns. The Elasticsearch server is only loosely coupled to the client. Not every change at the code on server side would enforce client code updates. As a side effect, the Elasticsearch server codebase, which is rather large, could be modularized by reusing the client code base.

The idea of the Elasticsearch Client project is to factor out the client code from the server code. This allows more flexible implementations, for instance for using additional transport plugins. Developing additional transport implementations like WebSocket or SPDY can benefit from a codebase common to all Elasticsearch clients.

Goals of the Elasticsearch Client project are

- minimum version is Java 7
- a base client API and three client submodules (ingest client, search client, admin client)
- reusable code in any Java project (for implementing connectors etc.)
- implement a WebSocket client which is API-compatible to existing client code for NodeClient/TransportClient
- HTTP client (re-)implementation (REST)
- SPDY client implementation (future plan)

It is expected that clients will run remote, that is they never share the same JVM with the server code.

What makes an Elasticsearch client?
===================================

When we talk of an Elasticsearch client, we refer to a Java program that
 
- can build requests with XContentBuilder
- submit requests to the server 
- receives responses from the server
- uses QueryBuilders / FilterBuilders for building search requests

Elasticsearch client code differs from the server code, it has no code for

- cluster discovery, cluster management
- node membership in the cluster (different to NodeClient or TransportClient)
- services, modules
- index engine, shard operations
- field mapping
- TCP transport (the Elasticsearch internal protocol)
- plugins
- rivers
- scripting (mvel, Javascript, Python etc.)
- analyzers, tokenizers

Because the client build does not use relocation like the Elasticsearch server does, Maven project dependencies will be transparent (Lucene, Jackson, Guava, Joda etc.)

Client hierarchy
================

There is a client hierarchy: the Ingest Client, the Search Client, and the Admin Client.

- the **Ingest Client** can issue ingest actions without requiring Lucene jars. It is only meant for data pushing. With an Ingest Client, it is not possible to send queries (only get requests) or admin actions like index creations or deletions or node shutdowns/restarts.

- the **Search Client** can issue read operations and uses the Lucene Queries jar. With a search client, it is not possible to ingest any data or to issue admin actions.

- the **Admin Client** can issue administrative actions and will also have the capabilities of a Search Client, because actions like warming/explain depend on the Search Client.

The selected Maven project group ID is **org.elasticsearch.client** and the Maven artifact names are

    **elasticsearch-client** (pom)
        
		**elasticsearch-client-api** (jar)
        
		**elasticsearch-client-ingest-api** (jar)
        
		**elasticsearch-client-search-api** (jar)
        
		**elasticsearch-client-admin-api** (jar)

		**elasticsearch-client-compression-lzf** (jar)

		**elasticsearch-client-compression-snappy** (jar)

		**elasticsearch-client-transport-api** (jar)

		**elasticsearch-client-transport-netty** (jar)

		**elasticsearch-client-discovery-tao** (jar)

This modularization allows client development for the full or only for parts of the Elasticsearch API. 


Generating the client codebase
==============================

The Elasticsearch codebase (currently 0.20.0.Beta1-SNAPHOT) was used as a starting point for the project.

Unfortunately, the code required some modifications. These are the main changes applied to the code while factoring out client code from the Elasticsearch codebase. The list may be incomplete.

- using jsr166y as implemented in JDK 7

- Lucene UnicodeUtil (and deps) copied to org.elasticseach.common.lucene.util

- shorter org.elasticsearch.client.Client API, methods split to IngestClient, SearchClient, AdminClient

- moved all NAME strings beyond org.elasticsearch.index.query from the ...Action class to the corresponding ...Builder class 

- ClusterBlocks: org.elasticsearch.cluster.metadata.MetaDataStateIndexService.INDEX_CLOSED_BLOCK moved to ClusterBlocks.INDEX_CLOSED_BLOCK

- NodesInfoResponse: removed AbstractComponent in SettingsFilter

- RoutingAllocation: Result subclass factored to out org.elasticsearch.cluster.routing.allocation.RoutingAllocationResult

- in org.elasticsearch.cluster.metadata.AliasAction, method AliasAction filter(FilterBuilder filterBuilder) removed 

- Service API for ES compressors, submodules for each compression algo

- Compressor: many compressor instances, regarding to Netty depnedency (and Lucene)

- IndexMetadata: MapperService.DEFAULT_MAPPING moved to IndexMetadata.DEFAULT_MAPPING

- MappingMetadata:  TimestampFieldMapper.DEFAULT_DATE_TIME_FORMAT moved to MappingMetadata.DEFAULT_DATE_TIME_FORMAT

- constructor MappingMetaData(DocumentMapper docMapper)  removed

- ThreadPool: reduced versions for client-api and transport-api submodules (ClientThreadPool, TransportThreadPool)

- ThreadPool.Info subclass moved to ThreadPoolInfo.Info

- IndexAction: removed process() method, because it is only related to TransportIndexAction / TransportBulkAction

- org.elasticsearch.action.Action: got a fourth class parameter (the client class)
