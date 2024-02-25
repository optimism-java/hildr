package io.optimism.driver;

import io.optimism.common.BlockInfo;
import io.optimism.common.Epoch;
import io.optimism.config.Config;
import io.optimism.engine.Engine;
import io.optimism.engine.EngineApi;
import io.optimism.engine.ExecutionPayload;
import io.optimism.engine.ExecutionPayload.PayloadAttributes;
import io.optimism.engine.ExecutionPayload.PayloadStatus;
import io.optimism.engine.ExecutionPayload.Status;
import io.optimism.engine.ForkChoiceUpdate;
import io.optimism.engine.ForkChoiceUpdate.ForkchoiceState;
import io.optimism.engine.OpEthExecutionPayload;
import io.optimism.engine.OpEthForkChoiceUpdate;
import io.optimism.engine.OpEthPayloadStatus;
import io.optimism.utilities.telemetry.TracerTaskWrapper;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Hash;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlock.TransactionObject;

/**
 * The type EngineDriver.
 *
 * @param <E> the type parameter
 * @author grapebaba
 * @since 0.1.0
 */
public class EngineDriver<E extends Engine> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EngineDriver.class);
    private E engine;

    private Web3j web3j;

    private BigInteger blockTime;

    private BlockInfo unsafeHead;

    private BlockInfo safeHead;

    private Epoch safeEpoch;

    private BlockInfo finalizedHead;

    private Epoch finalizedEpoch;

    /**
     * Instantiates a new Engine driver.
     *
     * @param finalizedHead the finalized head
     * @param finalizedEpoch the finalized epoch
     * @param web3j the web 3 j
     * @param config the config
     */
    @SuppressWarnings("unchecked")
    public EngineDriver(BlockInfo finalizedHead, Epoch finalizedEpoch, Web3j web3j, Config config) {
        this.engine = (E) new EngineApi(config, config.l2EngineUrl(), config.jwtSecret());
        this.unsafeHead = finalizedHead;
        this.finalizedHead = finalizedHead;
        this.finalizedEpoch = finalizedEpoch;
        this.safeHead = finalizedHead;
        this.safeEpoch = finalizedEpoch;
        this.web3j = web3j;
        this.blockTime = config.chainConfig().blockTime();
    }

    /** Stop. */
    public void stop() {
        this.web3j.shutdown();
    }

    /**
     * Gets block time.
     *
     * @return the block time
     */
    public BigInteger getBlockTime() {
        return blockTime;
    }

    /**
     * Gets unsafe head.
     *
     * @return the unsafe head
     */
    public BlockInfo getUnsafeHead() {
        return unsafeHead;
    }

    /**
     * Gets safe head.
     *
     * @return the safe head
     */
    public BlockInfo getSafeHead() {
        return safeHead;
    }

    /**
     * Gets safe epoch.
     *
     * @return the safe epoch
     */
    public Epoch getSafeEpoch() {
        return safeEpoch;
    }

    /**
     * Gets finalized head.
     *
     * @return the finalized head
     */
    public BlockInfo getFinalizedHead() {
        return finalizedHead;
    }

    /**
     * Gets finalized epoch.
     *
     * @return the finalized epoch
     */
    public Epoch getFinalizedEpoch() {
        return finalizedEpoch;
    }

    /**
     * Handle attributes completable future.
     *
     * @param attributes the attributes
     * @throws ExecutionException the execution exception
     * @throws InterruptedException the interrupted exception
     */
    public void handleAttributes(PayloadAttributes attributes) throws ExecutionException, InterruptedException {
        EthBlock block = this.blockAt(attributes.timestamp());
        if (block == null || block.getBlock() == null) {
            processAttributes(attributes);
        } else {
            if (this.shouldSkip(block, attributes)) {
                skipAttributes(attributes, block);
            } else {
                this.unsafeHead = this.safeHead;
                processAttributes(attributes);
            }
        }
    }

    /**
     * Handle unsafe payload completable future.
     *
     * @param payload the payload
     * @throws ExecutionException the execution exception
     * @throws InterruptedException the interrupted exception
     */
    public void handleUnsafePayload(ExecutionPayload payload) throws ExecutionException, InterruptedException {
        this.pushPayload(payload);
        this.unsafeHead = BlockInfo.from(payload);
        this.updateForkchoice();
        LOGGER.info("head updated: {} {}", this.unsafeHead.number(), this.unsafeHead.hash());
    }

    /**
     * Update finalized.
     *
     * @param head the head
     * @param epoch the epoch
     */
    public void updateFinalized(BlockInfo head, Epoch epoch) {
        this.finalizedHead = head;
        this.finalizedEpoch = epoch;
    }

    /** Reorg. */
    public void reorg() {
        this.unsafeHead = this.finalizedHead;
        this.safeHead = this.finalizedHead;
        this.safeEpoch = this.finalizedEpoch;
    }

    /**
     * Engine ready completable future.
     *
     * @return the completable future
     * @throws InterruptedException the interrupted exception
     */
    public boolean engineReady() throws InterruptedException {
        ForkchoiceState forkchoiceState = createForkchoiceState();

        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            var res = scope.fork(
                    TracerTaskWrapper.wrap(() -> EngineDriver.this.engine.forkchoiceUpdated(forkchoiceState, null)));
            scope.join();
            return scope.exception().isEmpty() && !res.get().hasError();
        }
    }

    @SuppressWarnings("preview")
    private boolean shouldSkip(EthBlock block, PayloadAttributes attributes) {
        LOGGER.debug(
                "comparing block at {} with attributes at {}", block.getBlock().getTimestamp(), attributes.timestamp());
        LOGGER.debug("block: {}", block.getBlock());
        LOGGER.debug("attributes: {}", attributes);
        List<String> attributesHashes =
                attributes.transactions().stream().map(Hash::sha3).collect(Collectors.toList());
        LOGGER.debug("attribute hashes: {}", attributesHashes);

        return block.getBlock().getTransactions().stream()
                        .map(transactionResult -> ((TransactionObject) transactionResult).getHash())
                        .toList()
                        .equals(attributesHashes)
                && attributes.timestamp().equals(block.getBlock().getTimestamp())
                && attributes.prevRandao().equalsIgnoreCase(block.getBlock().getMixHash())
                && attributes
                        .suggestedFeeRecipient()
                        .equalsIgnoreCase(
                                StringUtils.isNotEmpty(block.getBlock().getAuthor())
                                        ? block.getBlock().getAuthor()
                                        : block.getBlock().getMiner())
                && attributes.gasLimit().equals(block.getBlock().getGasLimit());
    }

    @SuppressWarnings("preview")
    private EthBlock blockAt(BigInteger timestamp) throws InterruptedException, ExecutionException {
        BigInteger timeDiff = timestamp.subtract(this.finalizedHead.timestamp());
        BigInteger blocks = timeDiff.divide(this.blockTime);
        BigInteger blockNumber = this.finalizedHead.number().add(blocks);

        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            StructuredTaskScope.Subtask<EthBlock> ethBlockFuture = scope.fork(TracerTaskWrapper.wrap(
                    () -> web3j.ethGetBlockByNumber(DefaultBlockParameter.valueOf(blockNumber), true)
                            .send()));

            scope.join();
            scope.throwIfFailed();

            return ethBlockFuture.get();
        }
    }

    private ForkchoiceState createForkchoiceState() {
        return new ForkchoiceState(this.unsafeHead.hash(), this.safeHead.hash(), this.finalizedHead.hash());
    }

    private void updateSafeHead(BlockInfo newHead, Epoch newEpoch, boolean reorgUnsafe) {
        if (!this.safeHead.equals(newHead)) {
            this.safeHead = newHead;
            this.safeEpoch = newEpoch;
        }
        if (reorgUnsafe || this.safeHead.number().compareTo(this.unsafeHead.number()) > 0) {
            this.unsafeHead = newHead;
        }
    }

    private void updateForkchoice() throws InterruptedException, ExecutionException {
        ForkchoiceState forkchoiceState = createForkchoiceState();

        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            StructuredTaskScope.Subtask<OpEthForkChoiceUpdate> forkChoiceUpdateFuture = scope.fork(
                    TracerTaskWrapper.wrap(() -> EngineDriver.this.engine.forkchoiceUpdated(forkchoiceState, null)));

            scope.join();
            scope.throwIfFailed();
            ForkChoiceUpdate forkChoiceUpdate = forkChoiceUpdateFuture.get().getForkChoiceUpdate();

            if (forkChoiceUpdate.payloadStatus().getStatus() != Status.VALID) {
                throw new ForkchoiceUpdateException(String.format(
                        "could not accept new forkchoice: %s",
                        forkChoiceUpdate.payloadStatus().getValidationError()));
            }
        }
    }

    private void pushPayload(ExecutionPayload payload) throws InterruptedException, ExecutionException {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            StructuredTaskScope.Subtask<OpEthPayloadStatus> payloadStatusFuture =
                    scope.fork(TracerTaskWrapper.wrap(() -> EngineDriver.this.engine.newPayload(payload)));

            scope.join();
            scope.throwIfFailed();
            PayloadStatus payloadStatus = payloadStatusFuture.get().getPayloadStatus();

            if (payloadStatus.getStatus() != Status.VALID && payloadStatus.getStatus() != Status.ACCEPTED) {
                throw new InvalidExecutionPayloadException("the provided checkpoint payload is invalid");
            }
        }
    }

    private OpEthExecutionPayload buildPayload(PayloadAttributes attributes)
            throws InterruptedException, ExecutionException {
        ForkchoiceState forkchoiceState = createForkchoiceState();

        ForkChoiceUpdate forkChoiceUpdate;
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            StructuredTaskScope.Subtask<OpEthForkChoiceUpdate> forkChoiceUpdateFuture =
                    scope.fork(TracerTaskWrapper.wrap(
                            () -> EngineDriver.this.engine.forkchoiceUpdated(forkchoiceState, attributes)));

            scope.join();
            scope.throwIfFailed();
            forkChoiceUpdate = forkChoiceUpdateFuture.get().getForkChoiceUpdate();
        }

        if (forkChoiceUpdate.payloadStatus().getStatus() != Status.VALID) {
            throw new InvalidPayloadAttributesException();
        }

        BigInteger payloadId = forkChoiceUpdate.payloadId();
        if (payloadId == null) {
            throw new PayloadIdNotReturnedException();
        }

        OpEthExecutionPayload res;
        try (var scope1 = new StructuredTaskScope.ShutdownOnFailure()) {
            StructuredTaskScope.Subtask<OpEthExecutionPayload> payloadFuture =
                    scope1.fork(TracerTaskWrapper.wrap(() -> EngineDriver.this.engine.getPayloadV2(payloadId)));

            scope1.join();
            scope1.throwIfFailed();
            res = payloadFuture.get();
        }
        return res;
    }

    private void skipAttributes(PayloadAttributes attributes, EthBlock block)
            throws ExecutionException, InterruptedException {
        Epoch newEpoch = attributes.epoch();
        BlockInfo newHead = BlockInfo.from(block.getBlock());
        this.updateSafeHead(newHead, newEpoch, false);
        this.updateForkchoice();
    }

    private void processAttributes(PayloadAttributes attributes) throws ExecutionException, InterruptedException {
        Epoch newEpoch = attributes.epoch();
        OpEthExecutionPayload opEthExecutionPayload = this.buildPayload(attributes);
        ExecutionPayload executionPayload = opEthExecutionPayload.getExecutionPayload();
        BlockInfo newHead = new BlockInfo(
                executionPayload.blockHash(),
                executionPayload.blockNumber(),
                executionPayload.parentHash(),
                executionPayload.timestamp());

        this.pushPayload(executionPayload);
        updateSafeHead(newHead, newEpoch, true);
        this.updateForkchoice();
    }
}
