package io.optimism.rpc.execution;

import io.optimism.rpc.internal.JsonRpcRequestContext;
import io.optimism.rpc.internal.response.JsonRpcResponse;
import io.optimism.rpc.methods.JsonRpcMethod;

/**
 * JsonRpcProcessor interface.
 *
 * @author thinkAfCod
 * @since 2023.06
 */
public interface JsonRpcProcessor {

    /**
     * process jsonRpcMethod with JsonRpcRequestContext.
     *
     * @param method JsonRpcMethod instant
     * @param request JsonRpcRequestContext instant
     * @return json rpc process response
     */
    JsonRpcResponse process(final JsonRpcMethod method, final JsonRpcRequestContext request);
}
