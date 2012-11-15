package org.elasticsearch.client.http;

import java.net.ConnectException;
import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.util.concurrent.UncategorizedExecutionException;
import org.testng.annotations.Test;

import static org.elasticsearch.common.settings.ImmutableSettings.settingsBuilder;

import org.elasticsearch.common.settings.Settings;

public class CountTests {

    private final ESLogger logger = ESLoggerFactory.getLogger(getClass().getName());

    @Test
    public void testCount() throws Exception {
        
        Settings settings = settingsBuilder()
               // .put("http.connection.url", "http://localhost:9200")
                .build();
                
        HttpSearchClient client = new HttpSearchClient(settings);
        try {

            CountResponse response = client.prepareCount("hbz20121113").execute().actionGet(3000L);

            logger.info("success, got response, count = {}",
                    response.count());
        } catch (UncategorizedExecutionException e) {
            if (!(e.getRootCause() instanceof ConnectException)) {
                throw e;
            } else {
                logger.warn("oops, test skipped, no node found to connect to: " + e.getRootCause().getMessage());
            }
        } finally {
            client.close();
        }
    }
}
