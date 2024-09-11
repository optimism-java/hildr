package io.optimism.telemetry;

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

    private static Function<String, Tracer> tracerSupplier;

    /**
     * Instantiates a new Tracer task wrapper.
     */
    private TracerTaskWrapper() {}

    /**
     * Sets tracer supplier.
     *
     * @param supplier the supplier
     */
    public static void setTracerSupplier(Function<String, Tracer> supplier) {
        TracerTaskWrapper.tracerSupplier = supplier;
    }

    /**
     * Wrap callable. It Will use default tracer name for tracer.
     *
     * @param <T>  the type parameter
     * @param call the call
     * @return the callable
     */
    public static <T> Callable<T> wrap(Callable<T> call) {
        String DEFAULT_TRACER_NAME = "structure-task-scope";
        return TracerTaskWrapper.wrap(DEFAULT_TRACER_NAME, call);
    }

    /**
     * Wrap callable.
     *
     * @param <T>        the type parameter
     * @param tracerName the tracer name
     * @param call       the call
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
