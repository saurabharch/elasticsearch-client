package org.elasticsearch.client.http;

import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.common.util.concurrent.UncategorizedExecutionException;
import org.testng.annotations.Test;

import static org.elasticsearch.client.IngestRequests.indexRequest;

import java.util.Iterator;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.update.UpdateResponse;

public class IndestTests extends AbstractClientTest {

    protected void doTest() throws UncategorizedExecutionException {
        IndexResponse indexResponse = client
                .prepareIndex("test", "type1", "1")
                .setSource("{\"tags\":[\"red\"]}")
                .execute().actionGet();
        logger.info("success, got response = {}/{}/{}", indexResponse.index(), indexResponse.type(), indexResponse.id());

        GetResponse getResponse = client.prepareGet("test", "type1", "1").execute().actionGet();
        logger.info("success, got response, _source = {}",
                getResponse.getSourceAsString());

        BulkResponse bulkResponse = client.prepareBulk()
                .add(indexRequest().index("test").type("type1").id("2").source("Hello", "World"))
                .add(indexRequest().index("test").type("type1").id("3").source("Welcome", "to Elasticsearch"))
                .execute().actionGet();

        for (BulkItemResponse r : bulkResponse.items()) {
            if (r.failed()) {
                logger.error("error: {}", r.failureMessage());
            } else {
                logger.info("info: {}/{}/{}", r.getIndex(), r.getType(), r.getId());
            }
        }

        MultiGetResponse multiGetResponse = client.prepareMultiGet()
                .add("test", "type1", "1", "2")
                .execute().actionGet();

        Iterator<MultiGetItemResponse> it = multiGetResponse.iterator();
        while (it.hasNext()) {
            MultiGetItemResponse itemResponse = it.next();
            logger.info("success, got response = {}", itemResponse.response());
        }

        UpdateResponse response = client.prepareUpdate("test", "type1", "1")
                .setDoc("{\"Well\":\"done, boy!\"}")
                .execute().actionGet();

        logger.info("success, got response = {}/{}/{}", response.index(), response.type(), response.id());

        DeleteResponse deleteResponse = client.prepareDelete("test", "type1", "1")
                .execute().actionGet();

        logger.info("success, got response = {}/{}/{}", deleteResponse.index(), deleteResponse.type(), deleteResponse.id());

    }
}
