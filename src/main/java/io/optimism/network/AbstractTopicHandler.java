package io.optimism.network;

import static org.web3j.crypto.Sign.recoverFromSignature;
import static org.web3j.utils.Assertions.verifyPrecondition;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import io.libp2p.core.pubsub.ValidationResult;
import io.optimism.exceptions.DecodingException;
import io.optimism.types.ExecutionPayload;
import io.optimism.types.ExecutionPayloadEnvelop;
import io.optimism.types.ExecutionPayloadSSZ;
import io.optimism.types.enums.BlockVersion;
import java.math.BigInteger;
import java.security.SignatureException;
import java.time.Instant;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CopyOnWriteArrayList;
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
import tech.pegasys.teku.networking.p2p.gossip.PreparedGossipMessageFactory;
import tech.pegasys.teku.networking.p2p.libp2p.config.LibP2PParamsFactory;
import tech.pegasys.teku.service.serviceutils.ServiceCapacityExceededException;
import tech.pegasys.teku.statetransition.validation.InternalValidationResult;

/**
 * The type Abstract topic handler.
 *
 * @author grapebaba
 * @since 0.2.6
 */
public abstract class AbstractTopicHandler implements NamedTopicHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTopicHandler.class);

    private final PreparedGossipMessageFactory preparedGossipMessageFactory;

    private final String topic;

    private final AsyncRunner asyncRunner;

    private final UInt64 chainId;

    private final String unsafeBlockSigner;

    private final MessagePassingQueue<ExecutionPayload> unsafeBlockQueue;

    private final BlockVersion version;

    private final Cache<BigInteger, CopyOnWriteArrayList<String>> cache;

    /**
     * Instantiates a new Abstract topic handler.
     *
     * @param preparedGossipMessageFactory the prepared gossip message factory
     * @param topic                        the topic
     * @param asyncRunner                  the async runner
     * @param chainId                      the chain id
     * @param unsafeBlockSigner            the unsafe block signer
     * @param unsafeBlockQueue             the unsafe block queue
     * @param version                      the version
     */
    protected AbstractTopicHandler(
            PreparedGossipMessageFactory preparedGossipMessageFactory,
            String topic,
            AsyncRunner asyncRunner,
            UInt64 chainId,
            String unsafeBlockSigner,
            MessagePassingQueue<ExecutionPayload> unsafeBlockQueue,
            BlockVersion version) {
        this.preparedGossipMessageFactory = preparedGossipMessageFactory;
        this.topic = topic;
        this.asyncRunner = asyncRunner;
        this.chainId = chainId;
        this.unsafeBlockSigner = unsafeBlockSigner;
        this.unsafeBlockQueue = unsafeBlockQueue;
        this.version = version;
        this.cache = CacheBuilder.from("maximumSize=1000").build();
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
                            processMessage(internalValidation, message, deserialized);
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
     * @param payloadEnvelop the payloadEnvelop
     * @param signature      the signature
     * @param payloadHash    the payload hash
     */
    public record BlockMessage(
            ExecutionPayloadEnvelop payloadEnvelop, Sign.SignatureData signature, byte[] payloadHash) {

        /**
         * From block message.
         *
         * @param data    the data
         * @param version the version
         * @return the block message
         */
        public static BlockMessage from(byte[] data, BlockVersion version) {
            Sign.SignatureData signature = new Sign.SignatureData(
                    data[64],
                    Bytes.wrap(data, 0, 32).toArray(),
                    Bytes.wrap(data, 32, 32).toArray());
            Bytes payload = Bytes.wrap(ArrayUtils.subarray(data, 65, data.length));
            ExecutionPayloadEnvelop executionPayloadEnvelop;
            if (version == BlockVersion.V3) {
                executionPayloadEnvelop = ExecutionPayloadEnvelop.from(payload);
            } else {
                ExecutionPayloadSSZ executionPayloadSSZ = ExecutionPayloadSSZ.from(payload, version);
                ExecutionPayload executionPayload = ExecutionPayload.from(executionPayloadSSZ);
                executionPayloadEnvelop = new ExecutionPayloadEnvelop(null, executionPayload);
            }
            byte[] payloadHash = Hash.sha3(payload.toArray());

            return new BlockMessage(executionPayloadEnvelop, signature, payloadHash);
        }
    }

    private CompletionStage<BlockMessage> deserialize(PreparedGossipMessage message) throws DecodingException {
        return SafeFuture.completedFuture(decode(message));
    }

    private BlockMessage decode(PreparedGossipMessage message) throws DecodingException {
        try {
            var decodesMsg =
                    message.getDecodedMessage().getDecodedMessageOrElseThrow().toArray();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Received gossip message {} on topic: {}", Numeric.toHexString(decodesMsg), topic);
            }
            return BlockMessage.from(decodesMsg, version);
        } catch (PreparedGossipMessage.GossipDecodingException e) {
            LOGGER.error("Failed to decode gossip message", e);
            throw new DecodingException("Failed to decode gossip message", e);
        }
    }

    SafeFuture<InternalValidationResult> checkBlock(BlockMessage blockMessage) {
        LOGGER.debug("Checking block {}", blockMessage);

        // [REJECT] if the signature by the sequencer is not valid
        byte[] msg = signatureMessage(chainId, blockMessage.payloadHash);
        try {
            BigInteger fromPub = signedMessageHashToKey(msg, blockMessage.signature());
            String from = Numeric.prependHexPrefix(Keys.getAddress(fromPub));

            if (!from.equalsIgnoreCase(this.unsafeBlockSigner)) {
                LOGGER.warn("Block signature is invalid, from: {}, expected: {}", from, this.unsafeBlockSigner);
                return SafeFuture.completedFuture(
                        InternalValidationResult.reject("Block signature is invalid: %s", blockMessage.signature()));
            }

        } catch (SignatureException e) {
            return SafeFuture.completedFuture(
                    InternalValidationResult.reject("Block signature is invalid: %s", blockMessage.signature()));
        }

        ExecutionPayload executionPayload = blockMessage.payloadEnvelop.executionPayload();
        long now = Instant.now().getEpochSecond();

        // [REJECT] if the `payload.timestamp` is more than 5 seconds into the future
        if (executionPayload.timestamp().longValue() > now + 5) {
            LOGGER.warn(
                    "Block timestamp is too far in the future: {}, now: {}",
                    executionPayload.timestamp().longValue(),
                    now);
            return SafeFuture.completedFuture(InternalValidationResult.reject(
                    "Block timestamp is too far in the future: %d",
                    executionPayload.timestamp().longValue()));
        }

        // [REJECT] if the `payload.timestamp` is older than 60 seconds in the past
        if (executionPayload.timestamp().longValue() < now - 60) {
            LOGGER.warn(
                    "Block timestamp is too far in the past: {}, now: {}",
                    executionPayload.timestamp().longValue(),
                    now);
            return SafeFuture.completedFuture(InternalValidationResult.reject(
                    "Block timestamp is too far in the past: %d",
                    executionPayload.timestamp().longValue()));
        }

        // TODO: block_hash check

        // [REJECT] if a V1 Block has withdrawals
        if (!version.hasWithdrawals() && executionPayload.withdrawals() != null) {
            LOGGER.warn("Block withdrawals is not empty");
            return SafeFuture.completedFuture(InternalValidationResult.reject(
                    "Payload is on v1 topic, but has withdrawals. Bad hash: %s, Withdrawals count: %d",
                    executionPayload.blockHash(), executionPayload.withdrawals().size()));
        }

        // [REJECT] if a V2/V3 Block does not have withdrawals
        if (version.hasWithdrawals() && executionPayload.withdrawals() == null) {
            LOGGER.warn("Block withdrawals is null");
            return SafeFuture.completedFuture(InternalValidationResult.reject(
                    "Payload is on v2/v3 topic, but does not have withdrawals. Bad Hash: %s",
                    executionPayload.blockHash()));
        }

        // [REJECT] if a V2/V3 Block has non-empty withdrawals
        if (version.hasWithdrawals() && !executionPayload.withdrawals().isEmpty()) {
            LOGGER.warn("Block withdrawals is not empty");
            return SafeFuture.completedFuture(InternalValidationResult.reject(
                    "Payload is on v2/v3 topic, but has withdrawals. Bad hash: %s, Withdrawals count: %d",
                    executionPayload.blockHash(), executionPayload.withdrawals().size()));
        }

        // [REJECT] if the block is on a topic <= V2 and has a blob gas value set
        if (!version.hasBlobProperties() && executionPayload.blobGasUsed() != null) {
            LOGGER.warn("Block has blob gas value set");
            return SafeFuture.completedFuture(InternalValidationResult.reject(
                    "Payload is on v1/v2 topic, but has blob gas value set. Bad hash: %s, Blob gas: %d",
                    executionPayload.blockHash(), executionPayload.blobGasUsed().longValue()));
        }

        // [REJECT] if the block is on a topic <= V2 and has an excess blob gas value set
        if (!version.hasBlobProperties() && executionPayload.excessBlobGas() != null) {
            LOGGER.warn("Block has excess blob gas value set");
            return SafeFuture.completedFuture(InternalValidationResult.reject(
                    "Payload is on v1/v2 topic, but has excess blob gas value set. Bad hash: %s, Excess blob gas: %d",
                    executionPayload.blockHash(),
                    executionPayload.excessBlobGas().longValue()));
        }

        // [REJECT] if the block is on a topic >= V3 and does not have a blob gas value
        if (version.hasBlobProperties() && executionPayload.blobGasUsed() == null) {
            LOGGER.warn("Block has no blob gas value set");
            return SafeFuture.completedFuture(InternalValidationResult.reject(
                    "Payload is on v3 topic, but has no blob gas value set. Bad hash: %s",
                    executionPayload.blockHash()));
        }

        // [REJECT] if the block is on a topic >= V3 and does not have an excess blob gas value
        if (version.hasBlobProperties() && executionPayload.excessBlobGas() == null) {
            LOGGER.warn("Block has no excess blob gas value set");
            return SafeFuture.completedFuture(InternalValidationResult.reject(
                    "Payload is on v3 topic, but has no excess blob gas value set. Bad hash: %s",
                    executionPayload.blockHash()));
        }

        // [REJECT] if the block is on a topic >= V3 and has a blob gas used value that is not zero
        if (version.hasBlobProperties() && executionPayload.blobGasUsed().longValue() != 0) {
            LOGGER.warn("Block has non-zero blob gas value set");
            return SafeFuture.completedFuture(InternalValidationResult.reject(
                    "Payload is on v3 topic, but has non-zero blob gas value set. Bad hash: %s, Blob gas: %d",
                    executionPayload.blockHash(), executionPayload.blobGasUsed().longValue()));
        }

        // [REJECT] if the block is on a topic >= V3 and has an excess blob gas value that is not zero
        if (version.hasBlobProperties() && executionPayload.excessBlobGas().longValue() != 0) {
            LOGGER.warn("Block has non-zero excess blob gas value set");
            return SafeFuture.completedFuture(InternalValidationResult.reject(
                    "Payload is on v3 topic, but has non-zero excess blob gas value set. Bad hash: %s, Excess blob gas: %d",
                    executionPayload.blockHash(),
                    executionPayload.excessBlobGas().longValue()));
        }

        // [REJECT] if the block is on a topic >= V3 and the parent beacon block root is nil
        if (version.hasParentBeaconBlockRoot() && blockMessage.payloadEnvelop.parentBeaconBlockRoot() == null) {
            LOGGER.warn("Block has nil parent beacon block root");
            return SafeFuture.completedFuture(InternalValidationResult.reject(
                    "Payload is on v3 topic, but has nil parent beacon block root. Bad hash: %s",
                    executionPayload.blockHash()));
        }

        var exist = this.cache.getIfPresent(executionPayload.blockNumber());
        if (exist == null) {
            exist = new CopyOnWriteArrayList<>();
            this.cache.put(executionPayload.blockNumber(), exist);
        }

        // [REJECT] if more than 5 blocks have been seen with the same block height
        if (exist.size() > 5) {
            LOGGER.warn("seen too many different blocks at same height: {}", executionPayload.blockNumber());
            return SafeFuture.completedFuture(InternalValidationResult.reject(
                    "seen too many different blocks at same height: %d", executionPayload.blockNumber()));
        }

        // [IGNORE] if the block has already been seen
        if (exist.contains(executionPayload.blockHash())) {
            LOGGER.warn("Block has been processed before");
            return SafeFuture.completedFuture(InternalValidationResult.ignore(
                    "Payload has been processed before. Bad hash: %s", executionPayload.blockHash()));
        }

        exist.add(executionPayload.blockHash());
        return SafeFuture.completedFuture(InternalValidationResult.ACCEPT);
    }

    /**
     * Signature message byte [ ].
     *
     * @param chainId     the chain id
     * @param payloadHash the payload hash
     * @return the byte [ ]
     */
    protected byte[] signatureMessage(UInt64 chainId, byte[] payloadHash) {
        Bytes32 domain = Bytes32.ZERO;
        byte[] chainIdBytes = Numeric.toBytesPadded(chainId.toBigInteger(), 32);
        return Hash.sha3(Arrays.concatenate(domain.toArray(), chainIdBytes, payloadHash));
    }

    /**
     * Process message.
     *
     * @param internalValidationResult the internal validation result
     * @param message                  the message
     * @param blockMessage             the block message
     */
    protected void processMessage(
            final InternalValidationResult internalValidationResult,
            final PreparedGossipMessage message,
            final BlockMessage blockMessage) {
        switch (internalValidationResult.code()) {
            case REJECT -> LOGGER.warn(
                    "Rejecting gossip message on topic {}, reason: {}, decoded message: {}",
                    topic,
                    internalValidationResult.getDescription(),
                    message.getDecodedMessage().getDecodedMessage().orElseThrow());
            case IGNORE -> LOGGER.debug("Ignoring message for topic: {}", topic);
            case SAVE_FOR_FUTURE -> LOGGER.debug("Deferring message for topic: {}", topic);
            case ACCEPT -> {
                LOGGER.debug(
                        "Accepting message for topic: {}, number: {}",
                        topic,
                        blockMessage.payloadEnvelop.executionPayload().blockNumber());
                this.unsafeBlockQueue.offer(blockMessage.payloadEnvelop.executionPayload());
            }
            default -> throw new UnsupportedOperationException(
                    String.format("Unexpected validation result: %s", internalValidationResult));
        }
    }

    /**
     * Handle message processing error validation result.
     *
     * @param message the message
     * @param err     the err
     * @return the validation result
     */
    protected ValidationResult handleMessageProcessingError(final PreparedGossipMessage message, final Throwable err) {
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

    /**
     * From internal validation result validation result.
     *
     * @param result the result
     * @return the validation result
     */
    protected static ValidationResult fromInternalValidationResult(InternalValidationResult result) {
        return switch (result.code()) {
            case ACCEPT -> ValidationResult.Valid;
            case SAVE_FOR_FUTURE, IGNORE -> ValidationResult.Ignore;
            case REJECT -> ValidationResult.Invalid;
        };
    }

    /**
     * Signed message hash to key big integer.
     *
     * @param messageHash   the message hash
     * @param signatureData the signature data
     * @return the big integer
     * @throws SignatureException the signature exception
     */
    protected static BigInteger signedMessageHashToKey(byte[] messageHash, Sign.SignatureData signatureData)
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
