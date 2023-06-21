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

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.optimism.config.Config.ChainConfig;
import io.optimism.config.Config.CliConfig;
import io.optimism.config.Config.SystemConfig;
import java.math.BigInteger;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.web3j.utils.Numeric;

/**
 * The type ConfigTest.
 *
 * @author grapebaba
 * @since 0.1.0
 */
class ConfigTest {

  /** Create. */
  @Test
  void create() {
    CliConfig cliConfig = new CliConfig(null, null, null, "testjwt", null, null);
    Config config =
        Config.create(
            Paths.get("src", "test", "resources", "test.toml"),
            cliConfig,
            ChainConfig.optimismGoerli());
    assertEquals("https://example2.com", config.l2RpcUrl());
    assertEquals("http://127.0.0.1:8551", config.l2EngineUrl());
    assertEquals("", config.l1RpcUrl());
    assertEquals("testjwt", config.jwtSecret());
    assertEquals(9545, config.rpcPort());
    assertEquals(
        "0x6ffc1bf3754c01f6bb9fe057c1578b87a8571ce2e9be5ca14bace6eccfd336c7",
        config.chainConfig().l1StartEpoch().hash());
    assertEquals(BigInteger.valueOf(8300214L), config.chainConfig().l1StartEpoch().number());
    assertEquals(BigInteger.valueOf(1673550516L), config.chainConfig().l1StartEpoch().timestamp());
    assertEquals("optimism-goerli", config.chainConfig().network());
    assertEquals(
        "0x0f783549ea4313b784eadd9b8e8a69913b368b7366363ea814d7707ac505175f",
        config.chainConfig().l2Genesis().hash());
    assertEquals(BigInteger.valueOf(4061224L), config.chainConfig().l2Genesis().number());
    assertEquals(
        "0x31267a44f1422f4cab59b076548c075e79bd59e691a23fbce027f572a2a49dc9",
        config.chainConfig().l2Genesis().parentHash());
    assertEquals(BigInteger.valueOf(1673550516L), config.chainConfig().l2Genesis().timestamp());
    assertEquals(
        "0x7431310e026b69bfc676c0013e12a1a11411eec9",
        config.chainConfig().systemConfig().batchSender());
    assertEquals(BigInteger.valueOf(25_000_000L), config.chainConfig().systemConfig().gasLimit());
    assertEquals(BigInteger.valueOf(2100), config.chainConfig().systemConfig().l1FeeOverhead());
    assertEquals(BigInteger.valueOf(1000000), config.chainConfig().systemConfig().l1FeeScalar());
    assertEquals("0xff00000000000000000000000000000000000420", config.chainConfig().batchInbox());
    assertEquals(
        "0x5b47E1A08Ea6d985D6649300584e6722Ec4B1383", config.chainConfig().depositContract());
    assertEquals(
        "0xAe851f927Ee40dE99aaBb7461C00f9622ab91d60", config.chainConfig().systemConfigContract());
    assertEquals(
        "0xEF2ec5A5465f075E010BE70966a8667c94BCe15a", config.chainConfig().l2Tol1MessagePasser());
    assertEquals(BigInteger.valueOf(111_111_111L), config.chainConfig().maxChannelSize());
    assertEquals(BigInteger.valueOf(300L), config.chainConfig().channelTimeout());
    assertEquals(BigInteger.valueOf(3600L), config.chainConfig().seqWindowSize());
    assertEquals(BigInteger.valueOf(600L), config.chainConfig().maxSeqDrift());
    assertEquals(BigInteger.valueOf(1679079600L), config.chainConfig().regolithTime());
    assertEquals(BigInteger.valueOf(2L), config.chainConfig().blockTime());
  }

  /**
   * The type ChainConfig test.
   *
   * @author grapebaba
   * @since 0.1.0
   */
  static class ChainConfigTest {

    /** Optimism goerli. */
    @Test
    void baseGoerli() {
      ChainConfig chainConfig = ChainConfig.baseGoerli();
      assertEquals(chainConfig.regolithTime(), BigInteger.valueOf(Long.MAX_VALUE));
    }

    /** Base goerli. */
    @Test
    void optimismGoerli() {
      ChainConfig chainConfig = ChainConfig.optimismGoerli();
      assertEquals(chainConfig.regolithTime(), new BigInteger("1679079600"));
    }
  }

  /**
   * The type SystemConfig test.
   *
   * @author grapebaba
   * @since 0.1.0
   */
  static class SystemConfigTest {

