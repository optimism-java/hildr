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

package io.optimism.type;

import java.math.BigInteger;
import org.web3j.protocol.core.Response;

/**
 * RollupConfig Response.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public class RollupConfigRes extends Response<RollupConfigRes.RollupConfig> {

  /** Constructor of RollupConfigRes. */
  public RollupConfigRes() {}

  /**
   * Returns RollupConfig.
   *
   * @return rollup config info
   */
  public RollupConfig getConfig() {
    return getResult();
  }

  @Override
  public void setResult(RollupConfig result) {
    super.setResult(result);
  }

  /**
   * Rollup config.
   *
   * @param genesis Genesis anchor point of the rollup.
   * @param blockTime Seconds per L2 block
   * @param maxSequencerDrift Sequencer batches may not be more than MaxSequencerDrift seconds after
   *     the L1 timestamp of the sequencing window end.
   * @param seqWindowSize Number of epochs (L1 blocks) per sequencing window, including the epoch L1
   *     origin block itself
   * @param channelTimeout Number of L1 blocks between when a channel can be opened and when it must
   *     be closed by.
   * @param l1ChainId Required to verify L1 signatures
   * @param l2ChainId Required to identify the L2 network and create p2p signatures unique for this
   *     chain.
   * @param regolithTime RegolithTime sets the activation time of the Regolith network-upgrade: a
   *     pre-mainnet Bedrock change that addresses findings of the Sherlock contest related to
   *     deposit attributes. "Regolith" is the loose deposited rock that sits on top of Bedrock.
   * @param batchInboxAddress L1 address that batches are sent to.
   * @param depositContractAddress L1 Deposit Contract Address.
   * @param l1SystemConfigAddress L1 System Config Address.
   */
  public record RollupConfig(
      Genesis genesis,
      BigInteger blockTime,
      BigInteger maxSequencerDrift,
      BigInteger seqWindowSize,
      BigInteger channelTimeout,
      BigInteger l1ChainId,
      BigInteger l2ChainId,
      BigInteger regolithTime,
      String batchInboxAddress,
      String depositContractAddress,
      String l1SystemConfigAddress) {}
}
