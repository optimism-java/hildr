package io.optimism.network;

import static org.web3j.crypto.Sign.recoverFromSignature;
import static org.web3j.utils.Assertions.verifyPrecondition;

import io.libp2p.core.pubsub.ValidationResult;
import io.optimism.engine.ExecutionPayload;
import java.math.BigInteger;
import java.security.SignatureException;
import java.time.Instant;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.RejectedExecutionException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.units.bigints.UInt64;
import org.bouncycastle.util.Arrays;
import org.jctools.queues.MessagePassingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.ECDSASignature;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;
import tech.pegasys.teku.infrastructure.async.AsyncRunner;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.networking.p2p.gossip.PreparedGossipMessage;
import tech.pegasys.teku.networking.p2p.gossip.PreparedGossipMessage.GossipDecodingException;
import tech.pegasys.teku.networking.p2p.gossip.PreparedGossipMessageFactory;
import tech.pegasys.teku.networking.p2p.libp2p.config.LibP2PParamsFactory;
import tech.pegasys.teku.service.serviceutils.ServiceCapacityExceededException;
import tech.pegasys.teku.statetransition.validation.InternalValidationResult;

/**
 * The type BlockV2TopicHandler.
 *
 * @author grapebaba
 * @since 0.2.0
 */
@SuppressWarnings({"checkstyle:VariableDeclarationUsageDistance", "checkstyle:AbbreviationAsWordInName"})
public class BlockV2TopicHandler implements NamedTopicHandler {
    // All fields (4s are offsets to dynamic data)

    // MAX_TRANSACTIONS_PER_PAYLOAD in consensus spec

    private static final Logger LOGGER = LoggerFactory.getLogger(BlockV1TopicHandler.class);
    private final PreparedGossipMessageFactory preparedGossipMessageFactory;

    private final String topic;

    private final AsyncRunner asyncRunner;

    private final UInt64 chainId;

    private final String unsafeBlockSigner;

    private final MessagePassingQueue<ExecutionPayload> unsafeBlockQueue;

    /**
     * Instantiates a new Block topic handler.
     *
     * @param preparedGossipMessageFactory the prepared gossip message factory
     * @param asyncRunner                  the async runner
     * @param chainId                      the chain id
     * @param unsafeBlockSigner            the unsafe block signer
     * @param unsafeBlockQueue             the unsafe block queue
     * @author grapebaba
     * @since 0.2.0
     */
    public BlockV2TopicHandler(
            PreparedGossipMessageFactory preparedGossipMessageFactory,
            AsyncRunner asyncRunner,
            UInt64 chainId,
            String unsafeBlockSigner,
            MessagePassingQueue<ExecutionPayload> unsafeBlockQueue) {
        this.preparedGossipMessageFactory = preparedGossipMessageFactory;
        this.topic = String.format("/optimism/%s/1/blocks", chainId.toString());
        this.asyncRunner = asyncRunner;
        this.chainId = chainId;
        this.unsafeBlockSigner = unsafeBlockSigner;
        this.unsafeBlockQueue = unsafeBlockQueue;
    }

    @Override
    public PreparedGossipMessage prepareMessage(Bytes payload) {
        return preparedGossipMessageFactory.create(topic, payload, null);
    }

    @Override
    public SafeFuture<ValidationResult> handleMessage(PreparedGossipMessage message) {
        return SafeFuture.of(() -> deserialize(message))
                .thenCompose(deserialized ->
                        asyncRunner.runAsync(() -> checkBlock(deserialized).thenApply(internalValidation -> {
                            processMessage(internalValidation, message);
                            return fromInternalValidationResult(internalValidation);
                        })))
                .exceptionally(error -> handleMessageProcessingError(message, error));
    }

    /**
     * Gets topic.
     *
     * @return the topic
     */
    @Override
    public String getTopic() {
        return topic;
    }

