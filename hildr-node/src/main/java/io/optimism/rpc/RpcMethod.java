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

package io.optimism.rpc;

import java.util.HashSet;

/**
 * method handler of rpc.
 *
 * @author thinkAfCod
 * @since 2023.06
 */
public enum RpcMethod {

  /** optimism_outputAtBlock api. */
  OP_OUTPUT_AT_BLOCK("optimism_outputAtBlock"),
  /** optimism_syncStatus api */
  OP_SYNC_STATUS("optimism_syncStatus"),
  /** optimism_rollupConfig api */
  OP_ROLLUP_CONFIG("optimism_rollupConfig");

  private final String rpcMethodName;

  private static final HashSet<String> allMethodNames;

  static {
    allMethodNames = new HashSet<>();
    for (RpcMethod m : RpcMethod.values()) {
      allMethodNames.add(m.getRpcMethodName());
    }
  }

  RpcMethod(String rpcMethodName) {
    this.rpcMethodName = rpcMethodName;
  }

  /**
   * Gets rpc method name.
   *
   * @return the rpc method name
   */
  public String getRpcMethodName() {
    return rpcMethodName;
  }

  /**
   * Rpc method exists boolean.
   *
   * @param rpcMethodName the rpc method name
   * @return the boolean
   */
  public static boolean rpcMethodExists(final String rpcMethodName) {
    return allMethodNames.contains(rpcMethodName);
  }
}
