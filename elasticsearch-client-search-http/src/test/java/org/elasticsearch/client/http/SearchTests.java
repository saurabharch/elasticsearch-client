package org.elasticsearch.client.http;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.util.concurrent.UncategorizedExecutionException;

public class SearchTests extends AbstractSearchClientTest {


    @Override
    protected void doTest() throws UncategorizedExecutionException {
            SearchResponse response = client
                    .prepareSearch("test")
                    .setExtraSource("{\"query\":{\"match_all\":{}}}")
                    .execute().actionGet(3000L);

            logger.info("success, got response = {}",
                    response);
       
    }
}
