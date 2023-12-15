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

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.optimism.TestConstants;
import io.optimism.config.Config;
import java.math.BigInteger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jctools.queues.MessagePassingQueue;
import org.jctools.queues.MpscGrowableArrayQueue;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Test case of InnerWatcher.
 *
 * @author thinkAfCod
 * @since 2023.05
 */
public class InnerWatcherTest {

    private static Config config;

    private static ExecutorService executor;

    @BeforeAll
    static void setUp() {
        config = TestConstants.createConfig();
        executor = Executors.newSingleThreadExecutor();
    }

    @AfterAll
    static void tearDown() {
        executor.shutdownNow();
    }

    InnerWatcher createWatcher(
            BigInteger l2StartBlock, MessagePassingQueue<BlockUpdate> queue, ExecutorService executor) {
        var watcherl2StartBlock = l2StartBlock;
        if (l2StartBlock == null) {
            watcherl2StartBlock = config.chainConfig().l2Genesis().number();
        }
        return new InnerWatcher(
                config, queue, config.chainConfig().l1StartEpoch().number(), watcherl2StartBlock, executor);
    }

    @Test
    void testCreateInnerWatcher() {
        if (!TestConstants.isConfiguredApiKeyEnv) {
            return;
        }
        var queue = new MpscGrowableArrayQueue<BlockUpdate>(1024 * 4, 1024 * 64);
        var unused = this.createWatcher(null, queue, executor);
        unused = this.createWatcher(config.chainConfig().l2Genesis().number().add(BigInteger.TEN), queue, executor);
    }

    @Test
    void testTryIngestBlock() throws Exception {
        if (!TestConstants.isConfiguredApiKeyEnv) {
            return;
        }
        ExecutorService executor = Executors.newSingleThreadExecutor();
        var queue = new MpscGrowableArrayQueue<BlockUpdate>(1024 * 4, 1024 * 64);
        var watcher = this.createWatcher(null, queue, executor);
        watcher.startUp();
        watcher.tryIngestBlock();
        assertEquals(2, queue.size());
    }
}
