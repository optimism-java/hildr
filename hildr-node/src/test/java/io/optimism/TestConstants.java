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

package io.optimism;

import io.optimism.config.Config;
import java.util.Map;

/**
 * The type Test constants.
 *
 * @author thinkAfCod
 * @since 2023.06
 */
public class TestConstants {

  private TestConstants() {}

  /** The constant isConfiguredApiKeyEnv. */
  public static boolean isConfiguredApiKeyEnv = false;

  private static final String ETH_API_ENV = "ETH_API_KEY";
  private static final String OPT_API_ENV = "OPT_API_KEY";

  /** The L 1 rpc url format. */
  static String l1RpcUrlFormat = "https://eth-goerli.g.alchemy.com/v2/%s";

  /** The L 2 rpc url format. */
  static String l2RpcUrlFormat = "https://opt-goerli.g.alchemy.com/v2/%s";

  /**
   * Create config config.
   *
   * @return the config
   */
  public static Config createConfig() {
    Map<String, String> envs = System.getenv();
    isConfiguredApiKeyEnv = envs.containsKey(ETH_API_ENV) && envs.containsKey(OPT_API_ENV);
    if (!isConfiguredApiKeyEnv) {
      return null;
    }
    var l1RpcUrl = l1RpcUrlFormat.formatted(envs.get(ETH_API_ENV));
    var l2RpcUrl = l2RpcUrlFormat.formatted(envs.get(OPT_API_ENV));
    Config.CliConfig cliConfig =
        new Config.CliConfig(l1RpcUrl, l2RpcUrl, null, "testjwt", null, null, false);
    return Config.create(null, cliConfig, Config.ChainConfig.optimismGoerli());
  }
}
