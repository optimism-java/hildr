package io.optimism.utilities.telemetry;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.micrometer.core.instrument.MeterRegistry;
import io.optimism.telemetry.MetricsServer;
import io.optimism.telemetry.MetricsSupplier;
import java.io.IOException;
import java.util.HashMap;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Metrics server test case.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public class MetricsServerTest {

    static MetricsSupplier metricsSupplier;

    @BeforeAll
    static void setUp() {
        MeterRegistry registry = MetricsServer.createPrometheusRegistry();
        metricsSupplier = new MetricsSupplier(registry, "utilities_test", new HashMap<>());
        MetricsServer.start(registry, 9201);
    }

    @AfterAll
    static void tearDown() {
        MetricsServer.stop();
    }

    private String getMetric() throws IOException {
        OkHttpClient client = new OkHttpClient.Builder().build();
        Request request =
                new Request.Builder().get().url("http://127.0.0.1:9201/metrics").build();
        return client.newCall(request).execute().body().string();
    }

    @Test
    void testGauge() throws IOException {
        assertFalse(this.getMetric().contains("utilities_test_gauge"));

        metricsSupplier.getOrCreateGauge("gauge", new HashMap<>()).getAndSet(1);
        assertTrue(this.getMetric().contains("utilities_test_gauge 1.0"));
    }

    @Test
    void testGaugeWithTags() throws IOException {
        assertFalse(this.getMetric().contains("{utilities_test_gauge_tags{tagName"));

        HashMap<String, String> tag1 = new HashMap<>();
        tag1.put("tagName", "tagValue1");
        metricsSupplier.getOrCreateGauge("gauge_tags", tag1).getAndSet(1);
        assertTrue(this.getMetric().contains("utilities_test_gauge_tags{tagName=\"tagValue1\",} 1.0"));

        HashMap<String, String> tag2 = new HashMap<>();
        tag2.put("tagName", "tagValue2");
        metricsSupplier.getOrCreateGauge("gauge_tags", tag2).getAndSet(2);
        assertTrue(this.getMetric().contains("utilities_test_gauge_tags{tagName=\"tagValue2\",} 2.0"));
    }

    @Test
    void testCounter() throws IOException {
        System.out.println("testCounter-------------------\n" + this.getMetric());
        assertFalse(this.getMetric().contains("utilities_test_counter_total"));

        metricsSupplier.getOrCreateCounter("counter", new HashMap<>()).increment(1.0);
        assertTrue(this.getMetric().contains("utilities_test_counter_total 1.0"));
    }

    @Test
    void testCounterWithTags() throws IOException {
        assertFalse(this.getMetric().contains("utilities_test_counter_total{tagName"));

        HashMap<String, String> tag1 = new HashMap<>();
        tag1.put("tagName", "tagValue1");
        metricsSupplier.getOrCreateCounter("counter_tags", tag1).increment(1);
        assertTrue(this.getMetric().contains("utilities_test_counter_tags_total{tagName=\"tagValue1\",} 1.0"));

        HashMap<String, String> tag2 = new HashMap<>();
        tag2.put("tagName", "tagValue2");
        metricsSupplier.getOrCreateCounter("counter_tags", tag2).increment(2);
        assertTrue(this.getMetric().contains("utilities_test_counter_tags_total{tagName=\"tagValue2\",} 2.0"));
    }

    @Test
    void testHistogram() throws IOException, InterruptedException {
        metricsSupplier
                .getOrCreateHistogram("histogram", "GWEI", new double[] {1, 2, 3, 4, 5}, new HashMap<>())
                .record(1);
        metricsSupplier
                .getOrCreateHistogram("histogram", "GWEI", new double[] {1, 2, 3, 4, 5}, new HashMap<>())
                .record(2);
        metricsSupplier
                .getOrCreateHistogram("histogram", "GWEI", new double[] {1, 2, 3, 4, 5}, new HashMap<>())
                .record(3);
        metricsSupplier
                .getOrCreateHistogram("histogram", "GWEI", new double[] {1, 2, 3, 4, 5}, new HashMap<>())
                .record(4);
        metricsSupplier
                .getOrCreateHistogram("histogram", "GWEI", new double[] {1, 2, 3, 4, 5}, new HashMap<>())
                .record(4);

        String metrics = this.getMetric();
        assertTrue(metrics.contains("utilities_test_histogram_GWEI_bucket{le=\"1.0\",} 1.0"));
        assertTrue(metrics.contains("utilities_test_histogram_GWEI_bucket{le=\"2.0\",} 2.0"));
        assertTrue(metrics.contains("utilities_test_histogram_GWEI_bucket{le=\"3.0\",} 3.0"));
        assertTrue(metrics.contains("utilities_test_histogram_GWEI_bucket{le=\"4.0\",} 5.0"));
        assertTrue(metrics.contains("utilities_test_histogram_GWEI_bucket{le=\"5.0\",} 5.0"));
    }

    @Test
    void testHistogramWithTags() throws IOException {
        HashMap<String, String> tag1 = new HashMap<>();
        tag1.put("tagName", "tagValue1");
        metricsSupplier
                .getOrCreateHistogram("histogram_tags", "GWEI", new double[] {1, 2, 3, 4, 5}, tag1)
                .record(1);

        HashMap<String, String> tag2 = new HashMap<>();
        tag2.put("tagName", "tagValue2");
        metricsSupplier
                .getOrCreateHistogram("histogram_tags", "GWEI", new double[] {1, 2, 3, 4, 5}, tag2)
                .record(2);

        HashMap<String, String> tag3 = new HashMap<>();
        tag3.put("tagName", "tagValue3");
        metricsSupplier
                .getOrCreateHistogram("histogram_tags", "GWEI", new double[] {1, 2, 3, 4, 5}, tag3)
                .record(3);

        HashMap<String, String> tag4 = new HashMap<>();
        tag4.put("tagName", "tagValue4");
        metricsSupplier
                .getOrCreateHistogram("histogram_tags", "GWEI", new double[] {1, 2, 3, 4, 5}, tag4)
                .record(4);

        HashMap<String, String> tag5 = new HashMap<>();
        tag5.put("tagName", "tagValue5");
        metricsSupplier
                .getOrCreateHistogram("histogram_tags", "GWEI", new double[] {1, 2, 3, 4, 5}, tag5)
                .record(4);

        String metrics = this.getMetric();
        System.out.println("testHistogramWithTags------------\n" + this.getMetric());

        assertTrue(
                metrics.contains("utilities_test_histogram_tags_GWEI_bucket{tagName=\"tagValue1\",le=\"+Inf\",} 1.0"));
        assertTrue(
                metrics.contains("utilities_test_histogram_tags_GWEI_bucket{tagName=\"tagValue2\",le=\"1.0\",} 0.0"));
        assertTrue(
                metrics.contains("utilities_test_histogram_tags_GWEI_bucket{tagName=\"tagValue3\",le=\"2.0\",} 0.0"));
        assertTrue(
                metrics.contains("utilities_test_histogram_tags_GWEI_bucket{tagName=\"tagValue4\",le=\"3.0\",} 0.0"));
        assertTrue(
                metrics.contains("utilities_test_histogram_tags_GWEI_bucket{tagName=\"tagValue5\",le=\"3.0\",} 0.0"));
    }
}
