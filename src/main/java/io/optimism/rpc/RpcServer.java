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
import io.optimism.rpc.methods.JsonRpcMethodAdapter;
import io.optimism.rpc.methods.JsonRpcMethodsFactory;
import io.optimism.types.enums.Logging;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpConnection;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
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

    /**
     * Instantiates a new Rpc server.
     *
     * @param config the config
     */
    public RpcServer(final Config config) {
        this.config = config;
        this.activeConnectionsCount = new AtomicInteger();
        this.maxActiveConnections = DEFAULT_MAX_ACTIVE_CONNECTIONS;
        this.vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(20));
        this.methods = new JsonRpcMethodsFactory().methods(this.config);
    }

    /** Start. */
    public void start() {
        this.httpServer = vertx.createHttpServer(getHttpServerOptions(config));
        httpServer.connectionHandler(connectionHandler());

        CompletableFuture<Void> future = new CompletableFuture<>();
        httpServer.requestHandler(buildRouter()).listen(res -> {
            if (!res.failed()) {
                logger.info("rpc server started at port {}", httpServer.actualPort());
                future.complete(null);
                return;
            }
            future.completeExceptionally(res.cause());
            httpServer = null;
        });
        try {
            future.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Handler<HttpServerRequest> buildRouter() {
        var router = Router.router(this.vertx);
        router.route().handler(BodyHandler.create()).handler(context -> {
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
                JsonRpcExecutorHandler.handler(new LoggedJsonRpcProcessor(new BaseJsonRpcProcessor()), methods));
        return router;
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
            connection.closeHandler(c -> logger.debug(
                    "Connection closed from {}. Total of active connections: {}/{}",
                    connection.remoteAddress(),
                    activeConnectionsCount.decrementAndGet(),
                    maxActiveConnections));
        };
    }

    private HttpServerOptions getHttpServerOptions(final Config config) {
        final HttpServerOptions httpServerOptions = new HttpServerOptions()
                .setHost(config.rpcAddr())
                .setPort(config.rpcPort())
                .setHandle100ContinueAutomatically(true)
                .setCompressionSupported(true);

        httpServerOptions.setMaxWebSocketFrameSize(1024 * 1024);
        httpServerOptions.setMaxWebSocketMessageSize(1024 * 1024 * 4);
        return httpServerOptions;
    }

    /** Stop. */
    public void stop() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        httpServer.close(res -> {
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

    /**
     * register method of handler to rpc server after start.
     *
     * @param methods map of json rpc handler
     */
    public void register(Map<String, Function> methods) {
        if (methods == null || methods.isEmpty()) {
            return;
        }
        methods.forEach((name, fn) -> {
            logger.info("put json rpc method into processor: name({})", name);
            this.methods.putIfAbsent(name, new JsonRpcMethodAdapter(name, fn));
        });
    }
}
