package io.optimism.batcher.telemetry;

import io.optimism.type.L1BlockRef;
import io.optimism.type.L2BlockRef;
import io.optimism.utilities.derive.stages.Frame;
import org.web3j.protocol.core.methods.response.EthBlock;

/**
 * The batcher metrics interface.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public interface BatcherMetrics {

    /**
     * Record latest L1 block ref.
     *
     * @param l1ref Latest L1 block ref
     */
    void recordLatestL1Block(L1BlockRef l1ref);

    /**
     * Record L2 block loaded.
     *
     * @param l2ref L2 block ref
     */
    void recordL2BlocksLoaded(L2BlockRef l2ref);

    /**
     * Record channel open event.
     *
     * @param frame Channel frame
     * @param numPendingBlocks Number of pending blocks
     */
    void recordChannelOpened(Frame frame, int numPendingBlocks);

    /**
     * Record L2 block added to channel manager.
     *
     * @param l2ref L2 block ref
     * @param numBlocksAdded Number of added blocks
     * @param numPendingBlocks Number of pending blocks
     * @param inputBytes Length of channel input bytes
     * @param outputComprBytes Length of channel compressed bytes
     */
    void recordL2BlocksAdded(
            L2BlockRef l2ref, int numBlocksAdded, int numPendingBlocks, int inputBytes, int outputComprBytes);

    /**
     * Record L2 block in pending queue.
     *
     * @param block L2 block
     */
    void recordL2BlockInPendingQueue(EthBlock.Block block);

    /**
     * Record L2 block put into channel.
     *
     * @param block L2 block
     */
    void recordL2BlockInChannel(EthBlock.Block block);

    /**
     * Record channel closed event.
     *
     * @param frame Channel frame.
     * @param numPendingBlocks Number of pending blocks
     * @param numFrames Number of channel frames
     * @param inputBytes Length of input bytes
     * @param outputComprBytes Length of output compressed bytes
     * @param errorReason Message of error reason
     */
    void recordChannelClosed(
            Frame frame, int numPendingBlocks, int numFrames, int inputBytes, int outputComprBytes, String errorReason);

    /**
     * Record channel fully submitted event.
     *
     * @param frame Channel frame
     */
    void recordChannelFullySubmitted(Frame frame);

    /**
     * Record channel timeout event.
     *
     * @param frame Channel frame
     */
    void recordChannelTimedOut(Frame frame);

    /** Record batch tx submitted event. */
    void recordBatchTxSubmitted();

    /** Record batch tx success event. */
    void recordBatchTxSuccess();

    /** Record batch tx failed event. */
    void recordBatchTxFailed();

    /**
     * Record batcher server version.
     *
     * @param version Batcher server version
     */
    void recordInfo(String version);

    /** Record batcher server has been started. */
    void recordUp();
}
