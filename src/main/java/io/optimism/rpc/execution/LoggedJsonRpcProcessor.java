package io.optimism.rpc.execution;

import io.optimism.rpc.internal.JsonRpcRequestContext;
import io.optimism.rpc.internal.response.JsonRpcErrorResponse;
import io.optimism.rpc.internal.response.JsonRpcResponse;
import io.optimism.rpc.methods.JsonRpcMethod;
import io.optimism.types.enums.JsonRpcResponseType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * LoggedJsonRpcProcessor.
 *
 * @author thinkAfCod
 * @since 2023.06
 */
public class LoggedJsonRpcProcessor implements JsonRpcProcessor {

    private static final Logger logger = LoggerFactory.getLogger(LoggedJsonRpcProcessor.class);

    private final JsonRpcProcessor rpcProcessor;

    /**
     * Instantiates a new Logged json rpc processor.
     *
     * @param rpcProcessor the rpc processor
     */
    public LoggedJsonRpcProcessor(final JsonRpcProcessor rpcProcessor) {
        this.rpcProcessor = rpcProcessor;
    }

    @Override
    public JsonRpcResponse process(final JsonRpcMethod method, final JsonRpcRequestContext context) {
        JsonRpcResponse jsonRpcResponse = rpcProcessor.process(method, context);
        if (JsonRpcResponseType.ERROR == jsonRpcResponse.getType()) {
            JsonRpcErrorResponse errorResponse = (JsonRpcErrorResponse) jsonRpcResponse;
            switch (errorResponse.getError()) {
                case INVALID_PARAMS -> logger.info("jsonrpc has error: {}", "Invalid Params");
                case UNAUTHORIZED -> logger.info("jsonrpc has error: {}", "Unauthorized");
                case INTERNAL_ERROR -> logger.info("jsonrpc has error: {}", "Error processing JSON-RPC requestBody");
                default -> logger.info("jsonrpc has error: {}", "Unexpected error");
            }
        }
        return jsonRpcResponse;
    }
}
