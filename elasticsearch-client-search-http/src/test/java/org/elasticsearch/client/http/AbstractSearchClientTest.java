package org.elasticsearch.client.http;

import java.net.ConnectException;
import org.elasticsearch.common.logging.ESLogger;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.util.concurrent.UncategorizedExecutionException;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public abstract class AbstractSearchClientTest {

    protected final ESLogger logger = ESLoggerFactory.getLogger(getClass().getName());
    protected HttpSearchClient client;

    @BeforeTest
    public void startClient() {
        if (client == null) {
            this.client = new HttpSearchClient();
        }
    }

    @AfterTest
    public void stopClient() {
        if (client != null) {
            client.close();
        }
    }

    @Test
    protected void test() {
        try {
            doTest();
        } catch (UncategorizedExecutionException e) {
            if (!(e.getRootCause() instanceof ConnectException)) {
                throw e;
            } else {
                logger.warn("oops, test skipped, no node found to connect to: " + e.getRootCause().getMessage());
            }
        }
    }

    protected abstract void doTest() throws UncategorizedExecutionException;
}
