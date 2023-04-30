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

import static io.optimism.derive.stages.BatchStatus.Undecided;

import com.google.common.collect.AbstractIterator;
import io.optimism.common.BlockInfo;
import io.optimism.common.Epoch;
import io.optimism.config.Config;
import io.optimism.derive.PurgeableIterator;
import io.optimism.derive.State;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import org.apache.commons.lang3.ArrayUtils;
import org.web3j.rlp.RlpDecoder;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpString;
import org.web3j.rlp.RlpType;

/**
 * The type Batches.
 *
 * @param <I> the type parameter
 * @author grapebaba
 * @since 0.1.0
 */
public class Batches<I extends PurgeableIterator<Channel>> extends AbstractIterator<Batch>
    implements PurgeableIterator<Batch> {

  private TreeMap<BigInteger, Batch> batches;

  private I channelIterator;

  private AtomicReference<State> state;

  private Config config;

  /**
   * Instantiates a new Batches.
   *
   * @param batches the batches
   * @param channelIterator the channel iterator
   * @param state the state
   * @param config the config
   */
  public Batches(
      TreeMap<BigInteger, Batch> batches, I channelIterator, State state, Config config) {
    this.batches = batches;
    this.channelIterator = channelIterator;
    this.state = new AtomicReference<>(state);
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
                  List.of(),
                  currentL1Block);
        }
      }
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
      outputStream.close();
      return outputStream.toByteArray();
    } catch (IOException | DataFormatException e) {
      throw new DecompressZlibException(e);
    }
  }

  private BatchStatus batchStatus(Batch batch) {
    State state = this.state.get();
    Epoch epoch = state.getSafeEpoch();
    Epoch nextEpoch = state.epoch(epoch.number().add(BigInteger.ONE));
    BlockInfo head = state.getSafeHead();
    BigInteger nextTimestamp = head.timestamp().add(this.config.chainConfig().blockTime());

    // check timestamp range
    switch (batch.timestamp().compareTo(nextTimestamp)) {
      case 1:
        return BatchStatus.Future;
      case -1:
        return BatchStatus.Drop;
      default:
        break;
    }

    // check that block builds on existing chain
    if (!batch.parentHash().equalsIgnoreCase(head.hash())) {
      return BatchStatus.Drop;
    }

    // check the inclusion delay
    if (batch
            .epochNum()
            .add(this.config.chainConfig().seqWindowSize())
            .compareTo(batch.l1InclusionBlock())
        < 0) {
      return BatchStatus.Drop;
    }

    Epoch batchOrigin;
    // check and set batch origin epoch
    if (batch.epochNum().compareTo(epoch.number()) == 0) {
      batchOrigin = epoch;
    } else if (batch.epochNum().compareTo(epoch.number().add(BigInteger.ONE)) == 0) {
      batchOrigin = nextEpoch;
    } else {
      return BatchStatus.Drop;
    }

    if (batchOrigin != null) {
      if (!batch.epochHash().equalsIgnoreCase(batchOrigin.hash())) {
        return BatchStatus.Drop;
      }

      if (batch.timestamp().compareTo(batchOrigin.timestamp()) < 0) {
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
                return BatchStatus.Drop;
              }
            } else {
              return Undecided;
            }
          }
        } else {
          return BatchStatus.Drop;
        }
      }
    } else {
      return Undecided;
    }

    if (batch.hasInvalidTransactions()) {
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
      I channelIterator, State state, Config config) {
    return new Batches<>(new TreeMap<>(), channelIterator, state, config);
  }
}
