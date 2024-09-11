package io.optimism.driver;

import io.optimism.types.ExecutionPayloadEnvelop;
import io.optimism.types.L2BlockRef;

/**
 * The sequencer Interface.
 * @author thinkAfCod
 * @since 0.4.1
 */
public interface ISequencer {

    /**
     * Starts new block building work, or seals existing work,
     * and is best timed by first awaiting the delay returned by PlanNextSequencerAction.
     * If a new block is successfully sealed, it will be returned for publishing, null otherwise.
     *
     * @return the execution payload envelop
     */
    ExecutionPayloadEnvelop runNextSequencerAction();

    /**
     * Returns a desired delay till the RunNextSequencerAction call.
     *
     * @return the next start sequencer action duration
     */
    long planNextSequencerAction();

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
