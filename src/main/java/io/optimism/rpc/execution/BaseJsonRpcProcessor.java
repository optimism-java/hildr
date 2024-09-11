package io.optimism.rpc.execution;

import io.optimism.rpc.internal.JsonRpcRequestContext;
import io.optimism.rpc.internal.response.JsonRpcErrorResponse;
import io.optimism.rpc.internal.response.JsonRpcResponse;
import io.optimism.rpc.methods.JsonRpcMethod;
import io.optimism.types.enums.JsonRpcError;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BaseJsonRpcProcessor
 *
 * @author thinkAfCod
 * @since 2023.06
 */
public class BaseJsonRpcProcessor implements JsonRpcProcessor {

    private static final Logger logger = LoggerFactory.getLogger(BaseJsonRpcProcessor.class);

    /** BaseJsonRpcProcessor constructor. */
    public BaseJsonRpcProcessor() {}

    @Override
    public JsonRpcResponse process(JsonRpcMethod method, JsonRpcRequestContext request) {
        try {
            return method.response(request);
        } catch (final RuntimeException e) {
            final JsonArray params = JsonObject.mapFrom(request.getRequest()).getJsonArray("params");
            logger.error(String.format("Error processing method: %s %s", method.getName(), params), e);
            return new JsonRpcErrorResponse(request.getRequest().getId(), JsonRpcError.INTERNAL_ERROR);
        }
    }
}
