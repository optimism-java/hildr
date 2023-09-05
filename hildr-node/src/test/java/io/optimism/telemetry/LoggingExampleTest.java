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

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
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
