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

import io.optimism.common.BlockInfo;
import io.optimism.config.Config;
import java.math.BigInteger;
import java.util.concurrent.Executors;
import org.jctools.queues.MessagePassingQueue;
import org.jctools.queues.MpscGrowableArrayQueue;

/**
 * the ChainWatcher class.
 *
 * @author thinkAfCod
 * @since 0.1.0
 */
@SuppressWarnings({"UnusedVariable"})
public class ChainWatcher {

    private volatile MessagePassingQueue<BlockUpdate> blockUpdateQueue;
    private volatile InnerWatcher innerWatcher;
    private final Config config;

    /**
     * Gets block update queue.
     *
     * @return the block update queue
     */
    public MessagePassingQueue<BlockUpdate> getBlockUpdateQueue() {
        return blockUpdateQueue;
    }

    /**
     * the ChainWatcher constructor.
     *
     * @param l1StartBlock the start block number of l1
     * @param l2StartBlock the start block number of l2
     * @param config the global config
     */
    public ChainWatcher(BigInteger l1StartBlock, BigInteger l2StartBlock, Config config) {
        this.config = config;
        this.blockUpdateQueue = new MpscGrowableArrayQueue<>(1024 * 4, 1024 * 64);
        this.innerWatcher = new InnerWatcher(
                this.config,
                this.blockUpdateQueue,
                l1StartBlock,
                l2StartBlock,
                Executors.newVirtualThreadPerTaskExecutor());
    }

    /** start ChainWatcher. */
    public void start() {
        innerWatcher.startAsync().awaitRunning();
    }

    /** stop the ChainWatcher. */
    public void stop() {
        innerWatcher.stopAsync().awaitTerminated();
    }

    /**
     * Restart.
     *
     * @param l1StartBlock new l1 start block number
     * @param l2StartBlock new l2 start block number
     */
    public void restart(BigInteger l1StartBlock, BigInteger l2StartBlock) {
        this.stop();
        this.blockUpdateQueue = new MpscGrowableArrayQueue<>(1024 * 4, 1024 * 64);
        this.innerWatcher = new InnerWatcher(
                this.config,
                this.blockUpdateQueue,
                l1StartBlock,
                l2StartBlock,
                Executors.newVirtualThreadPerTaskExecutor());
        this.start();
    }

    /**
     * Gets current L1 block info.
     *
     * @return the current L1 block info.
     */
    public BlockInfo getCurrentL1() {
        return this.innerWatcher.getCurrentL1();
    }

    /**
     * Gets current L1 finalized block info.
     *
     * @return the current L1 finalized block info.
     */
    public BlockInfo getCurrentL1Finalized() {
        return this.innerWatcher.getCurrentL1Finalized();
    }

    /**
     * Gets L1 head block info.
     *
     * @return L1 head block info.
     */
    public BlockInfo getL1HeadBlock() {
        return this.innerWatcher.getL1HeadBlock();
    }

    /**
     * Gets L1 safe block info.
     *
     * @return L1 safe block info.
     */
    public BlockInfo getL1SafeBlock() {
        return this.innerWatcher.getL1SafeBlock();
    }

    /**
     * Gets L1 finalized block info.
     *
     * @return L1 finalized block info.
     */
    public BlockInfo getL1FinalizedBlock() {
        return this.innerWatcher.getL1FinalizedBlock();
    }

    /**
     * Gets current rollup system config info.
     *
     * @return rollup system config info.
     */
    public Config.SystemConfig getSystemConfig() {
        return this.innerWatcher.getSystemConfig();
    }
}
