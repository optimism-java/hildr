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

package io.optimism.derive.stages;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Lists;
import io.optimism.common.BlockInfo;
import io.optimism.common.Epoch;
import io.optimism.config.Config;
import io.optimism.derive.PurgeableIterator;
import io.optimism.derive.State;
import io.optimism.derive.stages.Batches.Batch;
import io.optimism.derive.stages.Channels.Channel;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.rlp.RlpDecoder;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpString;
import org.web3j.rlp.RlpType;
import org.web3j.utils.Numeric;

/**
 * The type Batches.
 *
 * @param <I> the type parameter
 * @author grapebaba
 * @since 0.1.0
 */
public class Batches<I extends PurgeableIterator<Channel>> extends AbstractIterator<Batch>
    implements PurgeableIterator<Batch> {

  private static final Logger LOGGER = LoggerFactory.getLogger(Batches.class);
  private final TreeMap<BigInteger, Batch> batches;

  private final I channelIterator;

  private final AtomicReference<State> state;

  private final Config config;

  /**
   * Instantiates a new Batches.
   *
   * @param batches the batches
   * @param channelIterator the channel iterator
   * @param state the state
   * @param config the config
   */
  public Batches(
      TreeMap<BigInteger, Batch> batches,
      I channelIterator,
      AtomicReference<State> state,
      Config config) {
    this.batches = batches;
    this.channelIterator = channelIterator;
    this.state = state;
    this.config = config;
  }

  @Override
  public void purge() {
    this.channelIterator.purge();
    this.batches.clear();
  }

  @Override
  protected Batch computeNext() {
    if (this.channelIterator.hasNext()) {
      Channel channel = this.channelIterator.next();
      decodeBatches(channel).forEach(batch -> this.batches.put(batch.timestamp(), batch));
    }

    Batch derivedBatch = null;
    while (true) {
      if (this.batches.firstEntry() != null) {
        Batch batch = this.batches.firstEntry().getValue();
        switch (batchStatus(batch)) {
          case Accept:
            derivedBatch = batch;
            this.batches.remove(batch.timestamp());
            break;
          case Drop:
            LOGGER.warn("dropping invalid batch");
            this.batches.remove(batch.timestamp());
            continue;
          case Future, Undecided:
            break;
          default:
            throw new IllegalStateException("Unexpected value: " + batchStatus(batch));
        }
      } else {
        break;
      }
    }

    Batch batch = null;
    if (derivedBatch != null) {
      batch = derivedBatch;
    } else {
      State state = this.state.get();

      BigInteger currentL1Block = state.getCurrentEpochNum();
      BlockInfo safeHead = state.getSafeHead();
      Epoch epoch = state.getSafeEpoch();
      Epoch nextEpoch = state.epoch(epoch.number().add(BigInteger.ONE));
      BigInteger seqWindowSize = this.config.chainConfig().seqWindowSize();

      if (nextEpoch != null) {
        if (currentL1Block.compareTo(epoch.number().add(seqWindowSize)) > 0) {
          BigInteger nextTimestamp =
              safeHead.timestamp().add(this.config.chainConfig().blockTime());
          Epoch epochRes = nextTimestamp.compareTo(nextEpoch.timestamp()) < 0 ? epoch : nextEpoch;
          batch =
              new Batch(
                  safeHead.parentHash(),
                  epochRes.number(),
                  epochRes.hash(),
                  nextTimestamp,
                  Lists.newArrayList(),
                  currentL1Block);
        }
      }
    }

    if (batch == null) {
      LOGGER.debug("Failed to decode batch");
    }
    return batch;
  }

  /**
   * Decode batches list.
   *
   * @param channel the channel
   * @return the list
   */
  public static List<Batch> decodeBatches(Channel channel) {
    byte[] channelData = decompressZlib(channel.data());
    List<RlpType> batches = RlpDecoder.decode(channelData).getValues();
    return batches.stream()
        .map(
            rlpType -> {
              byte[] batchData =
                  ArrayUtils.subarray(
                      ((RlpString) rlpType).getBytes(),
                      1,
                      ((RlpString) batches.get(0)).getBytes().length);
              RlpList rlpBatchData = (RlpList) RlpDecoder.decode(batchData).getValues().get(0);
              return Batch.decode(rlpBatchData, channel.l1InclusionBlock());
            })
        .toList();
  }

  private static byte[] decompressZlib(byte[] data) {
    try {
      Inflater inflater = new Inflater();
      inflater.setInput(data);
      ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
      byte[] buffer = new byte[1024];
      while (!inflater.finished()) {
        int count = inflater.inflate(buffer);
        outputStream.write(buffer, 0, count);
      }

      return outputStream.toByteArray();
    } catch (DataFormatException e) {
      throw new DecompressZlibException(e);
    }
  }

  @SuppressWarnings("WhitespaceAround")
  private BatchStatus batchStatus(Batch batch) {
    State state = this.state.get();
    Epoch epoch = state.getSafeEpoch();
    Epoch nextEpoch = state.epoch(epoch.number().add(BigInteger.ONE));
    BlockInfo head = state.getSafeHead();
    BigInteger nextTimestamp = head.timestamp().add(this.config.chainConfig().blockTime());

    // check timestamp range
    switch (batch.timestamp().compareTo(nextTimestamp)) {
      case 1 -> {
        return BatchStatus.Future;
      }
      case -1 -> {
        return BatchStatus.Drop;
      }
      default -> {}
    }

    // check that block builds on existing chain
    if (!batch.parentHash().equalsIgnoreCase(head.hash())) {
      LOGGER.warn("invalid parent hash");
      return BatchStatus.Drop;
    }

    // check the inclusion delay
    if (batch
            .epochNum()
            .add(this.config.chainConfig().seqWindowSize())
            .compareTo(batch.l1InclusionBlock())
        < 0) {
      LOGGER.warn("inclusion window elapsed");
      return BatchStatus.Drop;
    }

    Epoch batchOrigin;
    // check and set batch origin epoch
    if (batch.epochNum().compareTo(epoch.number()) == 0) {
      batchOrigin = epoch;
    } else if (batch.epochNum().compareTo(epoch.number().add(BigInteger.ONE)) == 0) {
      batchOrigin = nextEpoch;
    } else {
      LOGGER.warn("invalid batch origin epoch number");
      return BatchStatus.Drop;
    }

    if (batchOrigin != null) {
      if (!batch.epochHash().equalsIgnoreCase(batchOrigin.hash())) {
        LOGGER.warn("invalid epoch hash");
        return BatchStatus.Drop;
      }

      if (batch.timestamp().compareTo(batchOrigin.timestamp()) < 0) {
        LOGGER.warn("batch too old");
        return BatchStatus.Drop;
      }

      // handle sequencer drift
      if (batch
              .timestamp()
              .compareTo(batchOrigin.timestamp().add(this.config.chainConfig().maxSeqDrift()))
          > 0) {
        if (batch.transactions().isEmpty()) {
          if (epoch.number().compareTo(batch.epochNum()) == 0) {
            if (nextEpoch != null) {
              if (batch.timestamp().compareTo(nextEpoch.timestamp()) >= 0) {
                LOGGER.warn("sequencer drift too large");
                return BatchStatus.Drop;
              }
            } else {
              return BatchStatus.Undecided;
            }
          }
        } else {
          LOGGER.warn("sequencer drift too large");
          return BatchStatus.Drop;
        }
      }
    } else {
      return BatchStatus.Undecided;
    }

    if (batch.hasInvalidTransactions()) {
      LOGGER.warn("invalid transaction");
      return BatchStatus.Drop;
    }

    return BatchStatus.Accept;
  }

  /**
   * Create batches.
   *
   * @param <I> the type parameter
   * @param channelIterator the channel iterator
   * @param state the state
   * @param config the config
   * @return the batches
   */
  public static <I extends PurgeableIterator<Channel>> Batches<I> create(
      I channelIterator, AtomicReference<State> state, Config config) {
    return new Batches<>(new TreeMap<>(), channelIterator, state, config);
  }

  /**
   * The enum BatchStatus.
   *
   * @author grapebaba
   * @since 0.1.0
   */
  public enum BatchStatus {
    /** Drop batch status. */
    Drop,
    /** Accept batch status. */
    Accept,
    /** Undecided batch status. */
    Undecided,
    /** Future batch status. */
    Future,
  }

  /**
   * The type Batch.
   *
   * @param parentHash the parent hash
   * @param epochNum the epoch num
   * @param epochHash the epoch hash
   * @param timestamp the timestamp
   * @param transactions the transactions
   * @param l1InclusionBlock L1 inclusion block
   * @author grapebaba
   * @since 0.1.0
   */
  public record Batch(
      String parentHash,
      BigInteger epochNum,
      String epochHash,
      BigInteger timestamp,
      List<String> transactions,
      BigInteger l1InclusionBlock) {

    /**
     * Decode batch.
     *
     * @param rlp the rlp
     * @param l1InclusionBlock L1 inclusion block
     * @return the batch
     */
    public static Batch decode(RlpList rlp, BigInteger l1InclusionBlock) {
      String parentHash = ((RlpString) rlp.getValues().get(0)).asString();
      BigInteger epochNum = ((RlpString) rlp.getValues().get(1)).asPositiveBigInteger();
      String epochHash = ((RlpString) rlp.getValues().get(2)).asString();
      BigInteger timestamp = ((RlpString) rlp.getValues().get(3)).asPositiveBigInteger();
      List<String> transactions =
          ((RlpList) rlp.getValues().get(4))
              .getValues().stream().map(rlpString -> ((RlpString) rlpString).asString()).toList();
      return new Batch(parentHash, epochNum, epochHash, timestamp, transactions, l1InclusionBlock);
    }

    /**
     * Has invalid transactions boolean.
     *
     * @return the boolean
     */
    public boolean hasInvalidTransactions() {
      return this.transactions.stream()
          .anyMatch(
              s ->
                  StringUtils.isEmpty(s)
                      || (Numeric.containsHexPrefix("0x")
                          ? StringUtils.startsWithIgnoreCase(s, "0x7E")
                          : StringUtils.startsWithIgnoreCase(s, "7E")));
    }
  }
}
