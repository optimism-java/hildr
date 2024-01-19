/*
 * Copyright 2023 q315xia@163.com
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

import com.sun.net.httpserver.HttpServer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * The MetricsServer type.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public class MetricsServer {

    private static PrometheusMeterRegistry registry;
    private static HttpServer httpServer;
    private static volatile Future<?> serverFuture;
    private static Semaphore semaphore = new Semaphore(1);

    private MetricsServer() {}

    /**
     * Create prometheus registry instance.
     *
     * @return prometheus registry instance
     */
    public static MeterRegistry createPrometheusRegistry() {
        return new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
    }

    /**
     * start a http server for prometheus to access.
     *
     * @param registry prometheus registry instance
     * @param port     custom http server port
     */
    public static void start(MeterRegistry registry, int port) {
        if (!(registry instanceof PrometheusMeterRegistry)) {
            throw new IllegalArgumentException("Registry type must be PrometheusMeterRegistry but not");
        }
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            if (!semaphore.tryAcquire(5, TimeUnit.SECONDS)) {
                throw new MetricsServerException("can not acquire metrics server resource");
            }
            if (serverFuture != null) {
                throw new MetricsServerException("metrics server has been already started");
            }
            MetricsServer.registry = (PrometheusMeterRegistry) registry;
            httpServer = HttpServer.create(new InetSocketAddress(port), 2);
            httpServer.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
            httpServer.createContext("/metrics", httpExchange -> {
                String response = MetricsServer.registry.scrape();
                httpExchange.sendResponseHeaders(200, response.getBytes(Charset.defaultCharset()).length);
                try (OutputStream os = httpExchange.getResponseBody()) {
                    os.write(response.getBytes(Charset.defaultCharset()));
                }
            });
            serverFuture = executor.submit(httpServer::start);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MetricsServerException("can not acquire metrics server resource", e);
        } catch (IOException e) {
            throw new MetricsServerException("create metrics server failed", e);
        } finally {
            semaphore.release();
        }
    }

    /**
     * stop the http server.
     */
    public static void stop() {
        if (!isActive()) {
            return;
        }
        try {
            if (!semaphore.tryAcquire(10, TimeUnit.SECONDS)) {
                throw new MetricsServerException("can not acquire metrics server resource");
            }
            if (serverFuture != null) {
                serverFuture.cancel(true);
            } else {
                throw new MetricsServerException("metrics server is stopped or never start");
            }
            if (httpServer != null) {
                httpServer.stop(5);
            }
            serverFuture = null;
            httpServer = null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MetricsServerException("can not acquire metrics server resource", e);
        } finally {
            semaphore.release();
        }
    }

    /**
     * Is active boolean.
     *
     * @return the boolean
     */
    public static boolean isActive() {
        return serverFuture != null && !(serverFuture.isDone() || serverFuture.isCancelled());
    }
}
