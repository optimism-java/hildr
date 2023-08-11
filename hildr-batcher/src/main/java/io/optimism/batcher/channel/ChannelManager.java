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

package io.optimism.batcher.channel;

import io.optimism.type.BlockId;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.web3j.protocol.core.methods.response.EthBlock;

/**
 * ChannelManager class. create and remove channel object.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public class ChannelManager {

  private List<EthBlock.Block> blocks;

  private String latestBlockHash;

  /** Constructor of ChannelManager. */
  public ChannelManager() {
    this.blocks = new ArrayList<>(256);
  }

  /**
   * Adds an L2 block to the internal blocks queue. It throws ReorgException if the block does not
   * extend the last block loaded into the state. If no blocks were added yet, the parent hash check
   * is skipped.
   *
   * @param block L2 block data
   */
  public void addL2Block(EthBlock.Block block) {
    if (!StringUtils.isEmpty(latestBlockHash) && !latestBlockHash.equals(block.getParentHash())) {
      throw new ReorgException("block does not extend existing chain");
    }
    // todo metrics pending block
    this.blocks.add(block);
    this.latestBlockHash = block.getHash();
  }

  /**
   * Returns the next tx data that should be submitted to L1.
   *
   * <p>It currently only uses one frame per transaction. If the pending channel is full, it only
   * returns the remaining frames of this channel until it got successfully fully sent to L1. It
   * returns io.EOF if there's no pending frame.
   *
   * @param l1Head l1 head block id
   * @return The next tx data that should be submitted to L1.
   */
  public Channel.TxData txData(BlockId l1Head) {
    return null;
  }

  /**
   * Records a transaction as failed. It will attempt to resubmit the data in the failed
   * transaction.
   *
   * @param txId channel tx id
   */
  public void txFailed(Channel.TxData txId) {}

  /**
   * Marks a transaction as confirmed on L1. Unfortunately even if all frames in a channel have been
   * marked as confirmed on L1 the channel may be invalid and need to be resubmitted. This function
   * may reset the pending channel if the pending channel has timed out.
   *
   * @param txId channel tx id
   * @param inclusionBlock inclusion block id
   */
  public void txConfirmed(Channel.TxData txId, BlockId inclusionBlock) {}

  /** Clear blocks and channels that have not entered the pending state. */
  public void clear() {}
}
