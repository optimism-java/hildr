package io.optimism.rpc.methods;

import io.optimism.rpc.internal.JsonRpcRequestContext;
import io.optimism.rpc.internal.response.JsonRpcResponse;

/**
 * jsonrpc handle method interface. base on besu
 *
 * @author thinkAfCod
 * @since 2023.06
 */
public interface JsonRpcMethod {

    /**
     * Standardized JSON-RPC method name.
     *
     * @return identification of the JSON-RPC method.
     */
    String getName();

    /**
     * Applies the method to given request.
     *
     * @param request input data for the JSON-RPC method.
     * @return output from applying the JSON-RPC method to the input.
     */
    JsonRpcResponse response(JsonRpcRequestContext request);
}
