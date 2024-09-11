package io.optimism.telemetry;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.optimism.types.enums.Logging;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logging trace use example.
 *
 * @author thinkAfCod
 * @since 2023.05
 */
@SuppressWarnings("UnusedVariable")
public class LoggingExampleTest {
    private static final Logger logger = LoggerFactory.getLogger(LoggingExampleTest.class);

    @Test
    void testLogging() throws InterruptedException {

        final Thread[] threads = new Thread[10];
        for (int i = 0; i < 5; i++) {
            final int logId = i;
            Thread thread = new Thread(() -> {
                Tracer tracer = Logging.INSTANCE.getTracer("unit-test-case");
                Span span = tracer.nextSpan().name("my-span").start();
                logger.info("step 1:parent {} log", logId);
                try (var unusedScope1 = tracer.withSpan(span)) {
                    logger.info("step 2:parent {} log", logId);
                    Span childSpan = tracer.nextSpan().name("childSpan").start();
                    try (var unusedBag2 = tracer.withSpan(childSpan)) {
                        logger.info("step 3:parent {} log", logId);
                        throw new RuntimeException("test error for span");
                    } catch (Exception e) {
                        logger.error("catch a throw in scope", e);
                    } finally {
                        childSpan.end();
                    }
                } catch (Exception e) {
                    logger.error("catch a throw", e);
                } finally {
                    span.end();
                }
            });
            threads[i] = thread;
            thread.start();
        }
        for (Thread thread : threads) {
            try {
                if (thread != null) {
                    thread.join();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
