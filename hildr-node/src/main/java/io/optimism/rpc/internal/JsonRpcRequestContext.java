package io.optimism.rpc.internal;

import io.vertx.ext.web.RoutingContext;
import java.util.Objects;

/**
 * json rpc request context.
 *
 * @author thinkAfCod
 * @since 2023.06
 */
@SuppressWarnings("UnusedVariable")
public class JsonRpcRequestContext {

    private final RoutingContext context;

    private final JsonRpcRequest jsonRpcRequest;

    /**
     * Instantiates a new Json rpc request context.
     *
     * @param context the context
     * @param jsonRpcRequest the json rpc request
     */
    public JsonRpcRequestContext(final RoutingContext context, final JsonRpcRequest jsonRpcRequest) {
        this.context = context;
        this.jsonRpcRequest = jsonRpcRequest;
    }

    /**
     * Gets request.
     *
     * @return the request
     */
    public JsonRpcRequest getRequest() {
        return jsonRpcRequest;
    }

    /**
     * Gets parameter.
     *
     * @param <T> the type parameter
     * @param index the index
     * @param paramClass the param class
     * @return the parameter
     */
    public <T> T getParameter(final int index, final Class<T> paramClass) {
        return jsonRpcRequest.getParameter(index, paramClass);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JsonRpcRequestContext that)) return false;
        return Objects.equals(context, that.context) && Objects.equals(jsonRpcRequest, that.jsonRpcRequest);
    }

    @Override
    public int hashCode() {
        return Objects.hash(context, jsonRpcRequest);
    }
}
