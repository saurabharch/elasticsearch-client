package org.elasticsearch.client.http;

import java.net.ConnectException;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.util.concurrent.UncategorizedExecutionException;
import org.testng.annotations.Test;

import static org.elasticsearch.client.IngestRequests.indexRequest;

public class BulkTests {

    private final ESLogger logger = ESLoggerFactory.getLogger(getClass().getName());

    @Test
    public void testBulkIndex() throws Exception {
        try {
            HttpIngestClient client = new HttpIngestClient();
            
            BulkResponse response = client.prepareBulk()
                    .add(indexRequest().index("test").type("type1").id("2").source("Hello", "World"))
                    .add(indexRequest().index("test").type("type1").id("3").source("Welcome", "to Elasticsearch"))
                    .execute().actionGet();
            
            for (BulkItemResponse r : response.items()) {
                if (r.failed()) {
                    logger.error("error: {}", r.failureMessage());
                } else {
                    logger.info("info: {}/{}/{}", r.getIndex(), r.getType(), r.getId());
                }
            }
            
        } catch (UncategorizedExecutionException e) {
            if (!(e.getRootCause() instanceof ConnectException)) {
                throw e;
            } else {
                logger.warn("oops, test skipped, no node found to connect to: " + e.getRootCause().getMessage());
            }
        }
    }
}
