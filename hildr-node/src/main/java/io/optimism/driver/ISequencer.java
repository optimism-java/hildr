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

package io.optimism.driver;

import io.optimism.network.ExecutionPayloadEnvelop;
import io.optimism.type.L2BlockRef;
import java.time.Duration;

/**
 * The sequencer Interface.
 * @author thinkAfCod
 * @since 0.4.1
 */
public interface ISequencer {
    /**
     * Initiates a block building job on top of the given L2 head, safe and finalized blocks, and using the provided l1Origin.
     */
    void startBuildingBlock();

    /**
     * Takes the current block that is being built, and asks the engine to complete the building, seal the block, and persist it as canonical.
     * Warning: the safe and finalized L2 blocks as viewed during the initiation of the block building are reused for completion of the block building.
     * The Execution engine should not change the safe and finalized blocks between start and completion of block building.
     * @return the newest execution payload envelop
     */
    ExecutionPayloadEnvelop completeBuildingBlock();

    /**
     * Returns a desired delay till the RunNextSequencerAction call.
     * @return the next start sequencer action duration
     */
    Duration planNextSequencerAction();

    /**
     * Starts new block building work, or seals existing work,
     * and is best timed by first awaiting the delay returned by PlanNextSequencerAction.
     * If a new block is successfully sealed, it will be returned for publishing, null otherwise.
     * @return the execution payload envelop
     */
    ExecutionPayloadEnvelop runNextSequencerAction();

    /**
     * Returns the L2 head reference that the latest block is or was being built on top of.
     * @return the L2 head reference
     */
    L2BlockRef buildingOnto();

    /**
     * Cancels the current open block building job.
     * The sequencer only maintains one block building job at a time.
     */
    void cancelBuildingBlock();
}
