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

package io.optimism.driver;

import io.optimism.common.AttributesDepositedCall;
import io.optimism.common.BlockInfo;
import io.optimism.common.Epoch;
import java.math.BigInteger;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlock.TransactionObject;

/**
 * The type HeadInfo.
 *
 * @param l2BlockInfo the L2 block info
 * @param l1Epoch the L1 epoch
 * @param sequenceNumber the sequence number
 * @author grapebaba
 * @since 0.1.0
 */
public record HeadInfo(BlockInfo l2BlockInfo, Epoch l1Epoch, BigInteger sequenceNumber) {

  /**
   * From head info.
   *
   * @param block the block
   * @return the head info
   */
  public static HeadInfo from(EthBlock.Block block) {
    BlockInfo blockInfo = BlockInfo.from(block);

    if (block.getTransactions().isEmpty()) {
      throw new L1AttributesDepositedTxNotFoundException();
    }
    String txCallData = ((TransactionObject) block.getTransactions().get(0)).getInput();

    AttributesDepositedCall call = AttributesDepositedCall.from(txCallData);
    Epoch epoch = Epoch.from(call);

    return new HeadInfo(blockInfo, epoch, call.sequenceNumber());
  }
}
