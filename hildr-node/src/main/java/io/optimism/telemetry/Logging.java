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

import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.otel.bridge.OtelBaggageManager;
import io.micrometer.tracing.otel.bridge.OtelCurrentTraceContext;
import io.micrometer.tracing.otel.bridge.OtelTracer;
import io.micrometer.tracing.otel.bridge.Slf4JBaggageEventListener;
import io.micrometer.tracing.otel.bridge.Slf4JEventListener;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.util.Collections;

/**
 * Logging Config OpenTelemetry.
 *
 * @author thinkAfCod
 * @since 2023.05
 */
@SuppressWarnings("ImmutableEnumChecker")
public enum Logging {

  /** Logging single instance. */
  INSTANCE;

  private final Tracer tracer;

  Logging() {
    initializeOpenTelemetry();

    final var slf4JEventListener = new Slf4JEventListener();
    final var slf4JBaggageEventListener = new Slf4JBaggageEventListener(Collections.emptyList());

    var otelTracer = GlobalOpenTelemetry.getTracer("global");
    OtelCurrentTraceContext otelCurrentTraceContext = new OtelCurrentTraceContext();
    this.tracer =
        new OtelTracer(
            otelTracer,
            otelCurrentTraceContext,
            event -> {
              slf4JEventListener.onEvent(event);
              slf4JBaggageEventListener.onEvent(event);
            },
            new OtelBaggageManager(
                otelCurrentTraceContext, Collections.emptyList(), Collections.emptyList()));
  }

  /**
   * get Tracer single instance.
   *
   * @return Tracer single instance
   */
  public Tracer getTracer() {
    return tracer;
  }

  private static void initializeOpenTelemetry() {
    OpenTelemetrySdk sdk =
        OpenTelemetrySdk.builder()
            .setTracerProvider(SdkTracerProvider.builder().setSampler(Sampler.alwaysOn()).build())
            .setLoggerProvider(SdkLoggerProvider.builder().build())
            .build();

    GlobalOpenTelemetry.set(sdk);
    // Add hook to close SDK, which flushes logs
    Runtime.getRuntime().addShutdownHook(new Thread(sdk::close));
  }
}
