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

package io.optimism.rpc;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.optimism.config.Config;
import io.optimism.rpc.execution.BaseJsonRpcProcessor;
import io.optimism.rpc.execution.LoggedJsonRpcProcessor;
import io.optimism.rpc.handler.JsonRpcExecutorHandler;
import io.optimism.rpc.handler.JsonRpcParseHandler;
import io.optimism.rpc.handler.TimeoutHandler;
import io.optimism.rpc.methods.JsonRpcMethod;
import io.optimism.rpc.methods.JsonRpcMethodsFactory;
import io.optimism.telemetry.Logging;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpConnection;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * rpc server.
 *
 * @author thinkAfCod
 * @since 2023.06
 */
public class RpcServer {

  private static final Logger logger = LoggerFactory.getLogger(RpcServer.class);
  private static final int DEFAULT_MAX_ACTIVE_CONNECTIONS = 80;

  private final Config config;

  private final Vertx vertx;

  private final Map<String, JsonRpcMethod> methods;

  private HttpServer httpServer;

  private AtomicInteger activeConnectionsCount;
  private int maxActiveConnections;

  public RpcServer(final Config config) {
    this.config = config;
    this.activeConnectionsCount = new AtomicInteger();
    this.maxActiveConnections = DEFAULT_MAX_ACTIVE_CONNECTIONS;
    this.vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(1));
    this.methods = new JsonRpcMethodsFactory().methods(this.config);
  }

  public void start() throws Exception {
    this.httpServer = vertx.createHttpServer(getHttpServerOptions(config));
    httpServer.webSocketHandler(webSocketHandler());
    httpServer.connectionHandler(connectionHandler());

    CompletableFuture<Void> future = new CompletableFuture<>();
    httpServer
        .requestHandler(buildRouter())
        .listen(
            res -> {
              if (!res.failed()) {
                logger.info("rpc server started at port {}", httpServer.actualPort());
                future.complete(null);
                return;
              }
              future.completeExceptionally(res.cause());
              httpServer = null;
            });
    future.get();
  }

  private Handler<HttpServerRequest> buildRouter() {
    var router = Router.router(this.vertx);
    router
        .route()
        .handler(
            context -> {
              Tracer tracer = Logging.INSTANCE.getTracer("jsonrpc-server");
              Span span = tracer.nextSpan().name("requestHandle").start();
              context.put("CTX_TRACE", tracer);
              context.put("CTX_SPAN", span);
              try (var unused = tracer.withSpan(span)) {
                context.next();
              } finally {
                span.end();
              }
            });
    Route mainRoute = router.route("/").produces("application/json");
    mainRoute.blockingHandler(JsonRpcParseHandler.handler());
    mainRoute.blockingHandler(TimeoutHandler.handler());
    mainRoute.handler(
        JsonRpcExecutorHandler.handler(
            new LoggedJsonRpcProcessor(new BaseJsonRpcProcessor()), methods));
    return null;
  }

  private Handler<HttpConnection> connectionHandler() {
    return connection -> {
      if (activeConnectionsCount.get() >= maxActiveConnections) {
        // disallow new connections to prevent DoS
        logger.warn(
            "Rejecting new connection from {}. Max {} active connections limit reached.",
            connection.remoteAddress(),
            activeConnectionsCount.getAndIncrement());
        connection.close();
      } else {
        logger.debug(
            "Opened connection from {}. Total of active connections: {}/{}",
            connection.remoteAddress(),
            activeConnectionsCount.incrementAndGet(),
            maxActiveConnections);
      }
      connection.closeHandler(
          c ->
              logger.debug(
                  "Connection closed from {}. Total of active connections: {}/{}",
                  connection.remoteAddress(),
                  activeConnectionsCount.decrementAndGet(),
                  maxActiveConnections));
    };
  }

  private Handler<ServerWebSocket> webSocketHandler() {
    Handler<ServerWebSocket> o = null;
    return o;
  }

  private HttpServerOptions getHttpServerOptions(final Config config) {
    final HttpServerOptions httpServerOptions =
        new HttpServerOptions()
            .setHost("127.0.0.1")
            .setPort(config.rpcPort())
            .setHandle100ContinueAutomatically(true)
            .setCompressionSupported(true);

    httpServerOptions.setMaxWebSocketFrameSize(1024 * 1024);
    httpServerOptions.setMaxWebSocketMessageSize(1024 * 1024 * 4);
    return httpServerOptions;
  }

  public void stop() {
    CompletableFuture<Void> future = new CompletableFuture<>();
    httpServer.close(
        res -> {
          if (res.failed()) {
            future.completeExceptionally(res.cause());
          } else {
            httpServer = null;
            future.complete(null);
          }
        });
    try {
      future.get();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
