package io.optimism.telemetry;

import io.micrometer.core.instrument.Gauge;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * InnerMetrics tools.
 *
 * @author thinkAfCod
 * @since 2023.05
 */
public class InnerMetrics {

    private static AtomicReference<BigInteger> FINALIZED_HEAD;
    private static AtomicReference<BigInteger> SAFE_HEAD;
    private static AtomicReference<BigInteger> SYNCED;

    private InnerMetrics() {}

    /**
     * start a http server for prometheus to access.
     *
     * @param port custom http server port
     */
    public static void start(int port) {
        FINALIZED_HEAD = new AtomicReference<>(BigInteger.ZERO);
        SAFE_HEAD = new AtomicReference<>(BigInteger.ZERO);
        SYNCED = new AtomicReference<>(BigInteger.ZERO);
        var registry = MetricsServer.createPrometheusRegistry();
        Gauge.builder("finalized_head", FINALIZED_HEAD, ref -> ref.get().doubleValue())
                .description("finalized head number")
                .register(registry);

        Gauge.builder("safe_head", SAFE_HEAD, ref -> ref.get().doubleValue())
                .description("safe head number")
                .register(registry);

        Gauge.builder("synced", SYNCED, ref -> ref.get().doubleValue())
                .description("synced flag")
                .register(registry);
        MetricsServer.start(registry, port);
    }

    /** stop the http server. */
    public static void stop() {
        MetricsServer.stop();
    }

    /**
     * set metrics finalized head block.
     *
     * @param finalizedHead finalized head block
     */
    public static void setFinalizedHead(BigInteger finalizedHead) {
        if (FINALIZED_HEAD != null) {
            FINALIZED_HEAD.getAndSet(finalizedHead);
        }
    }

    /**
     * set metrics safe head block.
     *
     * @param safeHead safe head block
     */
    public static void setSafeHead(BigInteger safeHead) {
        if (SAFE_HEAD != null) {
            SAFE_HEAD.getAndSet(safeHead);
        }
    }

    /**
     * set metrics synced block count.
     *
     * @param synced synced block count
     */
    public static void setSynced(BigInteger synced) {
        if (SYNCED != null) {
            SYNCED.getAndSet(synced);
        }
    }
}
