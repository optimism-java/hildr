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

package io.optimism.concurrency;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.optimism.telemetry.Logging;
import java.util.concurrent.Callable;

/**
 * The type TracerTaskWrapper.
 *
 * @author thinkAfCod
 * @since 0.1.0
 */
public class TracerTaskWrapper {

  /** Instantiates a new Tracer task wrapper. */
  public TracerTaskWrapper() {}

  /**
   * Wrap callable.
   *
   * @param <T> the type parameter
   * @param call the call
   * @return the callable
   */
  public static <T> Callable<T> wrap(Callable<T> call) {
    return () -> {
      Tracer tracer = Logging.INSTANCE.getTracer("structure-task-scope");
      Span span = tracer.nextSpan().name("call").start();
      try (var spanInScope = tracer.withSpan(span)) {
        return call.call();
      } finally {
        span.end();
      }
    };
  }
}
