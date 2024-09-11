package io.optimism.telemetry;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import io.optimism.exceptions.MetricsServerException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

/**
 * The MetricsSupplier type. Will cache all metrics, and register it to MeterRegistry.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public class MetricsSupplier {

    private final Map<String, Object> numberMap;

    private final Map<String, Counter> counterMap;

    private final Map<String, DistributionSummary> histogramMap;

    private final MeterRegistry registry;

    private final String namespace;

    private final Map<String, String> descMap;

    private final HashMap<String, EventMeter> eventMap;

    /**
     * The MetricsSupplier constructor.
     *
     * @param registry The meter registry instance
     * @param namespace The prefix of metrics name
     * @param descMap Map of metrics name to description
     */
    public MetricsSupplier(MeterRegistry registry, String namespace, Map<String, String> descMap) {
        this.numberMap = new HashMap<>();
        this.counterMap = new HashMap<>();
        this.histogramMap = new HashMap<>();
        this.eventMap = new HashMap<>();
        this.descMap = descMap;
        this.registry = registry;
        this.namespace = namespace;
    }

    /**
     * Get gauge. If not exist will create a new gauge.
     *
     * @param name Metrics name.
     * @param tagKeyValue Metrics tags.
     * @return gauge value.
     */
    @SuppressWarnings("unchecked")
    public AtomicReference<BigDecimal> getOrCreateGaugeDecimal(String name, final Map<String, String> tagKeyValue) {
        final String label = name + tagKeyValue.toString();
        var value = this.numberMap.computeIfAbsent(label, key -> {
            var guage = new AtomicReference<BigDecimal>();
            Gauge.Builder<AtomicReference<BigDecimal>> gaugeBuilder = Gauge.builder(
                            withPrefix(name), guage, ref -> ref.get().doubleValue())
                    .description(descMap.get(name));
            if (!tagKeyValue.isEmpty()) {
                final List<Tag> tags = toList(tagKeyValue);
                gaugeBuilder.tags(Tags.of(tags));
            }
            gaugeBuilder.register(registry);
            return guage;
        });
        if (value instanceof AtomicReference) {
            return (AtomicReference<BigDecimal>) value;
        } else {
            throw new MetricsServerException("Not match type for the metrics name");
        }
    }

    /**
     * Get gauge. If not exist will create a new gauge.
     *
     * @param name Metrics name.
     * @param tagKeyValue Metrics tags.
     * @return gauge value.
     */
    public AtomicLong getOrCreateGauge(String name, final Map<String, String> tagKeyValue) {
        final String label = name + tagKeyValue.toString();
        return (AtomicLong) this.numberMap.computeIfAbsent(label, key -> {
            final AtomicLong guage = new AtomicLong();
            Gauge.Builder<AtomicLong> gaugeBuilder = Gauge.builder(withPrefix(name), guage, AtomicLong::doubleValue)
                    .description(descMap.get(name));
            if (!tagKeyValue.isEmpty()) {
                final List<Tag> tags = toList(tagKeyValue);
                gaugeBuilder.tags(Tags.of(tags));
            }
            gaugeBuilder.register(registry);
            return guage;
        });
    }

    /**
     * Get histogram. If not exist will create a new histogram.
     *
     * @param name Metrics name.
     * @param baseUnit Base unit.
     * @param buckets Histogram buckets.
     * @param tagKeyValue Metrics tags.
     * @return the histogram.
     */
    public DistributionSummary getOrCreateHistogram(
            String name, String baseUnit, double[] buckets, final Map<String, String> tagKeyValue) {
        final String label = name + tagKeyValue.toString();
        return this.histogramMap.computeIfAbsent(label, key -> {
            Arrays.sort(buckets);
            DistributionSummary.Builder builder = DistributionSummary.builder(withPrefix(name))
                    .description(descMap.get(name))
                    .serviceLevelObjectives(buckets);
            if (StringUtils.isNotEmpty(baseUnit)) {
                builder.baseUnit(baseUnit);
            }
            if (!tagKeyValue.isEmpty()) {
                final List<Tag> tags = toList(tagKeyValue);
                builder.tags(Tags.of(tags));
            }

            return builder.register(registry);
        });
    }

    /**
     * Get counter. If not exist will create a new counter.
     *
     * @param name Metrics name.
     * @param tagKeyValue Metrics tags.
     * @return the counter.
     */
    public Counter getOrCreateCounter(String name, final Map<String, String> tagKeyValue) {
        final String label = name + tagKeyValue.toString();
        return this.counterMap.computeIfAbsent(label, key -> {
            Counter.Builder counterBuilder = Counter.builder(withPrefix(name)).description(descMap.get(name));
            if (!tagKeyValue.isEmpty()) {
                final List<Tag> tags = toList(tagKeyValue);
                counterBuilder.tags(Tags.of(tags));
            }
            return counterBuilder.register(registry);
        });
    }

    /**
     * Get event meter. If not exist will create a new event meter.
     *
     * @param name Metrics name.
     * @param tagKeyValue Metrics tags.
     * @return the event meter.
     */
    public EventMeter getOrCreateEventMeter(String name, final Map<String, String> tagKeyValue) {
        final String eventLabel = "%s_event".formatted(name);
        return this.eventMap.computeIfAbsent(eventLabel, label -> {
            Counter.Builder counterBuilder = Counter.builder(withPrefix("%s_total".formatted(name)))
                    .description(String.format("Count of %s events", name));

            final AtomicLong guage = new AtomicLong();
            Gauge.Builder<AtomicLong> gaugeBuilder = Gauge.builder(
                            withPrefix(String.format("last_%s_unix", name)), guage, AtomicLong::doubleValue)
                    .description(String.format("Timestamp of last %s event", name));
            if (!tagKeyValue.isEmpty()) {
                final List<Tag> tags = toList(tagKeyValue);
                counterBuilder.tags(Tags.of(tags));
                gaugeBuilder.tags(Tags.of(tags));
            }
            Counter total = counterBuilder.register(registry);
            gaugeBuilder.register(registry);
            return new EventMeter(total, guage);
        });
    }

    /**
     * Histogram buckets.
     *
     * @param start Start value
     * @param width Width value
     * @param count buckets count
     * @return Buckets array
     */
    public double[] linearBuckets(double start, double width, int count) {
        if (count < 1) {
            throw new MetricsServerException("LinearBuckets needs a positive count");
        }
        var buckets = new double[count];
        for (int i = 0; i < count; i++) {
            buckets[i] = start;
            start += width;
        }
        return buckets;
    }

    private String withPrefix(String name) {
        return String.format("%s_%s", namespace, name);
    }

    private List<Tag> toList(final Map<String, String> tagKeyValue) {
        return tagKeyValue.entrySet().stream()
                .map(entry -> Tag.of(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }
}
