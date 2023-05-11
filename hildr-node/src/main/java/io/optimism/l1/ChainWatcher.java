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

package io.optimism.l1;

import io.optimism.config.Config;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.jctools.queues.MpscBlockingConsumerArrayQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.tuples.generated.Tuple2;

/**
 * the ChainWatcher class.
 *
 * @author thinkAfCod
 * @since 0.1.0
 */
@SuppressWarnings("UnusedVariable")
public class ChainWatcher {

  private static final Logger LOGGER = LoggerFactory.getLogger(ChainWatcher.class);

  private Future<Void> handle;

  private final Config config;

  private BigInteger l1StartBlock;

  private BigInteger l2StartBlock;

  private Queue<BlockUpdate> blockUpdateReceiver;

  /**
   * the ChainWatcher constructor.
   *
   * @param l1StartBlock the start block number of l1
   * @param l2StartBlock the start block number of l2
   * @param config the global config
   */
  public ChainWatcher(BigInteger l1StartBlock, BigInteger l2StartBlock, Config config) {
    this.config = config;
    this.l1StartBlock = l1StartBlock;
    this.l2StartBlock = l2StartBlock;
    this.blockUpdateReceiver = new MpscBlockingConsumerArrayQueue<>(1000);
  }

  /** start ChainWatcher. */
  public void start() {
    if (handle != null && !handle.isDone()) {
      handle.cancel(true);
    }

    Tuple2<CompletableFuture<Void>, BlockingQueue<BlockUpdate>> tuple =
        startWatcher(this.l1StartBlock, this.l2StartBlock, this.config);
    this.handle = tuple.component1();
    this.blockUpdateReceiver = tuple.component2();
  }

  /**
   * restart ChainWatcher with new block number.
   *
   * @param l1StartBlock the start block number of l1
   * @param l2StartBlock the start block number of l2
   */
  public void restart(BigInteger l1StartBlock, BigInteger l2StartBlock) {
    if (handle != null && !handle.isDone()) {
      handle.cancel(true);
    }
    Tuple2<CompletableFuture<Void>, BlockingQueue<BlockUpdate>> tuple =
        startWatcher(l1StartBlock, l2StartBlock, this.config);
    this.handle = tuple.component1();
    this.blockUpdateReceiver = tuple.component2();
    this.l1StartBlock = l1StartBlock;
    this.l2StartBlock = l2StartBlock;
  }

  /** stop the ChainWatcher. */
  public void stop() {
    if (handle != null && !handle.isDone()) {
      handle.cancel(true);
    }
  }

  private static Tuple2<CompletableFuture<Void>, BlockingQueue<BlockUpdate>> startWatcher(
      BigInteger l1StartBlock, BigInteger l2StartBlock, Config config) {
    final BlockingQueue<BlockUpdate> queue = new MpscBlockingConsumerArrayQueue<>(1000);
    CompletableFuture<Void> future =
        CompletableFuture.runAsync(
            () -> {
              try {
                final InnerWatcher watcher =
                    new InnerWatcher(config, queue, l1StartBlock, l2StartBlock);
                while (!Thread.currentThread().isInterrupted()) {
                  LOGGER.debug("fetching L1 data for block {}", watcher.currentBlock);
                  try {
                    watcher.tryIngestBlock();
                  } catch (IOException e) {
                    LOGGER.warn("failed to fetch data for block {}: {}", watcher.currentBlock, e);
                  }
                }
                LOGGER.debug("thread has been interrupted");
              } catch (IOException e) {
                LOGGER.error("failed to fetch data");
              } catch (InterruptedException e) {
                LOGGER.error("thread has been interrupted", e);
              } catch (ExecutionException e) {
                LOGGER.error("execution exception", e);
              }
            });
    return new Tuple2<>(future, queue);
  }
}
