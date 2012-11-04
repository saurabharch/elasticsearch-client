package org.elasticsearch.client.http;

import java.net.ConnectException;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.util.concurrent.UncategorizedExecutionException;
import org.testng.annotations.Test;

public class GetTests {

    private final ESLogger logger = ESLoggerFactory.getLogger(getClass().getName());

    @Test
    public void testGet() throws Exception {
        try {
            HttpIngestClient client = new HttpIngestClient();
            
            GetResponse response = client.prepareGet("test", "type1", "1").execute().actionGet();
            
            logger.info("success, got response, _source = {}", 
                    response.getSourceAsString());
            
            
            
        } catch (UncategorizedExecutionException e) {
            if (!(e.getRootCause() instanceof ConnectException)) {
                throw e;
            } else {
                logger.warn("oops, test skipped, no node found to connect to: " + e.getRootCause().getMessage());
            }
        }
    }
}
