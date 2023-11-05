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
import io.optimism.rpc.internal.response.JsonRpcErrorResponse;
import io.optimism.rpc.internal.response.JsonRpcResponse;
import io.optimism.rpc.internal.response.JsonRpcResponseType;
import io.optimism.rpc.methods.JsonRpcMethod;
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
