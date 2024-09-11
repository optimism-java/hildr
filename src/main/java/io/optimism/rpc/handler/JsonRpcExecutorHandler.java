package io.optimism.rpc.handler;

import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.optimism.rpc.execution.JsonRpcExecutor;
import io.optimism.rpc.execution.JsonRpcProcessor;
import io.optimism.rpc.internal.JsonRpcRequest;
import io.optimism.rpc.internal.JsonRpcRequestContext;
import io.optimism.rpc.internal.response.JsonRpcErrorResponse;
import io.optimism.rpc.internal.response.JsonRpcResponse;
import io.optimism.rpc.methods.JsonRpcMethod;
import io.optimism.types.enums.JsonRpcError;
import io.optimism.types.enums.JsonRpcResponseType;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import java.io.IOException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * base on besu.
 *
 * @author thinkAfCod
 * @since 2023.06
 */
public class JsonRpcExecutorHandler {

    private static final Logger LOG = LoggerFactory.getLogger(JsonRpcExecutorHandler.class);

    private static final ObjectMapper mapper = new ObjectMapper();

    private JsonRpcExecutorHandler() {}

    /**
     * Handler handler.
     *
     * @param processor the processor
     * @param methods the methods
     * @return the handler
     */
    public static Handler<RoutingContext> handler(JsonRpcProcessor processor, Map<String, JsonRpcMethod> methods) {
        final JsonRpcExecutor jsonRpcExecutor = new JsonRpcExecutor(processor, methods);
        return ctx -> {
            try {
                if (!isJsonObjectRequest(ctx)) {
                    handleJsonRpcError(ctx, null, JsonRpcError.PARSE_ERROR);
                    return;
                }
                JsonObject req = ctx.get("REQUEST_BODY_AS_JSON_OBJECT");
                try {
                    JsonRpcRequest jsonRpcRequest = req.mapTo(JsonRpcRequest.class);
                    JsonRpcResponse jsonRpcResponse =
                            jsonRpcExecutor.execute(new JsonRpcRequestContext(ctx, jsonRpcRequest));

                    HttpServerResponse response = ctx.response();
                    response = response.putHeader("Content-Type", APPLICATION_JSON);
                    handleJsonObjectResponse(response, jsonRpcResponse);
                } catch (IOException e) {
                    final String method = req.getString("method");
                    LOG.error("{} - Error streaming JSON-RPC response", method, e);
                    throw new RuntimeException(e);
                }
            } catch (final RuntimeException e) {
                handleJsonRpcError(ctx, null, JsonRpcError.INTERNAL_ERROR);
            }
        };
    }

    private static boolean isJsonObjectRequest(final RoutingContext ctx) {
        return ctx.data().containsKey("REQUEST_BODY_AS_JSON_OBJECT");
    }

    private static void handleJsonRpcError(
            final RoutingContext routingContext, final Object id, final JsonRpcError error) {
        final HttpServerResponse response = routingContext.response();
        if (!response.closed()) {
            response.setStatusCode(statusCodeFromError(error).code())
                    .end(Json.encode(new JsonRpcErrorResponse(id, error)));
        }
    }

    private static HttpResponseStatus statusCodeFromError(final JsonRpcError error) {
        return switch (error) {
            case INVALID_REQUEST, PARSE_ERROR -> HttpResponseStatus.BAD_REQUEST;
            default -> HttpResponseStatus.OK;
        };
    }

    private static HttpResponseStatus status(final JsonRpcResponse response) {
        return switch (response.getType()) {
            case UNAUTHORIZED -> HttpResponseStatus.UNAUTHORIZED;
            case ERROR -> statusCodeFromError(((JsonRpcErrorResponse) response).getError());
            default -> HttpResponseStatus.OK;
        };
    }

    private static void handleJsonObjectResponse(
            final HttpServerResponse response, final JsonRpcResponse jsonRpcResponse) throws IOException {

        response.setStatusCode(status(jsonRpcResponse).code());
        if (jsonRpcResponse.getType() == JsonRpcResponseType.NONE) {
            response.end();
        } else {
            response.end(mapper.writeValueAsString(jsonRpcResponse));
        }
    }
}