    /** Batch hash. */
    @Test
    void batchHash() {
      SystemConfig systemConfig =
          new SystemConfig(
              "0x2d679b567db6187c0c8323fa982cfb88b74dbcc7",
              BigInteger.valueOf(25_000_000L),
              BigInteger.valueOf(2100),
              BigInteger.valueOf(1000000),
              "0x2d679b567db6187c0c8323fa982cfb88b74dbcc7");

      assertTrue(
          systemConfig.batcherHash().contains(Numeric.cleanHexPrefix(systemConfig.batchSender())));
    }
  }

  static class ExternalChainConfigTest {
    @Test
    void readExternalChainFromJson() {
      var devnetJson =
          "{\n"
              + "\"genesis\": {\n"
              + "  \"l1\": {\n"
              + "    \"hash\": \"0xdb52a58e7341447d1a9525d248ea07dbca7dfa0e105721dee1aa5a86163c088d\",\n"
              + "    \"number\": 0\n"
              + "  },\n"
              + "  \"l2\": {\n"
              + "    \"hash\": \"0xf85bca315a08237644b06a8350cda3bc0de1593745a91be93daeadb28fb3a32e\",\n"
              + "    \"number\": 0\n"
              + "  },\n"
              + "  \"l2_time\": 1685710775,\n"
              + "  \"system_config\": {\n"
              + "    \"batcherAddr\": \"0x3c44cdddb6a900fa2b585dd299e03d12fa4293bc\",\n"
              + "    \"overhead\":\n"
              + "         \"0x0000000000000000000000000000000000000000000000000000000000000834\",\n"
              + "    \"scalar\": \"0x00000000000000000000000000000000000000000000000000000000000f4240\",\n"
              + "    \"gasLimit\": 30000000\n"
              + "  }\n"
              + "},\n"
              + "\"block_time\": 2,\n"
              + "\"max_sequencer_drift\": 300,\n"
              + "\"seq_window_size\": 200,\n"
              + "\"channel_timeout\": 120,\n"
              + "\"l1_chain_id\": 900,\n"
              + "\"l2_chain_id\": 901,\n"
              + "\"regolith_time\": 0,\n"
              + "\"batch_inbox_address\": \"0xff00000000000000000000000000000000000000\",\n"
              + "\"deposit_contract_address\": \"0x6900000000000000000000000000000000000001\",\n"
              + "\"l1_system_config_address\": \"0x6900000000000000000000000000000000000009\"\n"
              + "}";
      var external =
          assertDoesNotThrow(
              () -> {
                var mapper = new ObjectMapper();
                return mapper.readValue(devnetJson, Config.ExternalChainConfig.class);
              },
              "parse json content should not throws but it does");
      var chain = Config.ChainConfig.fromExternal(external);
      assertEquals(chain.network(), "external");
      assertEquals(chain.l1ChainId(), BigInteger.valueOf(900L));
      assertEquals(chain.l2ChainId(), BigInteger.valueOf(901L));
      assertEquals(chain.l1StartEpoch().number(), BigInteger.ZERO);
      assertEquals(
          chain.l1StartEpoch().hash(),
          "0xdb52a58e7341447d1a9525d248ea07dbca7dfa0e105721dee1aa5a86163c088d");
      assertEquals(
          chain.l2Genesis().hash(),
          "0xf85bca315a08237644b06a8350cda3bc0de1593745a91be93daeadb28fb3a32e");
      assertEquals(chain.systemConfig().gasLimit(), BigInteger.valueOf(30_000_000L));
      assertEquals(chain.systemConfig().l1FeeOverhead(), BigInteger.valueOf(2100L));
      assertEquals(chain.systemConfig().l1FeeScalar(), BigInteger.valueOf(1_000_000L));
      assertEquals(
          chain.systemConfig().batchSender(), "0x3c44cdddb6a900fa2b585dd299e03d12fa4293bc");
      assertEquals(chain.batchInbox(), "0xff00000000000000000000000000000000000000");
      assertEquals(chain.depositContract(), "0x6900000000000000000000000000000000000001");
      assertEquals(chain.systemConfigContract(), "0x6900000000000000000000000000000000000009");

      assertEquals(chain.maxChannelSize(), BigInteger.valueOf(100_000_000L));
      assertEquals(chain.channelTimeout(), BigInteger.valueOf(120L));
      assertEquals(chain.seqWindowSize(), BigInteger.valueOf(200L));
      assertEquals(chain.maxSeqDrift(), BigInteger.valueOf(300L));
      assertEquals(chain.regolithTime(), BigInteger.ZERO);
      assertEquals(chain.blockTime(), BigInteger.TWO);
    }
  }
}
