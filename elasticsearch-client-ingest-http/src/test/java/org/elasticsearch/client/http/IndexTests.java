package org.elasticsearch.client.http;

import java.net.ConnectException;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.util.concurrent.UncategorizedExecutionException;
import org.testng.annotations.Test;

public class IndexTests {

    private final ESLogger logger = ESLoggerFactory.getLogger(getClass().getName());

    @Test
    public void testIndex() throws Exception {
        try {
            HttpIngestClient client = new HttpIngestClient();
            
            IndexResponse response = client.prepareIndex("test", "type1", "1")
                    .setSource("{\"tags\":[\"red\"]}")
                    .execute().actionGet();
            
            logger.info("success, got response = {}/{}/{}", response.index(), response.type(), response.id());
        } catch (UncategorizedExecutionException e) {
            if (!(e.getRootCause() instanceof ConnectException)) {
                throw e;
            } else {
                logger.warn("oops, test skipped, no node found to connect to: " + e.getRootCause().getMessage());
            }
        }
    }
}
