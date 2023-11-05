/*
 * Copyright 2023 q315xia@163.com
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

package io.optimism.batcher.telemetry;

import io.optimism.type.L1BlockRef;
import io.optimism.type.L2BlockRef;
import io.optimism.utilities.derive.stages.Frame;
import org.web3j.protocol.core.methods.response.EthBlock;

/**
 * An empty metrics when not enable open a metrics.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public class NoopBatcherMetrics implements BatcherMetrics {

    /** The NoopBatcherMetrics constructor. */
    public NoopBatcherMetrics() {}

    @Override
    public void recordLatestL1Block(L1BlockRef l1ref) {}

    @Override
    public void recordL2BlocksLoaded(L2BlockRef l2ref) {}

    @Override
    public void recordChannelOpened(Frame frame, int numPendingBlocks) {}

    @Override
    public void recordL2BlocksAdded(
            L2BlockRef l2ref, int numBlocksAdded, int numPendingBlocks, int inputBytes, int outputComprBytes) {}

    @Override
    public void recordL2BlockInPendingQueue(EthBlock.Block block) {}

    @Override
    public void recordL2BlockInChannel(EthBlock.Block block) {}

    @Override
    public void recordChannelClosed(
            Frame frame,
            int numPendingBlocks,
            int numFrames,
            int inputBytes,
            int outputComprBytes,
            String errorReason) {}

    @Override
    public void recordChannelFullySubmitted(Frame frame) {}

    @Override
    public void recordChannelTimedOut(Frame frame) {}

    @Override
    public void recordBatchTxSubmitted() {}

    @Override
    public void recordBatchTxSuccess() {}

    @Override
    public void recordBatchTxFailed() {}

    @Override
    public void recordInfo(String version) {}

    @Override
    public void recordUp() {}
}
