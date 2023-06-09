package io.optimism.concurrency;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.optimism.telemetry.Logging;
import java.util.concurrent.Callable;

/**
 * @author thinkAfCod
 * @since 2023.06
 */
public class TracerTaskWrapper {

  public static final <T> Callable<T> wrap(Callable<T> call) {
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
