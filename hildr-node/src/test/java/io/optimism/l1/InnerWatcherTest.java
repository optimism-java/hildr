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
import java.util.concurrent.ExecutionException;
import org.jctools.queues.MpscBlockingConsumerArrayQueue;
import org.junit.Before;

/**
 * @author thinkAfCod
 * @since 2023.05
 */
public class InnerWatcherTest {

  private Config config;

  @Before
  void init() {
    this.initConfig();
  }

  void initConfig() {
    Config.CliConfig cliConfig = new Config.CliConfig(null, null, null, "testjwt");
    this.config = Config.create(null, cliConfig, Config.ChainConfig.optimismGoerli());
  }

  InnerWatcher createWatcher() throws IOException, ExecutionException, InterruptedException {
    return new InnerWatcher(
        this.config,
        new MpscBlockingConsumerArrayQueue<>(1000),
        BigInteger.valueOf(10_000L),
        BigInteger.valueOf(20_000L),
        null);
  }

  void testCreateInnerWatcher() {}
}
