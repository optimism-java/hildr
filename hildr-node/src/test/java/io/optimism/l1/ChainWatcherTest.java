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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import io.optimism.config.Config;
import java.math.BigInteger;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.web3j.tuples.generated.Tuple2;

/**
 * @author thinkAfCod
 * @since 2023.05
 */
public class ChainWatcherTest {

  Config createConfig() {
    Config.CliConfig cliConfig = new Config.CliConfig(null, null, null, "testjwt");
    return Config.create(null, cliConfig, Config.ChainConfig.optimismGoerli());
  }

  ChainWatcher createWatcher() {
    return new ChainWatcher(
        BigInteger.valueOf(10_000L), BigInteger.valueOf(20_000L), this.createConfig());
  }

  @Test
  void testStartWatcher() {
    ExecutorService mockExecutor = mock(ExecutorService.class);
    BigInteger l1StartBlock = BigInteger.valueOf(20_000L);
    BigInteger l2StartBlock = BigInteger.valueOf(10_000L);
    Tuple2<CompletableFuture<Void>, BlockingQueue<BlockUpdate>> tuple =
        ChainWatcher.startWatcher(mockExecutor, l1StartBlock, l2StartBlock, this.createConfig());
    assertNotNull(tuple.component1());
    assertNotNull(tuple.component2());
  }

  @Test
  void testWatcherStopTask() {
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Runnable runnable = ChainWatcher.watcherTask(executor, () -> mock(InnerWatcher.class));
    CompletableFuture<Void> future = CompletableFuture.runAsync(runnable, executor);
    var unused = future.cancel(true);
    assertThrows(
        CancellationException.class, future::get, "should throw interruptedException but not");
  }
}
