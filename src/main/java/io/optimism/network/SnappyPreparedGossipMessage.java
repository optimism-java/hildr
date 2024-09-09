package io.optimism.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.nio.charset.StandardCharsets;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.tuweni.bytes.Bytes;
import org.web3j.crypto.Hash;
import tech.pegasys.teku.networking.p2p.gossip.PreparedGossipMessage;

/**
 * The type SnappyPreparedGossipMessage.
 *
 * @author grapebaba
 * @since 0.1.1
 */
public class SnappyPreparedGossipMessage implements PreparedGossipMessage {

    private static final Bytes VALID_DOMAIN = Bytes.wrap(new byte[] {1, 0, 0, 0});
    private static final Bytes INVALID_DOMAIN = Bytes.wrap(new byte[] {0, 0, 0, 0});
    private final String topic;
    private final Bytes originalMessage;

    /**
     * Instantiates a new Snappy prepared gossip message.
     *
     * @param topic the topic
     * @param originalMessage the original message
     */
    public SnappyPreparedGossipMessage(String topic, Bytes originalMessage) {
        this.topic = topic;
        this.originalMessage = originalMessage;
    }

    @Override
    public Bytes getMessageId() {
        ByteBuf topicLen = Unpooled.buffer(8);
        topicLen.writeLongLE(topic.length());
        Bytes topicLenBytes = Bytes.wrapByteBuf(topicLen);
        Bytes topicBytes = Bytes.wrap(topic.getBytes(StandardCharsets.UTF_8));
        return this.getDecodedMessage()
                .getDecodedMessage()
                .map(bytes -> Bytes.wrap(ArrayUtils.subarray(
                        Hash.sha256(Bytes.concatenate(VALID_DOMAIN, topicLenBytes, topicBytes, bytes)
                                .toArray()),
                        0,
                        20)))
                .orElseGet(() -> Bytes.wrap(ArrayUtils.subarray(
                        Hash.sha256(
                                Bytes.concatenate(INVALID_DOMAIN, topicLenBytes, topicBytes, this.getOriginalMessage())
                                        .toArray()),
                        0,
                        20)));
    }

    @Override
    public DecodedMessageResult getDecodedMessage() {
        byte[] out;
        try {
            out = org.xerial.snappy.Snappy.uncompress(originalMessage.toArray());
        } catch (Exception e) {
            return DecodedMessageResult.failed(e);
        }

        return DecodedMessageResult.successful(Bytes.wrap(out));
    }

    @Override
    public Bytes getOriginalMessage() {
        return originalMessage;
    }
}
