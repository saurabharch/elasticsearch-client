package org.elasticsearch.client.http;

import com.google.common.collect.Maps;
import java.net.ConnectException;
import java.util.Map;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.util.concurrent.UncategorizedExecutionException;
import org.testng.annotations.Test;

public class UpdateTests {

    private final ESLogger logger = ESLoggerFactory.getLogger(getClass().getName());

    @Test
    public void testUpdate() throws Exception {
        try {
            HttpIngestClient client = new HttpIngestClient();
            
            Map<String,Object> map = Maps.newHashMap();
            map.put("tag", "blue");
            
            UpdateResponse response = client.prepareUpdate("test", "type1", "1")
                    .setScript("ctx._source.tags += tag")
                    .setScriptParams(map)
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

    @Test
    public void testUpdateDoc() throws Exception {
        try {
            HttpIngestClient client = new HttpIngestClient();
            
            UpdateResponse response = client.prepareUpdate("test", "type1", "1")
                    .setDoc("{\"Well\":\"done, boy!\"}")
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
