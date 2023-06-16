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

import static java.util.Map.entry;

import io.optimism.common.BlockInfo;
import io.optimism.common.Epoch;
import java.math.BigInteger;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.loader.EnvironmentVarsLoader;
import org.github.gestalt.config.loader.MapConfigLoader;
import org.github.gestalt.config.loader.PropertyLoader;
import org.github.gestalt.config.source.FileConfigSource;
import org.github.gestalt.config.source.MapConfigSource;
import org.github.gestalt.config.toml.TomlLoader;
import org.web3j.utils.Numeric;

/**
 * The type Config.
 *
 * @param l1RpcUrl L1 chain rpc url.
 * @param l2RpcUrl L2 chain rpc url.
 * @param l2EngineUrl L2 engine API url.
 * @param jwtSecret L2 engine API jwt secret.
 * @param chainConfig The chain config.
 * @param rpcPort The rpc port.
 * @param checkpointSyncUrl The checkpoint sync url.
 * @author grapebaba
 * @since 0.1.0
 */
public record Config(
    String l1RpcUrl,
    String l2RpcUrl,
    String l2EngineUrl,
    String jwtSecret,
    String checkpointSyncUrl,
    Integer rpcPort,
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
      defaultProvider.put("config.checkpointSyncUrl", "");
      defaultProvider.put("config.rpcPort", "9545");
      MapConfigSource defaultProviderConfigSource = new MapConfigSource(defaultProvider);

      Map<String, String> chainProvider = chainConfig.toConfigMap();
      MapConfigSource chainConfigSource = new MapConfigSource(chainProvider);

      Map<String, String> cliProvider = cliConfig.toConfigMap();
      MapConfigSource cliConfigSource = new MapConfigSource(cliProvider);

      Gestalt gestalt;
      if (configPath != null) {
        FileConfigSource tomlConfigSource = new FileConfigSource(configPath);
        gestalt =
            new GestaltBuilder()
                .addConfigLoader(environmentVarsLoader)
                .addConfigLoader(mapConfigLoader)
                .addConfigLoader(tomlLoader)
                .addConfigLoader(propertyLoader)
                .addSource(defaultProviderConfigSource)
                .addSource(chainConfigSource)
                .addSource(tomlConfigSource)
                .addSource(cliConfigSource)
                .build();
      } else {
        gestalt =
            new GestaltBuilder()
                .addConfigLoader(environmentVarsLoader)
                .addConfigLoader(mapConfigLoader)
                .addConfigLoader(tomlLoader)
                .addConfigLoader(propertyLoader)
                .addSource(defaultProviderConfigSource)
                .addSource(chainConfigSource)
                .addSource(cliConfigSource)
                .build();
      }
      gestalt.loadConfigs();
      return gestalt.getConfig("config", Config.class);

    } catch (GestaltException e) {
      throw new ConfigLoadException(e);
    }
  }

  /**
   * The type Cli config.
   *
   * @param l1RpcUrl L1 chain rpc url.
   * @param l2RpcUrl L2 chain rpc url.
   * @param l2EngineUrl L2 engine API url.
   * @param jwtSecret L2 engine API jwt secret.
   * @param checkpointSyncUrl The checkpoint sync url.
   * @param rpcPort The rpc port.
   */
  public record CliConfig(
      String l1RpcUrl,
      String l2RpcUrl,
      String l2EngineUrl,
      String jwtSecret,
      String checkpointSyncUrl,
      Integer rpcPort) {

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
      if (StringUtils.isNotEmpty(checkpointSyncUrl)) {
        map.put("config.checkpointSyncUrl", checkpointSyncUrl);
      }
      if (rpcPort != null) {
        map.put("config.rpcPort", rpcPort.toString());
      }
      return map;
    }
  }

  /**
   * The type ChainConfig.
   *
   * @param network The network name.
   * @param l1StartEpoch The L1 block referenced by the L2 chainConfig.
   * @param l2Genesis The L2 genesis block info.
   * @param systemConfig The initial system config value.
   * @param batchInbox The batch inbox address.
   * @param depositContract The deposit contract address.
   * @param systemConfigContract The L1 system config contract.
   * @param maxChannelSize The maximum byte size of all pending channels.
   * @param channelTimeout The max timeout for a channel (as measured by the frame L1 block number).
   * @param seqWindowSize Number of L1 blocks in a sequence window.
   * @param maxSeqDrift Maximum timestamp drift.
   * @param regolithTime Timestamp of the regolith hardfork.
   * @param blockTime Network blocktime.
   * @param l2Tol1MessagePasser L2 To L1 Message passer address.
   * @param chainId The chain id.
   * @author grapebaba
   * @since 0.1.0
   */
  public record ChainConfig(
      String network,
      BigInteger chainId,
      Epoch l1StartEpoch,
      BlockInfo l2Genesis,
      SystemConfig systemConfig,
      String batchInbox,
      String depositContract,
      String systemConfigContract,
      BigInteger maxChannelSize,
      BigInteger channelTimeout,
      BigInteger seqWindowSize,
      BigInteger maxSeqDrift,
      BigInteger regolithTime,
      BigInteger blockTime,
      String l2Tol1MessagePasser) {

    /**
     * Optimism chain config.
     *
     * @return the chain config
     */
    public static ChainConfig optimism() {
      return new ChainConfig(
          "optimism",
          BigInteger.valueOf(10L),
          new Epoch(
              BigInteger.valueOf(17422590L),
              "0x438335a20d98863a4c0c97999eb2481921ccd28553eac6f913af7c12aec04108",
              BigInteger.valueOf(1686068903L)),
          new BlockInfo(
              "0xdbf6a80fef073de06add9b0d14026d6e5a86c85f6d102c36d3d8e9cf89c2afd3",
              BigInteger.valueOf(105235063L),
              "0x21a168dfa5e727926063a28ba16fd5ee84c814e847c81a699c7a0ea551e4ca50",
              BigInteger.valueOf(1686068903L)),
          new SystemConfig(
              "0x6887246668a3b87f54deb3b94ba47a6f63f32985",
              BigInteger.valueOf(30_000_000L),
              BigInteger.valueOf(188L),
              BigInteger.valueOf(684000L),
              "0xAAAA45d9549EDA09E70937013520214382Ffc4A2"),
          "0xff00000000000000000000000000000000000010",
          "0xbEb5Fc579115071764c7423A4f12eDde41f106Ed",
          "0x229047fed2591dbec1eF1118d64F7aF3dB9EB290",
          BigInteger.valueOf(100_000_000L),
          BigInteger.valueOf(300L),
          BigInteger.valueOf(3600L),
          BigInteger.valueOf(600L),
          BigInteger.ZERO,
          BigInteger.valueOf(2L),
          "0x4200000000000000000000000000000000000016");
    }

    /**
     * Optimism goerli chain config.
     *
     * @return the chain config
     */
    public static ChainConfig optimismGoerli() {
      return new ChainConfig(
          "optimism-goerli",
          BigInteger.valueOf(420L),
          new Epoch(
              BigInteger.valueOf(8300214L),
              "0x6ffc1bf3754c01f6bb9fe057c1578b87a8571ce2e9be5ca14bace6eccfd336c7",
              BigInteger.valueOf(1673550516L)),
          new BlockInfo(
              "0x0f783549ea4313b784eadd9b8e8a69913b368b7366363ea814d7707ac505175f",
              BigInteger.valueOf(4061224L),
              "0x31267a44f1422f4cab59b076548c075e79bd59e691a23fbce027f572a2a49dc9",
              BigInteger.valueOf(1673550516L)),
          new SystemConfig(
              "0x7431310e026b69bfc676c0013e12a1a11411eec9",
              BigInteger.valueOf(25_000_000L),
              BigInteger.valueOf(2100),
              BigInteger.valueOf(1000000),
              "0x715b7219D986641DF9eFd9C7Ef01218D528e19ec"),
          "0xff00000000000000000000000000000000000420",
          "0x5b47E1A08Ea6d985D6649300584e6722Ec4B1383",
          "0xAe851f927Ee40dE99aaBb7461C00f9622ab91d60",
          BigInteger.valueOf(100_000_000L),
          BigInteger.valueOf(300L),
          BigInteger.valueOf(3600L),
          BigInteger.valueOf(600L),
          BigInteger.valueOf(1679079600L),
          BigInteger.valueOf(2L),
          "0xEF2ec5A5465f075E010BE70966a8667c94BCe15a");
    }

    /**
     * Base goerli ChainConfig.
     *
     * @return the chain config
     */
    public static ChainConfig baseGoerli() {
      return new ChainConfig(
          "base-goerli",
          BigInteger.valueOf(84531L),
          new Epoch(
              BigInteger.valueOf(8410981L),
              "0x73d89754a1e0387b89520d989d3be9c37c1f32495a88faf1ea05c61121ab0d19",
              BigInteger.valueOf(1675193616L)),
          new BlockInfo(
              "0xa3ab140f15ea7f7443a4702da64c10314eb04d488e72974e02e2d728096b4f76",
              BigInteger.valueOf(0L),
              Numeric.toHexString(new byte[32]),
              BigInteger.valueOf(1675193616L)),
          new SystemConfig(
              "0x2d679b567db6187c0c8323fa982cfb88b74dbcc7",
              BigInteger.valueOf(25_000_000L),
              BigInteger.valueOf(2100),
              BigInteger.valueOf(1000000),
              "0x32a4e99A72c11E9DD3dC159909a2D7BD86C1Bc51"),
          "0x8453100000000000000000000000000000000000",
          "0xe93c8cd0d409341205a592f8c4ac1a5fe5585cfa",
          "0xb15eea247ece011c68a614e4a77ad648ff495bc1",
          BigInteger.valueOf(100_000_000L),
          BigInteger.valueOf(100L),
          BigInteger.valueOf(3600L),
          BigInteger.valueOf(600L),
          BigInteger.valueOf(Long.MAX_VALUE),
          BigInteger.valueOf(2L),
          "0x4200000000000000000000000000000000000016");
    }

    /**
     * To ConfigMap.
     *
     * @return the map
     */
    public Map<String, String> toConfigMap() {
      return Map.ofEntries(
          entry("config.chainConfig.network", this.network),
          entry("config.chainConfig.chainId", this.chainId.toString()),
          entry("config.chainConfig.l1StartEpoch.number", this.l1StartEpoch.number().toString()),
          entry(
              "config.chainConfig.l1StartEpoch.timestamp",
              this.l1StartEpoch.timestamp().toString()),
          entry("config.chainConfig.l1StartEpoch.hash", this.l1StartEpoch.hash()),
          entry("config.chainConfig.l2Genesis.hash", this.l2Genesis.hash()),
          entry("config.chainConfig.l2Genesis.parentHash", this.l2Genesis.parentHash()),
          entry("config.chainConfig.l2Genesis.number", this.l2Genesis.number().toString()),
          entry("config.chainConfig.l2Genesis.timestamp", this.l2Genesis.timestamp().toString()),
          entry("config.chainConfig.systemConfig.batchSender", this.systemConfig.batchSender()),
          entry(
              "config.chainConfig.systemConfig.gasLimit", this.systemConfig.gasLimit().toString()),
          entry(
              "config.chainConfig.systemConfig.l1FeeOverhead",
              this.systemConfig.l1FeeOverhead().toString()),
          entry(
              "config.chainConfig.systemConfig.l1FeeScalar",
              this.systemConfig.l1FeeScalar().toString()),
          entry(
              "config.chainConfig.systemConfig.unsafeBlockSigner",
              this.systemConfig.unsafeBlockSigner()),
          entry("config.chainConfig.batchInbox", this.batchInbox),
          entry("config.chainConfig.depositContract", this.depositContract),
          entry("config.chainConfig.systemConfigContract", this.systemConfigContract),
          entry("config.chainConfig.maxChannelSize", this.maxChannelSize.toString()),
          entry("config.chainConfig.channelTimeout", this.channelTimeout.toString()),
          entry("config.chainConfig.seqWindowSize", this.seqWindowSize.toString()),
          entry("config.chainConfig.maxSeqDrift", this.maxSeqDrift.toString()),
          entry("config.chainConfig.regolithTime", this.regolithTime.toString()),
          entry("config.chainConfig.blockTime", this.blockTime.toString()),
          entry("config.chainConfig.l2Tol1MessagePasser", this.l2Tol1MessagePasser));
    }
  }

  /**
   * The enum Sync mode.
   *
   * @author grapebaba
   * @since 0.1.0
   */
  public enum SyncMode {
    /** Fast sync mode. */
    Fast,
    /** Challenge sync mode. */
    Challenge,
    /** Full sync mode. */
    Full,
    /** Checkpoint sync mode. */
    Checkpoint;

    /**
     * From sync mode.
     *
     * @param value the value
     * @return the sync mode
     */
    public static SyncMode from(String value) {
      return switch (value) {
        case "fast" -> Fast;
        case "challenge" -> Challenge;
        case "full" -> Full;
        case "checkpoint" -> Checkpoint;
        default -> throw new RuntimeException("invalid sync mode");
      };
    }
  }

  /**
   * The type SystemAccounts.
   *
   * @param attributesDepositor attributes depositor.
   * @param attributesPreDeploy attributes preDeploy.
   * @param feeVault fee vault.
   * @author grapebaba
   * @since 0.1.0
   */
  public record SystemAccounts(
      String attributesDepositor, String attributesPreDeploy, String feeVault) {

    /**
     * Create default SystemAccounts instance.
     *
     * @return the SystemAccounts
     */
    public static SystemAccounts defaultSystemAccounts() {
      return new SystemAccounts(
          "0xdeaddeaddeaddeaddeaddeaddeaddeaddead0001",
          "0x4200000000000000000000000000000000000015",
          "0x4200000000000000000000000000000000000011");
    }
  }

  /**
   * The type SystemConfig.
   *
   * @param batchSender batch sender address.
   * @param gasLimit gas limit.
   * @param l1FeeOverhead L1 fee overhead.
   * @param l1FeeScalar L1 fee scalar.
   * @param unsafeBlockSigner unsafe block signer address.
   * @author grapebaba
   * @since 0.1.0
   */
  public record SystemConfig(
      String batchSender,
      BigInteger gasLimit,
      BigInteger l1FeeOverhead,
      BigInteger l1FeeScalar,
      String unsafeBlockSigner) {

    /**
     * Batch hash string.
     *
     * @return the string
     */
    public String batcherHash() {
      return Numeric.toHexStringWithPrefixZeroPadded(Numeric.toBigInt(batchSender), 64);
    }
  }
}
