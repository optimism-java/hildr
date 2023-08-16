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

import io.optimism.batcher.compressor.Compressor;
import io.optimism.batcher.exception.UnsupportedException;
import io.optimism.type.BlockId;
import io.optimism.type.L1BlockInfo;
import io.optimism.utilities.derive.stages.Batch;
import io.optimism.utilities.derive.stages.Frame;
import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.utils.Numeric;

/**
 * Channel class.Record the batcher data of block transaction and process this data with framing.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public class ChannelImpl implements Channel {

  private static final Logger LOGGER = LoggerFactory.getLogger(ChannelImpl.class);

  private static final int MAX_UNSIGNED_SHORT = (1 << 16) - 1;

  private static final String DEPOSIT_TX_TYPE = "0x7E";

  private static final int CH_ID_LEN = 16;

  private static final int MAX_RLP_BYTES_PER_CHANNEL = 10_000_000;

  private final ChannelConfig chConfig;

  private final BigInteger seqWindowTimeout;

  private final BigInteger id;

  private AtomicInteger frameNumber;

  private List<Frame> outputFrames;

  private Map<String, Frame> pendingTxs;

  private Map<String, BlockId> confirmedTxs;

  private List<EthBlock.Block> blocks;

  private BigInteger timeoutBlock;

  private Compressor compressor;

  private AtomicInteger rlpLength;

  private volatile boolean isFull;

  private volatile boolean isClose;

  /**
   * Constructor of ChannelImpl.
   *
   * @param chConfig channel config
   * @param compressor block data compressor
   */
  public ChannelImpl(ChannelConfig chConfig, Compressor compressor) {
    this.chConfig = chConfig;
    this.seqWindowTimeout =
        BigInteger.valueOf(chConfig.seqWindowSize() - chConfig.subSafetyMargin());
    this.compressor = compressor;
    try {
      var chIdBytes = new byte[CH_ID_LEN];
      SecureRandom.getInstanceStrong().nextBytes(chIdBytes);
      this.id = Numeric.toBigInt(chIdBytes);
    } catch (NoSuchAlgorithmException e) {
      throw new UnsupportedException(e);
    }
    this.blocks = new ArrayList<>();
    this.outputFrames = new ArrayList<>();
    this.pendingTxs = new HashMap<>();
    this.confirmedTxs = new HashMap<>();
    this.rlpLength = new AtomicInteger();
    this.frameNumber = new AtomicInteger();
    this.isFull = false;
    this.isClose = false;
  }

  @Override
  public L1BlockInfo addBlock(final EthBlock.Block block) {
    if (this.isFull()) {
      throw new ChannelFullException("this channel has been full of block data");
    }
    if (this.isClose) {
      throw new ChannelException("channel already closed");
    }
    final Tuple2<L1BlockInfo, Batch> l1InfoAndBatch = this.blockToBatch(block);
    final L1BlockInfo l1Info = l1InfoAndBatch.component1();
    final Batch batch = l1InfoAndBatch.component2();
    try {
      this.addBatch(batch);
      this.blocks.add(block);
    } catch (ChannelFullException e) {
      this.isFull = true;
    }
    this.updateSeqWindowTimeout(batch);
    return l1Info;
  }

  @Override
  public void splitToFrame() {
    if (this.isFull()) {
      this.closeAndOutputAllFrames();
      return;
    }
    this.outputReadyFrames();
  }

  @Override
  public Frame nextFrame() {
    if (this.outputFrames.size() == 0) {
      throw new ChannelException("not next frame");
    }
    var tx = this.outputFrames.remove(0);
    this.pendingTxs.put(tx.code(), tx);
    return tx;
  }

  @Override
  public void pushFrame(Frame frame) {
    if (frame.channelId().equals(this.id)) {
      throw new ChannelException("wrong channel");
    }
    this.outputFrames.add(frame);
  }

  @Override
  public int totalFrames() {
    return this.frameNumber.get() + 1;
  }

  @Override
  public int pendingFrames() {
    return this.outputFrames.size();
  }

  @Override
  public boolean hasFrame() {
    return this.outputFrames.size() > 0;
  }

  @Override
  public void txFailed(Frame tx) {
    // todo metrics record batch tx failed.
    var code = tx.code();
    if (!this.pendingTxs.containsKey(code)) {
      LOGGER.warn(
          "unkown tx marked as failed: chId :{}; frameNum: {}", tx.channelId(), tx.frameNumber());
      return;
    }
    LOGGER.trace(
        "marked transaction as failed: chId :{}; frameNum: {}", tx.channelId(), tx.frameNumber());
    this.pushFrame(tx);
    this.pendingTxs.remove(code);
  }

  @Override
  public List<EthBlock.Block> txConfirmed(Frame tx, BlockId inclusionBlock) {
    // todo metrics RecordBatchTxSubmitted
    LOGGER.debug(
        "marked tx as confirmed: chId: {}; frameNum: {}; block: {}",
        tx.channelId(),
        tx.frameNumber(),
        inclusionBlock);
    var code = tx.code();
    if (!this.pendingTxs.containsKey(code)) {
      LOGGER.warn(
          "unknown transaction marked as confirmed: chId: {}; frameNum: {}; block: {}",
          tx.channelId(),
          tx.frameNumber(),
          inclusionBlock);
      return null;
    }
    this.pendingTxs.remove(code);
    this.confirmedTxs.put(code, inclusionBlock);
    var timeout =
        inclusionBlock
            .number()
            .add(BigInteger.valueOf(chConfig.channelTimeout()))
            .subtract(BigInteger.valueOf(chConfig.subSafetyMargin()));
    this.updateTimeout(timeout);
    if (this.isTimeout()) {
      // todo metrics recordChannelTimeout
      LOGGER.warn("Channel timeout: chId:{}", tx.channelId());
      return this.blocks;
    }
    if (this.isFullySubmitted()) {
      // todo metrics RecordChannelFullySubmitted
      LOGGER.info("Channel is fully submitted: chId:{}", tx.channelId());
    }
    return null;
  }

  @Override
  public boolean isFull() {
    return this.isFull;
  }

  @Override
  public boolean noneSubmitted() {
    return this.confirmedTxs.size() == 0 && this.pendingTxs.size() == 0;
  }

  @Override
  public boolean isFullySubmitted() {
    return this.isFull() && (this.pendingTxs.size() + this.pendingFrames() == 0);
  }

  @Override
  public boolean isTimeout(BigInteger blockNumber) {
    return this.timeoutBlock.equals(blockNumber);
  }

  private boolean isTimeout() {
    if (this.confirmedTxs.size() == 0) {
      return false;
    }
    var min = BigInteger.valueOf(Long.MAX_VALUE);
    var max = BigInteger.ZERO;
    Collection<BlockId> inclusionBlockIds = this.confirmedTxs.values();
    for (BlockId inclusionBlockId : inclusionBlockIds) {
      var inclusionBlockNumber = inclusionBlockId.number();
      if (inclusionBlockNumber.compareTo(min) < 0) {
        min = inclusionBlockNumber;
      }
      if (inclusionBlockNumber.compareTo(max) > 0) {
        max = inclusionBlockNumber;
      }
    }
    return max.subtract(min).compareTo(BigInteger.valueOf(this.chConfig.channelTimeout())) >= 0;
  }

  /**
   * update channel data timeout block number.
   *
   * @param blockNumber block height number
   */
  @Override
  public void updateTimeout(final BigInteger blockNumber) {
    if (this.timeoutBlock == null || this.timeoutBlock.compareTo(blockNumber) > 0) {
      this.timeoutBlock = blockNumber;
    }
  }

  @Override
  public int inputBytesLength() {
    return this.rlpLength.get();
  }

  @Override
  public int readyBytesLength() {
    return this.compressor.length();
  }

  @Override
  public void close() {
    if (this.isClose) {
      throw new ChannelException("channel has been closed");
    }
    this.isClose = true;
    try {
      this.compressor.close();
    } catch (IOException e) {
      throw new ChannelException("compressor closed failed", e);
    }
  }

  private Tuple2<L1BlockInfo, Batch> blockToBatch(EthBlock.Block block) {
    final List<EthBlock.TransactionResult> blockTxs = block.getTransactions();
    if (blockTxs == null || blockTxs.size() == 0) {
      throw new ChannelException(String.format("block %s has no transations", block.getHash()));
    }
    final EthBlock.TransactionObject depositTxObj = (EthBlock.TransactionObject) blockTxs.get(0);
    if (!DEPOSIT_TX_TYPE.equalsIgnoreCase(depositTxObj.getType())) {
      throw new ChannelException("block txs not contains deposit tx");
    }
    final L1BlockInfo l1Info =
        L1BlockInfo.from(Numeric.hexStringToByteArray(depositTxObj.getInput()));

    final List<String> txDataList = new ArrayList<>(blockTxs.size());
    for (int i = 1; i < blockTxs.size(); i++) {
      final EthBlock.TransactionObject txObj = (EthBlock.TransactionObject) blockTxs.get(i);
      if (DEPOSIT_TX_TYPE.equalsIgnoreCase(txObj.getType())) {
        continue;
      }
      txDataList.add(txObj.getInput());
    }
    return new Tuple2(
        l1Info,
        new Batch(
            block.getParentHash(),
            l1Info.number(),
            l1Info.blockHash(),
            block.getTimestamp(),
            txDataList,
            null));
  }

  private int addBatch(Batch batch) {
    if (this.isClose) {
      throw new ChannelException("channel already closed");
    }
    byte[] encode = batch.encode();
    if ((this.rlpLength.get() + encode.length) > MAX_RLP_BYTES_PER_CHANNEL) {
      throw new ChannelFullException(
          String.format(
              "could not add %d bytes to channel of %d bytes, max is %d",
              encode.length, this.rlpLength.get(), MAX_RLP_BYTES_PER_CHANNEL));
    }
    this.rlpLength.addAndGet(encode.length);
    return this.compressor.write(encode);
  }

  private void closeAndOutputAllFrames() {
    this.close();
    boolean isLastFrame = false;
    while (!isLastFrame) {
      isLastFrame = createFrame();
    }
  }

  private void outputReadyFrames() {
    while (this.readyBytesLength() >= this.chConfig.maxFrameSize()) {
      boolean isLastFrame = this.createFrame();
      if (isLastFrame) {
        break;
      }
    }
  }

  private boolean createFrame() {
    var frame = this.frame(this.chConfig.maxFrameSize());
    if (frame.frameNumber() == MAX_UNSIGNED_SHORT) {
      this.isFull = true;
    }
    this.outputFrames.add(frame);
    // todo numFrames++
    // todo outputBytes += len(frame.data)
    return frame.isLastFrame();
  }

  private Frame frame(final int maxSize) {
    if (maxSize < Frame.FRAME_V0_OVER_HEAD_SIZE) {
      throw new ChannelException("maxSize is too small to fit the fixed frame overhead");
    }
    var lastFrameFlag = false;
    var dataSize = maxSize - Frame.FRAME_V0_OVER_HEAD_SIZE;
    if (dataSize > this.compressor.length()) {
      dataSize = this.compressor.length();
      lastFrameFlag = this.isClose;
    }

    byte[] data = new byte[dataSize];
    int read = this.compressor.read(data);
    if (read != data.length) {
      read = read == -1 ? 0 : read;
      byte[] temp = new byte[read];
      System.arraycopy(data, 0, temp, 0, read);
      data = temp;
    }
    var frame = Frame.create(this.id, this.frameNumber.get(), data, lastFrameFlag);
    this.frameNumber.addAndGet(1);
    return frame;
  }

  private void updateSeqWindowTimeout(final Batch batch) {
    var timeout = batch.epochNum().add(this.seqWindowTimeout);
    this.updateTimeout(timeout);
  }
}
