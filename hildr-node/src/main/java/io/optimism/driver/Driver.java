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
import static org.web3j.protocol.core.DefaultBlockParameterName.FINALIZED;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import io.optimism.common.BlockInfo;
import io.optimism.common.Epoch;
import io.optimism.common.HildrServiceExecutionException;
import io.optimism.config.Config;
import io.optimism.derive.Pipeline;
import io.optimism.engine.Engine;
import io.optimism.engine.EngineApi;
import io.optimism.engine.ExecutionPayload;
import io.optimism.engine.ExecutionPayload.PayloadAttributes;
import io.optimism.l1.BlockUpdate;
import io.optimism.l1.ChainWatcher;
import io.optimism.telemetry.InnerMetrics;
import java.math.BigInteger;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import jdk.incubator.concurrent.StructuredTaskScope;
import org.jctools.queues.MessagePassingQueue;
import org.jctools.queues.MpscUnboundedXaddArrayQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

/**
 * The type Driver.
 *
 * @param <E> the type parameter
 * @author grapebaba
 * @since 0.1.0
 */
public class Driver<E extends Engine> extends AbstractExecutionThreadService {

  private static final Logger LOGGER = LoggerFactory.getLogger(Driver.class);
  private Pipeline pipeline;

  private EngineDriver<E> engineDriver;

  private List<UnfinalizedBlock> unfinalizedBlocks;

  private BigInteger finalizedL1BlockNumber;

  private List<ExecutionPayload> futureUnsafeBlocks;

  private AtomicReference<io.optimism.derive.State> state;

  private ChainWatcher chainWatcher;

  private MessagePassingQueue<ExecutionPayload> unsafeBlockQueue;

  private Executor executor;

  /**
   * Instantiates a new Driver.
   *
   * @param pipeline the pipeline
   * @param state the state
   * @param chainWatcher the chain watcher
   * @param unsafeBlockQueue the unsafe block queue
   * @param engineDriver the engine driver
   */
  @SuppressWarnings("preview")
  public Driver(
      EngineDriver<E> engineDriver,
      Pipeline pipeline,
      AtomicReference<io.optimism.derive.State> state,
      ChainWatcher chainWatcher,
      MessagePassingQueue<ExecutionPayload> unsafeBlockQueue) {
    this.engineDriver = engineDriver;
    this.pipeline = pipeline;
    this.state = state;
    this.chainWatcher = chainWatcher;
    this.unsafeBlockQueue = unsafeBlockQueue;
    this.futureUnsafeBlocks = Lists.newArrayList();
    this.unfinalizedBlocks = Lists.newArrayList();
    this.executor = Executors.newVirtualThreadPerTaskExecutor();
  }

  public EngineDriver<E> getEngineDriver() {
    return engineDriver;
  }

  /**
   * From driver.
   *
   * @param config the config
   * @return the driver
   * @throws InterruptedException the interrupted exception
   * @throws ExecutionException the execution exception
   */
  public static Driver<EngineApi> from(Config config)
      throws InterruptedException, ExecutionException {
    Web3j provider = Web3j.build(new HttpService(config.l2RpcUrl()));

    EthBlock finalizedBlock;
    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
      Future<EthBlock> finalizedBlockFuture =
          scope.fork(() -> provider.ethGetBlockByNumber(FINALIZED, true).send());
      scope.join();
      scope.throwIfFailed();

      finalizedBlock = finalizedBlockFuture.resultNow();
    }

    HeadInfo head;
    if (finalizedBlock == null) {
      LOGGER.warn("could not get head info. Falling back to the genesis head.");
      head =
          new HeadInfo(
              config.chainConfig().l2Genesis(),
              config.chainConfig().l1StartEpoch(),
              BigInteger.ZERO);
    } else {
      head = HeadInfo.from(finalizedBlock.getBlock());
    }

    BlockInfo finalizedHead = head.l2BlockInfo();
    Epoch finalizedEpoch = head.l1Epoch();
    BigInteger finalizedSeq = head.sequenceNumber();

    LOGGER.info("starting from head: {}", finalizedHead.hash());

    ChainWatcher watcher =
        new ChainWatcher(finalizedEpoch.number(), finalizedHead.number(), config);

    AtomicReference<io.optimism.derive.State> state =
        new AtomicReference<>(
            io.optimism.derive.State.create(finalizedHead, finalizedEpoch, config));

    EngineDriver<EngineApi> engineDriver =
        new EngineDriver<>(finalizedHead, finalizedEpoch, provider, config);

    Pipeline pipeline = new Pipeline(state, config, finalizedSeq);

