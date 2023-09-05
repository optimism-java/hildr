/*
 * Copyright ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package io.optimism.rpc.execution;

import io.optimism.rpc.internal.JsonRpcRequestContext;
import io.optimism.rpc.internal.response.JsonRpcError;
import io.optimism.rpc.internal.response.JsonRpcErrorResponse;
import io.optimism.rpc.internal.response.JsonRpcResponse;
import io.optimism.rpc.methods.JsonRpcMethod;
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
