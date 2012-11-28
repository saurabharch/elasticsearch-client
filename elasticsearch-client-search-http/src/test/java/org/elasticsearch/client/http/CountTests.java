package org.elasticsearch.client.http;

import org.elasticsearch.action.count.CountResponse;
import org.elasticsearch.common.util.concurrent.UncategorizedExecutionException;

public class CountTests extends AbstractSearchClientTest {


    @Override
    protected void doTest() throws UncategorizedExecutionException {
            CountResponse response = client.prepareCount("test").execute().actionGet(3000L);

            logger.info("success, got response, count = {}",
                    response.count());
       
    }
}
