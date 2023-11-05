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

import io.optimism.batcher.compressor.CompressorConfig;
import io.optimism.batcher.compressor.Compressors;
import io.optimism.batcher.telemetry.BatcherMetrics;
import io.optimism.type.BlockId;
import io.optimism.type.L1BlockInfo;
import io.optimism.type.L2BlockRef;
import io.optimism.utilities.derive.stages.Frame;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.core.methods.response.EthBlock;

/**
 * ChannelManager class. create and remove channel object.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public class ChannelManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(ChannelManager.class);

  private final ChannelConfig chConfig;

  private final BatcherMetrics metrics;

  private final CompressorConfig compressorConfig;

  private List<EthBlock.Block> blocks;

  private final List<Channel> channels;

  private String latestBlockHash;

  private volatile boolean isClosed;

  private Channel latestChannel;

  private Map<String, Channel> txChMap;

  /**
   * Constructor of ChannelManager.
   *
   * @param chConfig channel config
   * @param compressorConfig compressor config
   */
  public ChannelManager(final ChannelConfig chConfig, final CompressorConfig compressorConfig) {
    this.chConfig = chConfig;
    this.metrics = chConfig.metrics();
    this.compressorConfig = compressorConfig;
    this.blocks = new ArrayList<>(256);
    this.channels = new ArrayList<>(256);
    this.isClosed = false;
    this.txChMap = new HashMap<>();
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
    this.blocks.add(block);
    this.latestBlockHash = block.getHash();
    this.metrics.recordL2BlockInPendingQueue(block);
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
  public Frame txData(final BlockId l1Head) {
    Channel framesSource = null;
    for (Channel channel : channels) {
      if (channel.hasFrame()) {
        framesSource = channel;
        break;
      }
    }
    if (framesSource != null || this.isClosed) {
      return this.nextFrameData(framesSource);
    }
    // no channel
    if (!this.hasSpace(this.latestChannel)) {
      this.latestChannel = this.openChannel();
      LOGGER.info(
          "Created a channel: id:{}, l1Head: {}, blocksPending:{}",
          this.latestChannel,
          l1Head,
          this.blocks.size());
      this.metrics.recordChannelOpened(null, this.blocks.size());
    }
    this.pushBlocks(this.latestChannel);
    this.updateChannelTimeout(l1Head);

    return this.nextFrameData(this.latestChannel);
  }

  /**
   * Records a transaction as failed. It will attempt to resubmit the data in the failed
   * transaction.
   *
   * @param tx channel tx
   */
  public void txFailed(final Frame tx) {
    var code = tx.code();
    if (!this.txChMap.containsKey(code)) {
      LOGGER.warn("transaction from unkown channel marked as failed: id: {}", tx.channelId());
      return;
    }
    Channel ch = this.txChMap.remove(tx.code());
    ch.txFailed(tx);
    if (!this.isClosed || !ch.noneSubmitted()) {
      return;
    }
    LOGGER.info(
        "Channel has no submitted transactions, clearing for shutdown: chId: {}", tx.channelId());
    this.channels.remove(ch);
    if (this.latestChannel.equals(ch)) {
      this.latestChannel = null;
    }
  }

  /**
   * Marks a transaction as confirmed on L1. Unfortunately even if all frames in a channel have been
   * marked as confirmed on L1 the channel may be invalid and need to be resubmitted. This function
   * may reset the pending channel if the pending channel has timed out.
   *
   * @param tx channel tx
   * @param inclusionBlock inclusion block id
   */
  public void txConfirmed(final Frame tx, final BlockId inclusionBlock) {
    this.metrics.recordBatchTxSubmitted();
    LOGGER.debug(
        "marked transaction as confirmed: chId: {}; frameNum: {};block: {}",
        tx.channelId(),
        tx.frameNumber(),
        inclusionBlock.number());
    var code = tx.code();
    if (!this.txChMap.containsKey(code)) {
      LOGGER.warn(
          "transaction from unknown channel marked as confirmed: chId: {}; frameNum: {};block: {}",
          tx.channelId(),
          tx.frameNumber(),
          inclusionBlock.number());
      return;
    }
    final var ch = this.txChMap.remove(code);
    List<EthBlock.Block> blocks = ch.txConfirmed(tx, inclusionBlock);
    if (blocks != null && blocks.size() > 0) {
      this.blocks.addAll(blocks);
    }
    if (!ch.isFullySubmitted()) {
      return;
    }
    this.channels.remove(ch);
    if (this.latestChannel.equals(ch)) {
      this.latestChannel = null;
    }
  }

  /** Close channel manager. */
  public void close() {
    if (!isClosed) {
      this.isClosed = true;
    } else {
      throw new ChannelException("channel manager has been closed");
    }
  }

  /**
   * Clears the entire state of the channel manager. It is intended to be used after an L2 reorg.
   */
  public void clear() {
    LOGGER.trace("clearing channel manager state");
    this.blocks.clear();
    this.isClosed = false;
    this.latestChannel = null;
    this.channels.clear();
    this.txChMap.clear();
  }

  private Frame nextFrameData(final Channel ch) {
    if (ch == null || !ch.hasFrame()) {
      return null;
    }
    var txData = ch.nextFrame();
    this.txChMap.put(txData.code(), ch);
    return txData;
  }

  private boolean hasSpace(final Channel channel) {
    return channel != null && !channel.isFull();
  }

  private Channel openChannel() {
    return new ChannelImpl(this.chConfig, Compressors.create(this.compressorConfig));
  }

  private void pushBlocks(final Channel lastChannel) {
    int blocksAdded = 0;
    L2BlockRef l2Ref = null;
    try {
      for (final EthBlock.Block block : this.blocks) {
        final L1BlockInfo l1Info = lastChannel.addBlock(block);
        l2Ref = L2BlockRef.fromBlockAndL1Info(block, l1Info);
        if (latestChannel.isFull()) {
          break;
        }
        this.metrics.recordL2BlockInChannel(block);
        blocksAdded += 1;
      }
    } catch (ChannelException e) {
      if (!(e instanceof ChannelFullException)) {
        LOGGER.error(
            "adding block[{}] to channel failed", this.blocks.get(blocksAdded).getNumber(), e);
      }
    }

    if (blocksAdded == this.blocks.size()) {
      this.blocks.clear();
    } else {
      this.blocks = this.blocks.stream().skip(blocksAdded).collect(Collectors.toList());
    }

    this.metrics.recordL2BlocksAdded(
        l2Ref,
        blocksAdded,
        this.blocks.size(),
        this.latestChannel.inputBytesLength(),
        this.latestChannel.readyBytesLength());

    LOGGER.debug(
        "Added blocks to channel:"
            + " blocksAdded: {}, blocksPending: {},"
            + " channelFull: {}, inputBytes: {}, readyBytes: {}",
        blocksAdded,
        this.blocks.size(),
        this.latestChannel.isFull(),
        this.latestChannel.inputBytesLength(),
        this.latestChannel.readyBytesLength());
  }

  private void updateChannelTimeout(BlockId l1Head) {
    this.latestChannel.updateTimeout(
        l1Head.number().add(BigInteger.valueOf(chConfig.maxChannelDuration())));
  }
}
