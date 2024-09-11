package io.optimism.telemetry;

import io.micrometer.core.instrument.Counter;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The EventMeter type.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public class EventMeter {

    private final Counter total;

    private final AtomicLong lastTime;

    /**
     * The EventMeter constructor.
     *
     * @param total The total counter
     * @param lastTime The last time record ref.
     */
    public EventMeter(Counter total, AtomicLong lastTime) {
        this.total = total;
        this.lastTime = lastTime;
    }

    /** Record event count and occur time. */
    public void record() {
        this.total.increment();
        this.lastTime.getAndSet(Instant.now().getEpochSecond());
    }
}
