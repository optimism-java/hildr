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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigInteger;
import org.junit.jupiter.api.Test;

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
    CliConfig cliConfig = new CliConfig(null, null, null, "testjwt");
    Config config = Config.create(null, cliConfig, ChainConfig.optimismGoerli());
    assertEquals("http://127.0.0.1:8545", config.l2RpcUrl());
    assertEquals("http://127.0.0.1:8551", config.l2EngineUrl());
    assertEquals("", config.l1RpcUrl());
    assertEquals("testjwt", config.jwtSecret());
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
    assertEquals(BigInteger.valueOf(100_000_000L), config.chainConfig().maxChannelSize());
    assertEquals(BigInteger.valueOf(300L), config.chainConfig().channelTimeout());
    assertEquals(BigInteger.valueOf(3600L), config.chainConfig().seqWindowSize());
    assertEquals(BigInteger.valueOf(600L), config.chainConfig().maxSeqDrift());
    assertEquals(BigInteger.valueOf(1679079600L), config.chainConfig().regolithTime());
    assertEquals(BigInteger.valueOf(2L), config.chainConfig().blockTime());
  }
}
