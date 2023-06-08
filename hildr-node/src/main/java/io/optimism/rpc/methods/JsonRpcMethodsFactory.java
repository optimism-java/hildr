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

import io.optimism.config.Config;
import java.util.HashMap;
import java.util.Map;

/**
 * JsonRpc method factory. copied from besu.
 *
 * @author thinkAfCod
 * @since 2023.06
 */
public class JsonRpcMethodsFactory {

  /** JsonRpcMethodsFactory constructor. */
  public JsonRpcMethodsFactory() {}

  /**
   * Methods map.
   *
   * @param config the config
   * @return the map
   */
  public Map<String, JsonRpcMethod> methods(Config config) {
    final Map<String, JsonRpcMethod> methods = new HashMap<>();
    JsonRpcMethod outputAtBlock =
        new OutputAtBlock(config.l2RpcUrl(), config.chainConfig().l2Tol1MessagePasser());

    methods.put(outputAtBlock.getName(), outputAtBlock);

    return methods;
  }
}
