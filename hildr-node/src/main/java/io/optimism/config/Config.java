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


import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.loader.EnvironmentVarsLoader;
import org.github.gestalt.config.loader.MapConfigLoader;
import org.github.gestalt.config.loader.PropertyLoader;
import org.github.gestalt.config.source.MapConfigSource;
import org.github.gestalt.config.toml.TomlLoader;

/**
 * The type Config.
 *
 * @param l1RpcUrl L1 chain rpc url.
 * @param l2RpcUrl L2 chain rpc url.
 * @param l2EngineUrl L2 engine API url.
 * @param jwtSecret L2 engine API jwt secret.
 * @param chainConfig The chain config.
 * @author grapebaba
 * @since 0.1.0
 */
public record Config(
    String l1RpcUrl,
    String l2RpcUrl,
    String l2EngineUrl,
    String jwtSecret,
    ChainConfig chainConfig) {

  /**
   * Create Config.
   *
   * @param configPath the config path
   * @param cliConfig the cli config
   * @param chainConfig the chain config
   * @return the config
   */
  public static Config create(Path configPath, CliConfig cliConfig, ChainConfig chainConfig) {

    try {
      EnvironmentVarsLoader environmentVarsLoader = new EnvironmentVarsLoader();
      MapConfigLoader mapConfigLoader = new MapConfigLoader();
      TomlLoader tomlLoader = new TomlLoader();
      PropertyLoader propertyLoader = new PropertyLoader();

      Map<String, String> defaultProvider = new HashMap<>();
      defaultProvider.put("config.l2RpcUrl", "http://127.0.0.1:8545");
      defaultProvider.put("config.l2EngineUrl", "http://127.0.0.1:8551");
      defaultProvider.put("config.l1RpcUrl", "");
      defaultProvider.put("config.jwtSecret", "");
      MapConfigSource defaultProviderConfigSource = new MapConfigSource(defaultProvider);

      Map<String, String> chainProvider = chainConfig.toConfigMap();
      MapConfigSource chainConfigSource = new MapConfigSource(chainProvider);

      Map<String, String> cliProvider = cliConfig.toConfigMap();
      MapConfigSource cliConfigSource = new MapConfigSource(cliProvider);
      Gestalt gestalt =
          new GestaltBuilder()
              .addConfigLoader(environmentVarsLoader)
              .addConfigLoader(mapConfigLoader)
              .addConfigLoader(tomlLoader)
              .addConfigLoader(propertyLoader)
              .addSource(defaultProviderConfigSource)
              .addSource(chainConfigSource)
              .addSource(cliConfigSource)
              .build();
      gestalt.loadConfigs();
      return gestalt.getConfig("config", Config.class);

    } catch (GestaltException e) {
      throw new RuntimeException(e);
    }
  }
}
