/*
 * Copyright 2023 q315xia@163.com
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

package io.optimism.batcher.telemetry;

import io.micrometer.core.instrument.MeterRegistry;
import io.optimism.utilities.telemetry.MetricsServer;

/**
 * The BatcherMetricsServer type.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public class BatcherMetricsServer {

    private BatcherMetricsServer() {}

    /**
     * Create a NoopMatcherMetrics instance.
     *
     * @return NoopMatcherMetrics instance.
     */
    public static BatcherMetrics noop() {
        return new NoopBatcherMetrics();
    }

    /**
     * Start a metrics server on specific port.
     *
     * @param port Server port
     */
    public static void start(int port) {
        MeterRegistry registry = getRegistry();
        MetricsServer.start(registry, port);
    }

    /**
     * Get single instance of the meter register .
     *
     * @return the meter register
     */
    public static MeterRegistry getRegistry() {
        return BatcherMetricsRegistry.INSTANCE.registry();
    }

    /** Stop an active metrics server. */
    public static void stop() {
        MetricsServer.stop();
    }

    /**
     * Check if the service is active.
     *
     * @return Return ture if MetricsServer has been started and still alive, otherwise false.
     */
    public static boolean isActive() {
        return MetricsServer.isActive();
    }
}
