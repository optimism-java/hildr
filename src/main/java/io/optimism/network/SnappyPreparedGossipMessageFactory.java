package io.optimism.network;

import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.teku.networking.p2p.gossip.PreparedGossipMessage;
import tech.pegasys.teku.networking.p2p.gossip.PreparedGossipMessageFactory;
import tech.pegasys.teku.spec.config.NetworkingSpecConfig;

/**
 * The type SnappyPreparedGossipMessageFactory.
 *
 * @author grapebaba
 * @since 0.1.1
 */
public class SnappyPreparedGossipMessageFactory implements PreparedGossipMessageFactory {

    /** Instantiates a new SnappyPreparedGossipMessageFactory. */
    public SnappyPreparedGossipMessageFactory() {}

    @Override
    public PreparedGossipMessage create(
            final String topic, final Bytes data, final NetworkingSpecConfig networkingConfig) {
        return new SnappyPreparedGossipMessage(topic, data);
    }
}
