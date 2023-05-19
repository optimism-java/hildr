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

import static java.lang.Thread.sleep;

import com.google.common.collect.Iterables;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import io.optimism.common.BlockInfo;
import io.optimism.common.Epoch;
import io.optimism.derive.Pipeline;
import io.optimism.engine.Engine;
import io.optimism.engine.ExecutionPayload;
import io.optimism.engine.ExecutionPayload.PayloadAttributes;
import io.optimism.l1.BlockUpdate;
import io.optimism.l1.ChainWatcher;
import java.math.BigInteger;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import jdk.incubator.concurrent.StructuredTaskScope;
import org.jctools.queues.MessagePassingQueue;
import org.web3j.utils.Numeric;

/**
 * The type Driver.
 *
 * @param <E> the type parameter
 * @author grapebaba
 * @since 0.1.0
 */
public class Driver<E extends Engine> extends AbstractExecutionThreadService {

  private Pipeline pipeline;

  private EngineDriver<E> engineDriver;

  private List<UnfinalizedBlock> unfinalizedBlocks;

  private BigInteger finalizedL1BlockNumber;

  private List<ExecutionPayload> futureUnsafeBlocks;

  private AtomicReference<io.optimism.derive.State> state;

  private ChainWatcher chainWatcher;

  private MessagePassingQueue<ExecutionPayload> unsafeBlockQueue;

  private volatile boolean shutdownFlag;
  private Executor executor;

  /** Instantiates a new Driver. */
  public Driver() {}

  @Override
  protected void run() {
    while (isRunning()) {
      try {
        this.checkShutdown();
        this.advance();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException(e);
      } catch (ExecutionException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  protected void startUp() {
    try {
      this.awaitEngineReady();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new RuntimeException(e);
    }
    this.chainWatcher.start();
  }

  @Override
  protected Executor executor() {
    return this.executor;
  }

  private void checkShutdown() {
    if (shutdownFlag) {
      this.stopAsync();
      this.awaitTerminated();
    }
  }

  private void stop() {
    this.stopAsync();
    this.awaitTerminated();
  }

  private void awaitEngineReady() throws InterruptedException {
    while (!this.engineDriver.engineReady()) {
      checkShutdown();
      sleep(Duration.ofSeconds(1));
    }
  }

  private void advance() throws InterruptedException, ExecutionException {
    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
      scope.fork(
          (Callable<Void>)
              () -> {
                Driver.this.advanceSafeHead();
                return null;
              });

      scope.fork(
          (Callable<Void>)
              () -> {
                Driver.this.advanceUnsafeHead();
                return null;
              });

      scope.join();
      scope.throwIfFailed();
    }
  }

  private void advanceSafeHead() throws ExecutionException, InterruptedException {
    this.updateStateHead();

    while (this.pipeline.hasNext()) {
      PayloadAttributes payloadAttributes = this.pipeline.next();
      BigInteger l1InclusionBlock = payloadAttributes.l1InclusionBlock();
      if (l1InclusionBlock == null) {
        throw new RuntimeException("attributes without inclusion block");
      }

      BigInteger seqNumber = payloadAttributes.seqNumber();
      if (seqNumber == null) {
        throw new RuntimeException("attributes without seq number");
      }

      Driver.this.engineDriver.handleAttributes(payloadAttributes);

      BlockInfo newSafeHead = Driver.this.engineDriver.getSafeHead();
      Epoch newSafeEpoch = Driver.this.engineDriver.getSafeEpoch();

      UnfinalizedBlock newUnfinalizedBlock =
          new UnfinalizedBlock(newSafeHead, newSafeEpoch, l1InclusionBlock, seqNumber);

      Driver.this.unfinalizedBlocks.add(newUnfinalizedBlock);
    }
  }

  private void advanceUnsafeHead() throws ExecutionException, InterruptedException {
    for (ExecutionPayload payload = this.unsafeBlockQueue.poll();
        payload != null;
        payload = this.unsafeBlockQueue.poll()) {
      this.futureUnsafeBlocks.add(payload);
    }

    this.futureUnsafeBlocks =
        this.futureUnsafeBlocks.stream()
            .filter(
                payload -> {
                  BigInteger unsafeBlockNum = payload.blockNumber();
                  BigInteger syncedBlockNum = Driver.this.engineDriver.getUnsafeHead().number();
                  return unsafeBlockNum.compareTo(syncedBlockNum) > 0
                      && unsafeBlockNum.subtract(syncedBlockNum).compareTo(BigInteger.valueOf(256))
                          < 0;
                })
            .toList();

    Optional<ExecutionPayload> nextUnsafePayload =
        Iterables.tryFind(
                this.futureUnsafeBlocks,
                input ->
                    input
                        .parentHash()
                        .equalsIgnoreCase(Driver.this.engineDriver.getUnsafeHead().hash()))
            .toJavaUtil();

    if (nextUnsafePayload.isPresent()) {
      this.engineDriver.handleUnsafePayload(nextUnsafePayload.get());
    }
  }

  private void updateStateHead() {
    this.state.getAndUpdate(
        state -> {
          state.updateSafeHead(this.engineDriver.getSafeHead(), this.engineDriver.getSafeEpoch());
          return state;
        });
  }

  @SuppressWarnings("preview")
  private void handleNextBlockUpdate() {
    boolean isStateFull = this.state.get().isFull();
    if (isStateFull) {
      return;
    }
    BlockUpdate next = this.chainWatcher.getBlockUpdateQueue().poll();
    if (next == null) {
      return;
    }

    switch (next) {
      case BlockUpdate.NewBlock newBlock -> {
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
      }
      case BlockUpdate.Reorg ignored -> {
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
      }
      case BlockUpdate.FinalityUpdate finalityUpdate -> Driver.this.finalizedL1BlockNumber =
          finalityUpdate.get();
      default -> throw new RuntimeException("unknown block update type");
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
