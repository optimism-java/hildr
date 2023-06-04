/*
 * Copyright 2023 281165273grape@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package io.optimism.rpc.execution;

import static io.optimism.rpc.internal.response.JsonRpcError.INVALID_REQUEST;

import io.optimism.rpc.RpcMethod;
import io.optimism.rpc.internal.JsonRpcRequest;
import io.optimism.rpc.internal.JsonRpcRequestContext;
import io.optimism.rpc.internal.JsonRpcRequestId;
import io.optimism.rpc.internal.response.JsonRpcError;
import io.optimism.rpc.internal.response.JsonRpcErrorResponse;
import io.optimism.rpc.internal.response.JsonRpcNoResponse;
import io.optimism.rpc.internal.response.JsonRpcResponse;
import io.optimism.rpc.methods.JsonRpcMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** base on besu. */
public class JsonRpcExecutor {

  private static final Logger LOG = LoggerFactory.getLogger(JsonRpcExecutor.class);

  private final JsonRpcProcessor rpcProcessor;
  private final Map<String, JsonRpcMethod> rpcMethods;

  public JsonRpcExecutor(
      final JsonRpcProcessor rpcProcessor, final Map<String, JsonRpcMethod> rpcMethods) {
    this.rpcProcessor = rpcProcessor;
    this.rpcMethods = rpcMethods;
  }

  public JsonRpcResponse execute(JsonRpcRequestContext context) {
    JsonRpcRequest requestBody = context.getRequest();
    try {
      final JsonRpcRequestId id = new JsonRpcRequestId(requestBody.getId());
      // Handle notifications
      if (requestBody.isNotification()) {
        // Notifications aren't handled so create empty result for now.
        return new JsonRpcNoResponse();
      }

      final Optional<JsonRpcError> unavailableMethod = validateMethodAvailability(requestBody);
      if (unavailableMethod.isPresent()) {
        return new JsonRpcErrorResponse(id, unavailableMethod.get());
      }

      final JsonRpcMethod method = rpcMethods.get(requestBody.getMethod());
      return rpcProcessor.process(method, context);
    } catch (final IllegalArgumentException e) {
      try {
        return new JsonRpcErrorResponse(requestBody.getId(), INVALID_REQUEST);
      } catch (final ClassCastException idNotIntegerException) {
        return new JsonRpcErrorResponse(null, INVALID_REQUEST);
      }
    }
  }

  private Optional<JsonRpcError> validateMethodAvailability(final JsonRpcRequest request) {
    final String name = request.getMethod();

    if (LOG.isDebugEnabled()) {
      final JsonArray params = JsonObject.mapFrom(request).getJsonArray("params");
      LOG.debug("JSON-RPC request -> {} {}", name, params);
    }

    final JsonRpcMethod method = rpcMethods.get(name);

    if (method == null) {
      if (!RpcMethod.rpcMethodExists(name)) {
        return Optional.of(JsonRpcError.METHOD_NOT_FOUND);
      }
      if (!rpcMethods.containsKey(name)) {
        return Optional.of(JsonRpcError.METHOD_NOT_ENABLED);
      }
    }

    return Optional.empty();
  }
}
