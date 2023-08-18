package io.optimism.batcher.telemetry;

import io.micrometer.core.instrument.MeterRegistry;
import io.optimism.utilities.telemetry.MetricsServer;

/**
 * The BatcherMetricsRegistry enum.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
@SuppressWarnings("ImmutableEnumChecker")
public enum BatcherMetricsRegistry {
  /** The instance of BatcherMetricsRegistry. */
  INSTANCE;

  private final MeterRegistry prometheusRegistry;

  BatcherMetricsRegistry() {
    this.prometheusRegistry = MetricsServer.createPrometheusRegistry();
  }

  /**
   * Get singleton meter registry.
   *
   * @return the meter registry
   */
  public MeterRegistry registry() {
    return this.prometheusRegistry;
  }
}
