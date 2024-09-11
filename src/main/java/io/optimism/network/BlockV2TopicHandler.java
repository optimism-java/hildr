package io.optimism.network;

import io.optimism.types.ExecutionPayload;
import io.optimism.types.enums.BlockVersion;
import org.apache.tuweni.units.bigints.UInt64;
import org.jctools.queues.MessagePassingQueue;
import tech.pegasys.teku.infrastructure.async.AsyncRunner;
import tech.pegasys.teku.networking.p2p.gossip.PreparedGossipMessageFactory;

/**
 * The type BlockV2TopicHandler.
 *
 * @author grapebaba
 * @since 0.2.4
 */
public class BlockV2TopicHandler extends AbstractTopicHandler {

    /**
     * Instantiates a new BlockV2TopicHandler.
     *
     * @param preparedGossipMessageFactory the prepared gossip message factory
     * @param asyncRunner                  the async runner
     * @param chainId                      the chain id
     * @param unsafeBlockSigner            the unsafe block signer
     * @param unsafeBlockQueue             the unsafe block queue
     */
    public BlockV2TopicHandler(
            PreparedGossipMessageFactory preparedGossipMessageFactory,
            AsyncRunner asyncRunner,
            UInt64 chainId,
            String unsafeBlockSigner,
            MessagePassingQueue<ExecutionPayload> unsafeBlockQueue) {
        super(
                preparedGossipMessageFactory,
                String.format("/optimism/%s/%d/blocks", chainId.toString(), BlockVersion.V2.getVersion()),
                asyncRunner,
                chainId,
                unsafeBlockSigner,
                unsafeBlockQueue,
                BlockVersion.V2);
    }
}
