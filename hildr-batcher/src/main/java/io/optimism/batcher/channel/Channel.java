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
import io.optimism.type.L1BlockInfo;
import io.optimism.utilities.derive.stages.Frame;
import java.io.Closeable;
import java.math.BigInteger;
import java.util.List;
import org.web3j.protocol.core.methods.response.EthBlock;

/**
 * Channel interface. cache batch submit data.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public interface Channel extends Closeable {

  /**
   * Add block to channel, block will be parsed and be compressed.
   *
   * @param block Block on L2
   * @return l1 block info
   */
  L1BlockInfo addBlock(EthBlock.Block block);

  /** Split channel data to frames. */
  void splitToFrame();

  /**
   * Get next frame data should be published to l1. The data added to the channel will be
   * transformed into multiple frames.
   *
   * @return TxData instance contains frame data
   */
  Frame nextFrame();

  /**
   * Push frame to current channel.
   *
   * @param frame TxData instance
   */
  void pushFrame(Frame frame);

  /**
   * Get Total frames.
   *
   * @return total value.
   */
  int totalFrames();

  /**
   * Get count of pending frames.
   *
   * @return count of pending frames
   */
  int pendingFrames();

  /**
   * If has frame.
   *
   * @return true if has data of frame, otherwise false.
   */
  boolean hasFrame();

  /**
   * Has none pending tx data.
   *
   * @return true if has none pending tx data, otherwise false.
   */
  boolean noneSubmitted();

  /**
   * Check is tx data fully submitted.
   *
   * @return ture if fully submitted, otherwise false.
   */
  boolean isFullySubmitted();

  /**
   * Process failed tx that belong to the channel. Will push tx back to pending queue.
   *
   * @param tx failed tx data
   */
  void txFailed(Frame tx);

  /**
   * Process confirmed tx that belong to the channel.
   *
   * @param tx confirmed tx data
   * @param inclusionBlock tx data be inclusion block number
   * @return if channel was timeout, the blocks added to the channel will be returned.
   */
  List<EthBlock.Block> txConfirmed(Frame tx, BlockId inclusionBlock);

  /**
   * If channel touch limit of frame data.
   *
   * @return true if full of data, otherwise false.
   */
  boolean isFull();

  /**
   * If the channel data expired at the specified block height.
   *
   * @param blockNumber block height number
   * @return true if timeout,otherwise false.
   */
  boolean isTimeout(BigInteger blockNumber);

  /**
   * Update channel data expired at the specified block height.
   *
   * @param blockNumber block height number
   */
  void updateTimeout(final BigInteger blockNumber);

  /**
   * Input bytes data.
   *
   * @return input bytes
   */
  int inputBytesLength();

  /**
   * Ready to publishing bytes.
   *
   * @return ready to publishing bytes
   */
  int readyBytesLength();
}
