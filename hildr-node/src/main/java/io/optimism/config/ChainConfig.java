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
import java.util.Map;
import org.web3j.utils.Numeric;

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
 * @author grapebaba
 * @since 0.1.0
 */
public record ChainConfig(
    String network,
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
   * Optimism goerli chain config.
   *
   * @return the chain config
   */
  public static ChainConfig optimismGoerli() {
    return new ChainConfig(
        "optimism-goerli",
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
            BigInteger.valueOf(1000000)),
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
            BigInteger.valueOf(1000000)),
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
        entry("config.chainConfig.l1StartEpoch.number", this.l1StartEpoch.number().toString()),
        entry(
            "config.chainConfig.l1StartEpoch.timestamp", this.l1StartEpoch.timestamp().toString()),
        entry("config.chainConfig.l1StartEpoch.hash", this.l1StartEpoch.hash()),
        entry("config.chainConfig.l2Genesis.hash", this.l2Genesis.hash()),
        entry("config.chainConfig.l2Genesis.parentHash", this.l2Genesis.parentHash()),
        entry("config.chainConfig.l2Genesis.number", this.l2Genesis.number().toString()),
        entry("config.chainConfig.l2Genesis.timestamp", this.l2Genesis.timestamp().toString()),
        entry("config.chainConfig.systemConfig.batchSender", this.systemConfig.batchSender()),
        entry("config.chainConfig.systemConfig.gasLimit", this.systemConfig.gasLimit().toString()),
        entry(
            "config.chainConfig.systemConfig.l1FeeOverhead",
            this.systemConfig.l1FeeOverhead().toString()),
        entry(
            "config.chainConfig.systemConfig.l1FeeScalar",
            this.systemConfig.l1FeeScalar().toString()),
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
