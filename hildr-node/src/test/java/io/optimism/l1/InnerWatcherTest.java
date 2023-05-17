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

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.optimism.config.Config;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jctools.queues.MpscBlockingConsumerArrayQueue;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * test case of InnerWatcher.
 *
 * @author thinkAfCod
 * @since 2023.05
 */
public class InnerWatcherTest {

  private static final String ETH_API_ENV = "ETH_API_KEY";
  private static final String OPT_API_ENV = "OPT_API_KEY";

  static String l1RpcUrlFormat = "https://eth-goerli.g.alchemy.com/v2/%s";
  static String l2RpcUrlFormat = "https://opt-goerli.g.alchemy.com/v2/%s";

  private static boolean isConfiguredApiKeyEnv = false;

  private static Config config;

  private static ExecutorService executor;

  @BeforeAll
  static void setUp() {
    config = createConfig();
    executor = Executors.newSingleThreadExecutor();
  }

  @AfterAll
  static void tearDown() {
    executor.shutdownNow();
  }

  static Config createConfig() {
    Map<String, String> envs = System.getenv();
    isConfiguredApiKeyEnv = envs.containsKey(ETH_API_ENV) && envs.containsKey(OPT_API_ENV);
    if (!isConfiguredApiKeyEnv) {
      return null;
    }
    var l1RpcUrl = l1RpcUrlFormat.formatted(envs.get(ETH_API_ENV));
    var l2RpcUrl = l2RpcUrlFormat.formatted(envs.get(OPT_API_ENV));
    Config.CliConfig cliConfig = new Config.CliConfig(l1RpcUrl, l2RpcUrl, null, "testjwt");
    return Config.create(null, cliConfig, Config.ChainConfig.optimismGoerli());
  }

  InnerWatcher createWatcher(
      BigInteger l2StartBlock, BlockingQueue<BlockUpdate> queue, ExecutorService executor)
      throws IOException, ExecutionException, InterruptedException {
    var watcherl2StartBlock = l2StartBlock;
    if (l2StartBlock == null) {
      watcherl2StartBlock = config.chainConfig().l2Genesis().number();
    }
    return new InnerWatcher(
        config, queue, config.chainConfig().l1StartEpoch().number(), watcherl2StartBlock, executor);
  }

  @Test
  void testCreateInnerWatcher() throws IOException, ExecutionException, InterruptedException {
    if (!isConfiguredApiKeyEnv) {
      return;
    }
    var queue = new MpscBlockingConsumerArrayQueue<BlockUpdate>(1000);
    var unused = this.createWatcher(null, queue, executor);
    unused =
        this.createWatcher(
            config.chainConfig().l2Genesis().number().add(BigInteger.TEN), queue, executor);
  }

  @Test
  void testTryIngestBlock() throws IOException, ExecutionException, InterruptedException {
    if (!isConfiguredApiKeyEnv) {
      return;
    }
    ExecutorService executor = Executors.newSingleThreadExecutor();
    var queue = new MpscBlockingConsumerArrayQueue<BlockUpdate>(1000);
    var watcher = this.createWatcher(null, queue, executor);
    watcher.tryIngestBlock().get();
    assertTrue(queue.size() != 0);
  }
}
