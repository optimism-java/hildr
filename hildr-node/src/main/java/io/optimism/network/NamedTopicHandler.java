package io.optimism.network;

import tech.pegasys.teku.networking.p2p.gossip.TopicHandler;

/**
 * The interface NamedTopicHandler.
 *
 * @author grapebaba
 * @since 0.2.0
 */
public interface NamedTopicHandler extends TopicHandler {
    /**
     * Gets topic.
     *
     * @return the topic
     */
    String getTopic();
}
