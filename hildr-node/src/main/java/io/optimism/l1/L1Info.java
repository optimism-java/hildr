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

package io.optimism.l1;

import static org.web3j.protocol.core.methods.response.EthBlock.Block;
import static org.web3j.protocol.core.methods.response.EthBlock.TransactionObject;

import io.optimism.config.SystemConfig;
import io.optimism.derive.stages.UserDeposited;
import java.util.List;

/**
 * The type L1Info.
 *
 * @param blockInfo L1 block info.
 * @param systemConfig system config.
 * @param userDeposits user deposits.
 * @param batcherTransactions batcher transactions.
 * @param finalized finalized.
 * @author grapebaba
 * @since 0.1.0
 */
public record L1Info(
    L1BlockInfo blockInfo,
    SystemConfig systemConfig,
    List<UserDeposited> userDeposits,
    List<String> batcherTransactions,
    boolean finalized) {

  /**
   * Create L1Info.
   *
   * @param block the block
   * @param userDeposits the user deposits
   * @param batchInbox the batch inbox
   * @param finalized the finalized
   * @param systemConfig the system config
   * @return the L1Info
   */
  public static L1Info create(
      Block block,
      List<UserDeposited> userDeposits,
      String batchInbox,
      boolean finalized,
      SystemConfig systemConfig) {
    L1BlockInfo l1BlockInfo =
        L1BlockInfo.create(
            block.getNumber(),
            block.getHash(),
            block.getTimestamp(),
            block.getBaseFeePerGas(),
            block.getMixHash());
    List<String> batcherTransactions =
        createBatcherTransactions(block, systemConfig.batchSender(), batchInbox);

    return new L1Info(l1BlockInfo, systemConfig, userDeposits, batcherTransactions, finalized);
  }

  private static List<String> createBatcherTransactions(
      Block block, String batchSender, String batchInbox) {
    return block.getTransactions().stream()
        .filter(
            transactionResult ->
                batchSender.equals(((TransactionObject) transactionResult).getFrom())
                    && batchInbox.equals(((TransactionObject) transactionResult).getTo()))
        .map(transactionResult -> ((TransactionObject) transactionResult).getInput())
        .toList();
  }
}
