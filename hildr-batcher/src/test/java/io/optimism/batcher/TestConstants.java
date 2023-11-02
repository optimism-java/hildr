/*
 * Copyright 2023 q315xia@163.com
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

package io.optimism.batcher;

import java.util.Map;

/**
 * The type Test constants.
 *
 * @author thinkAfCod
 * @since 2023.06
 */
public class TestConstants {

  /** The constant isConfiguredApiKeyEnv. */
  public static boolean isConfiguredApiKeyEnv = false;

  private static final String ETH_API_ENV = "ETH_API_KEY";
  private static final String OPT_API_ENV = "OPT_API_KEY";
  private static final String ROLLUP_API_KEY = "ROLLUP_API_KEY";

  /** The L 1 rpc url format. */
  static String l1RpcUrlFormat = "https://eth-goerli.g.alchemy.com/v2/%s";

  /** The L 2 rpc url format. */
  static String l2RpcUrlFormat = "https://opt-goerli.g.alchemy.com/v2/%s";

  public static String l1RpcUrl;

  public static String l2RpcUrl;

  public static String rollupRpcUrl;

  static {
    Map<String, String> envs = System.getenv();
    isConfiguredApiKeyEnv = envs.containsKey(ETH_API_ENV) && envs.containsKey(OPT_API_ENV);
    l1RpcUrl = l1RpcUrlFormat.formatted(envs.get(ETH_API_ENV));
    l2RpcUrl = l2RpcUrlFormat.formatted(envs.get(OPT_API_ENV));
    rollupRpcUrl = l2RpcUrlFormat.formatted(envs.get(ROLLUP_API_KEY));
  }

  private TestConstants() {}
}