    @Override
    public int getMaxMessageSize() {
        return LibP2PParamsFactory.MAX_COMPRESSED_GOSSIP_SIZE;
    }

    /**
     * The type BlockMessage.
     *
     * @param payload     the payload
     * @param signature   the signature
     * @param payloadHash the payload hash
     */
    public record BlockMessage(ExecutionPayload payload, Sign.SignatureData signature, byte[] payloadHash) {

        /**
         * From block message.
         *
         * @param data the data
         * @return the block message
         */
        public static BlockMessage from(byte[] data) {
            Sign.SignatureData signature = new Sign.SignatureData(
                    data[64],
                    Bytes.wrap(data, 0, 32).toArray(),
                    Bytes.wrap(data, 32, 32).toArray());
            Bytes payload = Bytes.wrap(ArrayUtils.subarray(data, 65, data.length));
            ExecutionPayloadSSZ executionPayloadSSZ = ExecutionPayloadSSZ.from(payload, 1);
            ExecutionPayload executionPayload = ExecutionPayload.from(executionPayloadSSZ);
            byte[] payloadHash = Hash.sha3(payload.toArray());

            return new BlockMessage(executionPayload, signature, payloadHash);
        }
    }

    private CompletionStage<BlockMessage> deserialize(PreparedGossipMessage message) throws DecodingException {
        return SafeFuture.completedFuture(decode(message));
    }

    private BlockMessage decode(PreparedGossipMessage message) throws DecodingException {
        LOGGER.debug("Received gossip message {} on topic: {}", message, topic);
        try {
            return BlockMessage.from(
                    message.getDecodedMessage().getDecodedMessageOrElseThrow().toArray());
        } catch (GossipDecodingException e) {
            LOGGER.error("Failed to decode gossip message", e);
            throw new DecodingException("Failed to decode gossip message", e);
        }
    }

    private SafeFuture<InternalValidationResult> checkBlock(BlockMessage blockMessage) {
        LOGGER.debug("Checking block {}", blockMessage);
        long now = Instant.now().getEpochSecond();
        if (blockMessage.payload.timestamp().longValue() > now + 5) {
            LOGGER.debug(
                    "Block timestamp is too far in the future: {}, now: {}",
                    blockMessage.payload.timestamp().longValue(),
                    now);
            return SafeFuture.completedFuture(InternalValidationResult.reject(
                    "Block timestamp is too far in the future: %d",
                    blockMessage.payload.timestamp().longValue()));
        }

        if (blockMessage.payload.timestamp().longValue() < now - 60) {
            LOGGER.debug(
                    "Block timestamp is too far in the past: {}, now: {}",
                    blockMessage.payload.timestamp().longValue(),
                    now);
            return SafeFuture.completedFuture(InternalValidationResult.reject(
                    "Block timestamp is too far in the past: %d",
                    blockMessage.payload.timestamp().longValue()));
        }

        if (blockMessage.payload.withdrawals() == null) {
            LOGGER.debug("Block withdrawals is null");
            return SafeFuture.completedFuture(InternalValidationResult.reject(
                    "Payload is on v2 topic, but does not have withdrawals. Bad Hash: %s",
                    blockMessage.payload.blockHash()));
        }

        if (!blockMessage.payload.withdrawals().isEmpty()) {
            LOGGER.debug("Block withdrawals is not empty");
            return SafeFuture.completedFuture(InternalValidationResult.reject(
                    "Payload is on v2 topic, but does not have withdrawals. Bad hash: %s, Withdrawl count: %d",
                    blockMessage.payload.blockHash(),
                    blockMessage.payload.withdrawals().size()));
        }

        byte[] msg = signatureMessage(chainId, blockMessage.payloadHash);

        try {
            BigInteger fromPub = signedMessageHashToKey(msg, blockMessage.signature());
            String from = Numeric.prependHexPrefix(Keys.getAddress(fromPub));

            if (!from.equalsIgnoreCase(this.unsafeBlockSigner)) {
                LOGGER.debug("Block signature is invalid, from: {}, expected: {}", from, this.unsafeBlockSigner);
                return SafeFuture.completedFuture(
                        InternalValidationResult.reject("Block signature is invalid: %s", blockMessage.signature()));
            }

        } catch (SignatureException e) {
            return SafeFuture.completedFuture(
                    InternalValidationResult.reject("Block signature is invalid: %s", blockMessage.signature()));
        }

        this.unsafeBlockQueue.offer(blockMessage.payload);
        return SafeFuture.completedFuture(InternalValidationResult.ACCEPT);
    }

