package io.optimism.batcher;

import com.google.common.util.concurrent.AbstractExecutionThreadService;
import io.optimism.batcher.channel.ChannelConfig;
import io.optimism.batcher.channel.ChannelManager;
import io.optimism.batcher.compressor.CompressorConfig;
import io.optimism.batcher.config.Config;
import io.optimism.batcher.exception.BatcherExecutionException;
import io.optimism.batcher.loader.BlockLoader;
import io.optimism.batcher.loader.LoaderConfig;
import io.optimism.batcher.publisher.ChannelDataPublisher;
import io.optimism.batcher.publisher.PublisherConfig;
import io.optimism.type.BlockId;
import io.optimism.utilities.derive.stages.Frame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

/**
 * BatcherSubmitter class.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
@SuppressWarnings("UnusedVariable")
public class BatcherSubmitter extends AbstractExecutionThreadService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatcherSubmitter.class);

    private final Config config;

    private final ChannelManager channelManager;
    private final BlockLoader blockLoader;
    private final ChannelDataPublisher channelPublisher;

    private volatile boolean isShutdownTriggered = false;

    /**
     * Constructor of BatcherSubmitter.
     *
     * @param config BatcherSubmitter config
     */
    public BatcherSubmitter(Config config) {
        this.config = config;
        this.channelManager = new ChannelManager(ChannelConfig.from(config), CompressorConfig.from(config));
        this.blockLoader = new BlockLoader(LoaderConfig.from(config), this.channelManager::addL2Block);
        this.blockLoader.init();
        this.channelPublisher = new ChannelDataPublisher(
                PublisherConfig.from(config, this.blockLoader.getRollConfig()),
                this.channelManager::txData,
                this::handleReceipt);
    }

    private void trySubmitBatchData() {
        this.blockLoader.loadBlock();
        // If no data has been sent, then sleep for a period of time.
        if (!this.channelPublisher.publishPendingBlock()) {
            try {
                Thread.sleep(config.pollInterval());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new BatcherExecutionException("Batcher thread has been interrupted", e);
            }
        }
    }

    private void handleReceipt(Frame tx, TransactionReceipt receipt) {
        if (receipt.isStatusOK()) {
            this.channelManager.txConfirmed(tx, new BlockId(receipt.getBlockHash(), receipt.getBlockNumber()));
        } else {
            this.channelManager.txFailed(tx);
        }
    }

    @Override
    protected void run() {
        while (isRunning() && !this.isShutdownTriggered) {
            this.trySubmitBatchData();
        }
    }

    @Override
    protected void shutDown() throws Exception {
        super.shutDown();
        this.blockLoader.close();
        this.channelPublisher.close();
        this.channelManager.clear();
    }

    @Override
    protected void triggerShutdown() {
        this.isShutdownTriggered = true;
    }
}
