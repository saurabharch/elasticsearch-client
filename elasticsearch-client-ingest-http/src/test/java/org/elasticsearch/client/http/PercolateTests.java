package org.elasticsearch.client.http;

import java.net.ConnectException;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.util.concurrent.UncategorizedExecutionException;
import org.testng.annotations.Test;

public class PercolateTests {

    private final ESLogger logger = ESLoggerFactory.getLogger(getClass().getName());

    @Test
    public void testPercolate() throws Exception {
        try {
            HttpIngestClient client = new HttpIngestClient();
            
        } catch (UncategorizedExecutionException e) {
            if (!(e.getRootCause() instanceof ConnectException)) {
                throw e;
            } else {
                logger.warn("oops, test skipped, no node found to connect to: " + e.getRootCause().getMessage());
            }
        }
    }
}
