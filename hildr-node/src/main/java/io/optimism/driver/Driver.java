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

import com.google.common.collect.Iterables;
import io.optimism.common.BlockInfo;
import io.optimism.common.Epoch;
import io.optimism.derive.Pipeline;
import io.optimism.derive.State;
import io.optimism.engine.Engine;
import io.optimism.engine.ExecutionPayload;
import io.optimism.l1.BlockUpdate;
import io.optimism.l1.ChainWatcher;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import org.jctools.queues.MessagePassingQueue;
import org.web3j.utils.Numeric;

/**
 * The type Driver.
 *
 * @param <E> the type parameter
 * @author grapebaba
 * @since 0.1.0
 */
public class Driver<E extends Engine> {

  private Pipeline pipeline;

  private EngineDriver<E> engineDriver;

  private List<UnfinalizedBlock> unfinalizedBlocks;

  private BigInteger finalizedL1BlockNumber;

  private List<ExecutionPayload> futureUnsafeBlocks;

  private AtomicReference<State> state;

  private ChainWatcher chainWatcher;

  private MessagePassingQueue<ExecutionPayload> unsafeBlockQueue;

  /** Instantiates a new Driver. */
  public Driver() {}

  @SuppressWarnings("preview")
  private CompletableFuture<Void> handleNextBlockUpdate() {
    CompletableFuture<Void> res = CompletableFuture.completedFuture(null);
    boolean isStateFull = this.state.get().isFull();
    if (isStateFull) {
      return res;
    }
    BlockUpdate next = this.chainWatcher.getBlockUpdateQueue().poll();
    if (next == null) {
      return res;
    }

    switch (next) {
      case BlockUpdate.NewBlock newBlock -> {
        return res.thenAccept(
            unused -> {
              BigInteger num = newBlock.get().blockInfo().number();
              Driver.this.pipeline.pushBatcherTransactions(
                  newBlock.get().batcherTransactions().stream()
                      .map(Numeric::hexStringToByteArray)
                      .toList(),
                  num);
              Driver.this.state.getAndUpdate(
                  state -> {
                    state.updateL1Info(((BlockUpdate.NewBlock) next).get());
                    return state;
                  });
            });
      }
      case BlockUpdate.Reorg ignored -> {
        return res.thenAccept(
            unused -> {
              Driver.this.unfinalizedBlocks.clear();
              Driver.this.chainWatcher.restart(
                  Driver.this.engineDriver.getFinalizedEpoch().number(),
                  Driver.this.engineDriver.getFinalizedHead().number());
              Driver.this.state.getAndUpdate(
                  state -> {
                    state.purge(
                        Driver.this.engineDriver.getFinalizedHead(),
                        Driver.this.engineDriver.getFinalizedEpoch());
                    return state;
                  });
              Driver.this.pipeline.purge();
              Driver.this.engineDriver.reorg();
            });
      }
      case BlockUpdate.FinalityUpdate finalityUpdate -> {
        return res.thenAccept(
            unused -> {
              Driver.this.finalizedL1BlockNumber = finalityUpdate.get();
            });
      }
      default -> {
        return res;
      }
    }
  }

  private void updateFinalized() {
    UnfinalizedBlock newFinalized =
        Iterables.getLast(
            this.unfinalizedBlocks.stream()
                .filter(
                    unfinalizedBlock ->
                        unfinalizedBlock.l1InclusionBlock.compareTo(
                                    Driver.this.finalizedL1BlockNumber)
                                <= 0
                            && unfinalizedBlock.seqNumber.compareTo(BigInteger.ZERO) == 0)
                .toList(),
            null);

    if (newFinalized != null) {
      this.engineDriver.updateFinalized(newFinalized.head, newFinalized.epoch);
    }

    this.unfinalizedBlocks =
        this.unfinalizedBlocks.stream()
            .filter(
                unfinalizedBlock ->
                    unfinalizedBlock.l1InclusionBlock.compareTo(Driver.this.finalizedL1BlockNumber)
                        > 0)
            .toList();
  }

  // TODO: add metrics
  private void updateMetrics() {}

  private record UnfinalizedBlock(
      BlockInfo head, Epoch epoch, BigInteger l1InclusionBlock, BigInteger seqNumber) {}
}