    // TODO: RPC SERVER
    MpscUnboundedXaddArrayQueue<ExecutionPayload> unsafeBlockQueue =
        new MpscUnboundedXaddArrayQueue<>(1024 * 64);
    return new Driver<>(engineDriver, pipeline, state, watcher, unsafeBlockQueue);
  }

  @Override
  protected void run() {
    while (isRunning()) {
      try {
        this.advance();
      } catch (InterruptedException e) {
        LOGGER.error("driver run interrupted", e);
        Thread.currentThread().interrupt();
        throw new HildrServiceExecutionException(e);
      } catch (ExecutionException e) {
        LOGGER.error("driver run fatal error", e);
        throw new HildrServiceExecutionException(e);
      }
    }
  }

  @Override
  protected void startUp() {
    try {
      this.awaitEngineReady();
    } catch (InterruptedException e) {
      LOGGER.error("driver run interrupted", e);
      Thread.currentThread().interrupt();
      throw new HildrServiceExecutionException(e);
    }
    this.chainWatcher.start();
  }

  @Override
  protected Executor executor() {
    return this.executor;
  }

  @Override
  protected void shutDown() {
    this.chainWatcher.stop();
  }

  private void awaitEngineReady() throws InterruptedException {
    while (!this.engineDriver.engineReady()) {
      sleep(Duration.ofSeconds(1));
    }
  }

  @SuppressWarnings("VariableDeclarationUsageDistance")
  private void advance() throws InterruptedException, ExecutionException {
    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
      var voidFuture =
          scope.fork(
              (Callable<Void>)
                  () -> {
                    Driver.this.advanceSafeHead();
                    return null;
                  });

      var voidFuture1 =
          scope.fork(
              (Callable<Void>)
                  () -> {
                    Driver.this.advanceUnsafeHead();
                    return null;
                  });

      scope.join();
      scope.throwIfFailed();
      voidFuture.resultNow();
      voidFuture1.resultNow();
    }

    this.updateFinalized();
    this.updateMetrics();
  }

  private void advanceSafeHead() throws ExecutionException, InterruptedException {
    this.handleNextBlockUpdate();
    this.updateStateHead();

    while (this.pipeline.hasNext()) {
      PayloadAttributes payloadAttributes = this.pipeline.next();
      BigInteger l1InclusionBlock = payloadAttributes.l1InclusionBlock();
      if (l1InclusionBlock == null) {
        throw new InvalidAttributesException("attributes without inclusion block");
      }

      BigInteger seqNumber = payloadAttributes.seqNumber();
      if (seqNumber == null) {
        throw new InvalidAttributesException("attributes without seq number");
      }

      Driver.this.engineDriver.handleAttributes(payloadAttributes);

      LOGGER.info(
          "safe head updated: {} {}",
          Driver.this.engineDriver.getSafeHead().number(),
          Driver.this.engineDriver.getSafeHead().hash());

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
                      && unsafeBlockNum.subtract(syncedBlockNum).compareTo(BigInteger.valueOf(256L))
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
      case BlockUpdate.NewBlock l1info -> {
        BigInteger num = l1info.get().blockInfo().number();
        Driver.this.pipeline.pushBatcherTransactions(
            l1info.get().batcherTransactions().stream().map(Numeric::hexStringToByteArray).toList(),
            num);

        Driver.this.state.getAndUpdate(
            state -> {
              state.updateL1Info(((BlockUpdate.NewBlock) next).get());
              return state;
            });
      }
      case BlockUpdate.Reorg ignored -> {
        LOGGER.warn("reorg detected, purging pipeline");
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
      case BlockUpdate.FinalityUpdate num -> Driver.this.finalizedL1BlockNumber = num.get();
      default -> throw new IllegalArgumentException("unknown block update type");
    }
  }

  private void updateFinalized() {
    UnfinalizedBlock newFinalized =
        Iterables.getLast(
            this.unfinalizedBlocks.stream()
                .filter(
                    unfinalizedBlock ->
                        unfinalizedBlock
                                    .l1InclusionBlock()
                                    .compareTo(Driver.this.finalizedL1BlockNumber)
                                <= 0
                            && unfinalizedBlock.seqNumber().compareTo(BigInteger.ZERO) == 0)
                .toList(),
            null);

    if (newFinalized != null) {
      this.engineDriver.updateFinalized(newFinalized.head(), newFinalized.epoch());
    }

    this.unfinalizedBlocks =
        this.unfinalizedBlocks.stream()
            .filter(
                unfinalizedBlock ->
                    unfinalizedBlock
                            .l1InclusionBlock()
                            .compareTo(Driver.this.finalizedL1BlockNumber)
                        > 0)
            .toList();
  }

  private void updateMetrics() {
    InnerMetrics.setFinalizedHead(this.engineDriver.getFinalizedHead().number());
    InnerMetrics.setSafeHead(this.engineDriver.getSafeHead().number());
    InnerMetrics.setSynced(this.unfinalizedBlocks.isEmpty() ? BigInteger.ZERO : BigInteger.ONE);
  }

  private boolean synced() {
    return !this.unfinalizedBlocks.isEmpty();
  }

  /**
   * The type Unfinalized block.
   *
   * @param head the head
   * @param epoch the epoch
   * @param l1InclusionBlock the L1 inclusion block
   * @param seqNumber the seq number
   */
  protected record UnfinalizedBlock(
      BlockInfo head, Epoch epoch, BigInteger l1InclusionBlock, BigInteger seqNumber) {}
}
