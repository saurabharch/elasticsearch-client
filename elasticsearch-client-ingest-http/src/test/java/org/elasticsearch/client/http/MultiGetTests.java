package org.elasticsearch.client.http;

import java.net.ConnectException;
import java.util.Iterator;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.util.concurrent.UncategorizedExecutionException;
import org.testng.annotations.Test;

public class MultiGetTests {

    private final ESLogger logger = ESLoggerFactory.getLogger(getClass().getName());

    @Test
    public void testMultiGet() throws Exception {
        try {
            HttpIngestClient client = new HttpIngestClient();
            
            MultiGetResponse response = client.prepareMultiGet()
                    .add("test", "type1", "1", "2")                    
                    .execute().actionGet();
            
            Iterator<MultiGetItemResponse> it = response.iterator();
            while (it.hasNext()) {
                MultiGetItemResponse itemResponse = it.next();
                logger.info("success, got response = {}", itemResponse.response());
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
