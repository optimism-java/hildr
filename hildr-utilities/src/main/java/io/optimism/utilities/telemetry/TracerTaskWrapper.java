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

package io.optimism.utilities.telemetry;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * The type TracerTaskWrapper.
 *
 * @author thinkAfCod
 * @since 0.1.0
 */
public class TracerTaskWrapper {

  private static String DEFAULT_TRACER_NAME = "structure-task-scope";

  private static Function<String, Tracer> tracerSupplier;

  /** Instantiates a new Tracer task wrapper. */
  private TracerTaskWrapper() {}

  public static void setTracerSupplier(Function<String, Tracer> supplier) {
    TracerTaskWrapper.tracerSupplier = supplier;
  }

  /**
   * Wrap callable. It Will use default tracer name for tracer.
   *
   * @param <T> the type parameter
   * @param call the call
   * @return the callable
   */
  public static <T> Callable<T> wrap(Callable<T> call) {
    return TracerTaskWrapper.wrap(DEFAULT_TRACER_NAME, call);
  }

  /**
   * Wrap callable.
   *
   * @param <T> the type parameter
   * @param call the call
   * @return the callable
   */
  public static <T> Callable<T> wrap(final String tracerName, final Callable<T> call) {
    return () -> {
      Tracer tracer = TracerTaskWrapper.tracerSupplier.apply(tracerName);
      Span span = tracer.nextSpan().name("call").start();
      try (var ignored = tracer.withSpan(span)) {
        return call.call();
      } finally {
        span.end();
      }
    };
  }
}
