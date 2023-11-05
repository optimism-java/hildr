/*
 * Copyright 2023 281165273grape@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

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
