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
