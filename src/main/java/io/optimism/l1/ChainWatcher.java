package io.optimism.l1;

import io.optimism.config.Config;
import io.optimism.types.BlockInfo;
import io.optimism.types.BlockUpdate;
import java.math.BigInteger;
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
        this.innerWatcher = new InnerWatcher(this.config, this.blockUpdateQueue, l1StartBlock, l2StartBlock);
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
        this.innerWatcher = new InnerWatcher(this.config, this.blockUpdateQueue, l1StartBlock, l2StartBlock);
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
