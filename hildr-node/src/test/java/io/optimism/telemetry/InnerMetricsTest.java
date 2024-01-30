package io.optimism.telemetry;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigInteger;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Inner metrics test case.
 *
 * @author thinkAfCod
 * @since 2023.05
 */
public class InnerMetricsTest {

    @BeforeAll
    static void setUp() {
        InnerMetrics.start(9200);
    }

    @AfterAll
    static void tearDown() {
        InnerMetrics.stop();
    }

    private String getMetric() throws IOException {
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request =
                new Request.Builder().get().url("http://127.0.0.1:9200/metrics").build();
        return client.newCall(request).execute().body().string();
    }

    @Test
    void testUpdateMetric() throws IOException {
        InnerMetrics.setFinalizedHead(BigInteger.ONE);
        InnerMetrics.setSafeHead(BigInteger.TWO);
        InnerMetrics.setSynced(BigInteger.valueOf(3L));
        String metric = this.getMetric();
        assertTrue(metric.contains("finalized_head 1.0"));
        assertTrue(metric.contains("safe_head 2.0"));
        assertTrue(metric.contains("synced 3.0"));
    }
}
