package io.optimism.network;

import io.optimism.types.ExecutionPayload;
import io.optimism.types.enums.BlockVersion;
import org.apache.tuweni.units.bigints.UInt64;
import org.jctools.queues.MessagePassingQueue;
import tech.pegasys.teku.infrastructure.async.AsyncRunner;
import tech.pegasys.teku.networking.p2p.gossip.PreparedGossipMessageFactory;

/**
 * The type BlockV3TopicHandler.
 *
 * @author grapebaba
 * @since 0.2.6
 */
public class BlockV3TopicHandler extends AbstractTopicHandler {

    /**
     * Instantiates a new BlockV3TopicHandler.
     *
     * @param preparedGossipMessageFactory the prepared gossip message factory
     * @param asyncRunner                  the async runner
     * @param chainId                      the chain id
     * @param unsafeBlockSigner            the unsafe block signer
     * @param unsafeBlockQueue             the unsafe block queue
     */
    public BlockV3TopicHandler(
            PreparedGossipMessageFactory preparedGossipMessageFactory,
            AsyncRunner asyncRunner,
            UInt64 chainId,
            String unsafeBlockSigner,
            MessagePassingQueue<ExecutionPayload> unsafeBlockQueue) {
        super(
                preparedGossipMessageFactory,
                String.format("/optimism/%s/%d/blocks", chainId.toString(), BlockVersion.V3.getVersion()),
                asyncRunner,
                chainId,
                unsafeBlockSigner,
                unsafeBlockQueue,
                BlockVersion.V3);
    }
}