    private byte[] signatureMessage(UInt64 chainId, byte[] payloadHash) {
        Bytes32 domain = Bytes32.ZERO;
        byte[] chainIdBytes = Numeric.toBytesPadded(chainId.toBigInteger(), 32);
        return Hash.sha3(Arrays.concatenate(domain.toArray(), chainIdBytes, payloadHash));
    }

    private void processMessage(
            final InternalValidationResult internalValidationResult, final PreparedGossipMessage message) {
        switch (internalValidationResult.code()) {
            case REJECT -> LOGGER.warn(
                    "Rejecting gossip message on topic {}, reason: {}, decoded message: {}",
                    topic,
                    internalValidationResult.getDescription(),
                    message.getDecodedMessage().getDecodedMessage().orElseThrow());
            case IGNORE -> LOGGER.debug("Ignoring message for topic: {}", topic);
            case SAVE_FOR_FUTURE -> LOGGER.debug("Deferring message for topic: {}", topic);
            case ACCEPT -> LOGGER.debug("Accepting message for topic: {}", topic);
            default -> throw new UnsupportedOperationException(
                    String.format("Unexpected validation result: %s", internalValidationResult));
        }
    }

    private ValidationResult handleMessageProcessingError(final PreparedGossipMessage message, final Throwable err) {
        final ValidationResult response;
        if (ExceptionUtils.hasCause(err, DecodingException.class)) {
            LOGGER.warn(
                    "Failed to decode gossip message on topic {}, raw message: {}, error: {}",
                    topic,
                    message.getOriginalMessage(),
                    err);
            response = ValidationResult.Invalid;
        } else if (ExceptionUtils.hasCause(err, RejectedExecutionException.class)) {
            LOGGER.warn("Discarding gossip message for topic {} because the executor queue is full", topic);
            response = ValidationResult.Ignore;
        } else if (ExceptionUtils.hasCause(err, ServiceCapacityExceededException.class)) {
            LOGGER.warn(
                    "Discarding gossip message for topic {} because the signature verification queue is full", topic);
            response = ValidationResult.Ignore;
        } else {
            LOGGER.warn("Encountered exception while processing message for topic {}, error: {}", topic, err);
            response = ValidationResult.Invalid;
        }

        return response;
    }

    private static ValidationResult fromInternalValidationResult(InternalValidationResult result) {
        return switch (result.code()) {
            case ACCEPT -> ValidationResult.Valid;
            case SAVE_FOR_FUTURE, IGNORE -> ValidationResult.Ignore;
            case REJECT -> ValidationResult.Invalid;
        };
    }

    private static BigInteger signedMessageHashToKey(byte[] messageHash, Sign.SignatureData signatureData)
            throws SignatureException {

        byte[] r = signatureData.getR();
        byte[] s = signatureData.getS();
        verifyPrecondition(r != null && r.length == 32, "r must be 32 bytes");
        verifyPrecondition(s != null && s.length == 32, "s must be 32 bytes");

        ECDSASignature sig =
                new ECDSASignature(new BigInteger(1, signatureData.getR()), new BigInteger(1, signatureData.getS()));

        BigInteger key = recoverFromSignature(signatureData.getV()[0], sig, messageHash);
        if (key == null) {
            throw new SignatureException("Could not recover public key from signature");
        }
        return key;
    }
}
