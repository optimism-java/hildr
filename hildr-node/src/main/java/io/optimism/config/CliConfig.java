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

package io.optimism.config;


import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * The type Cli config.
 *
 * @param l1RpcUrl L1 chain rpc url.
 * @param l2RpcUrl L2 chain rpc url.
 * @param l2EngineUrl L2 engine API url.
 * @param jwtSecret L2 engine API jwt secret.
 */
public record CliConfig(String l1RpcUrl, String l2RpcUrl, String l2EngineUrl, String jwtSecret) {

  /**
   * To configMap.
   *
   * @return the map
   */
  public Map<String, String> toConfigMap() {
    Map<String, String> map = new HashMap<>();
    if (StringUtils.isNotEmpty(l1RpcUrl)) {
      map.put("config.l1RpcUrl", l1RpcUrl);
    }
    if (StringUtils.isNotEmpty(l2RpcUrl)) {
      map.put("config.l2RpcUrl", l2RpcUrl);
    }
    if (StringUtils.isNotEmpty(l2EngineUrl)) {
      map.put("config.l2EngineUrl", l2EngineUrl);
    }
    if (StringUtils.isNotEmpty(jwtSecret)) {
      map.put("config.jwtSecret", jwtSecret);
    }
    return map;
  }
}
