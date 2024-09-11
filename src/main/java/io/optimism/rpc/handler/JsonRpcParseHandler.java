package io.optimism.rpc.handler;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.optimism.rpc.internal.response.JsonRpcErrorResponse;
import io.optimism.types.enums.JsonRpcError;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

/**
 * copied from besu.
 *
 * @author thinkAfCod
 * @since 2023.06
 */
public class JsonRpcParseHandler {

    private JsonRpcParseHandler() {}

    /**
     * Handler handler.
     *
     * @return the handler
     */
    public static Handler<RoutingContext> handler() {
        return ctx -> {
            final HttpServerResponse response = ctx.response();
            var body = ctx.body();
            if (body == null) {
                errorResponse(response, JsonRpcError.PARSE_ERROR);
            } else {
                try {
                    ctx.put("REQUEST_BODY_AS_JSON_OBJECT", body.asJsonObject());
                } catch (DecodeException | ClassCastException jsonObjectDecodeException) {
                    errorResponse(response, JsonRpcError.PARSE_ERROR);
                }
                ctx.next();
            }
        };
    }

    private static void errorResponse(final HttpServerResponse response, final JsonRpcError rpcError) {
        if (!response.closed()) {
            response.setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                    .end(Json.encode(new JsonRpcErrorResponse(null, rpcError)));
        }
    }
}
