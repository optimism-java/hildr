package io.optimism.rpc.methods;

import io.optimism.exceptions.HildrServiceExecutionException;
import io.optimism.rpc.internal.JsonRpcRequestContext;
import io.optimism.rpc.internal.response.JsonRpcResponse;
import io.optimism.rpc.internal.response.JsonRpcSuccessResponse;
import java.util.function.Function;

/**
 * The JsonRpcMethodAdapter type.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public class JsonRpcMethodAdapter implements JsonRpcMethod {

    private final String name;

    private final Function fn;

    /**
     * JsonRpcMethodAdapter type constructor.
     *
     * @param name json rpc method name
     * @param responseSupplier response result supplier
     */
    public JsonRpcMethodAdapter(String name, Function responseSupplier) {
        this.name = name;
        this.fn = responseSupplier;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public JsonRpcResponse response(JsonRpcRequestContext ctx) {
        try {
            var result = fn.apply(ctx.getRequest().getParams());
            if (!(result instanceof JsonRpcResponse)) {
                return new JsonRpcSuccessResponse(ctx.getRequest().getId(), result);
            }
            return (JsonRpcResponse) result;
        } catch (Exception e) {
            Thread.currentThread().interrupt();
            throw new HildrServiceExecutionException(e);
        }
    }
}
