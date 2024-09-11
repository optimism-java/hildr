package io.optimism.driver;

import io.optimism.config.Config;
import io.optimism.engine.Engine;
import io.optimism.engine.EngineApi;
import io.optimism.engine.OpEthExecutionPayload;
import io.optimism.engine.OpEthForkChoiceUpdate;
import io.optimism.engine.OpEthPayloadStatus;
import io.optimism.telemetry.TracerTaskWrapper;
import io.optimism.types.BlockInfo;
import io.optimism.types.Epoch;
import io.optimism.types.ExecutionPayload;
import io.optimism.types.ExecutionPayload.PayloadAttributes;
import io.optimism.types.ExecutionPayload.PayloadStatus;
import io.optimism.types.ExecutionPayload.Status;
import io.optimism.types.ExecutionPayloadEnvelop;
import io.optimism.types.ForkChoiceUpdate;
import io.optimism.types.ForkChoiceUpdate.ForkchoiceState;
import io.optimism.types.L2BlockRef;
import io.optimism.types.PayloadInfo;
import io.optimism.types.enums.BlockInsertion;
import io.optimism.types.enums.SyncStatus;
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
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlock.TransactionObject;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.tuples.generated.Tuple3;
import org.web3j.utils.Numeric;

/**
 * The type EngineDriver.
 *
 * @param <E> the type parameter
 * @author grapebaba
 * @since 0.1.0
 */
public class EngineDriver<E extends Engine> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EngineDriver.class);
    private final E engine;

    private final Web3j l2Client;

    private final BigInteger blockTime;

    private final boolean syncModeEl;

    private final Config.ChainConfig chainConfig;

    private BlockInfo unsafeHead;

    private BlockInfo safeHead;

    private Epoch safeEpoch;

    private BlockInfo finalizedHead;

    private Epoch finalizedEpoch;

    private SyncStatus syncStatus;

    // building state
    private L2BlockRef buildingOnto;
    private PayloadInfo buildingInfo;
    private boolean buildingSafe;

    /**
     * Instantiates a new Engine driver.
     *
     * @param finalizedHead the finalized head
     * @param finalizedEpoch the finalized epoch
     * @param l2Client the web 3 j
     * @param config the config
     */
    @SuppressWarnings("unchecked")
    public EngineDriver(BlockInfo finalizedHead, Epoch finalizedEpoch, Web3j l2Client, Config config) {
        this.engine = (E) new EngineApi(config, config.l2EngineUrl(), config.jwtSecret());
        this.unsafeHead = finalizedHead;
        this.finalizedHead = finalizedHead;
        this.finalizedEpoch = finalizedEpoch;
        this.safeHead = finalizedHead;
        this.safeEpoch = finalizedEpoch;
        this.l2Client = l2Client;
        this.chainConfig = config.chainConfig();
        this.blockTime = config.chainConfig().blockTime();
        this.syncModeEl = config.syncMode().isEl();
        this.syncStatus = syncModeEl ? SyncStatus.WillStartEL : SyncStatus.CL;
    }

    /** Stop. */
    public void stop() {
        this.l2Client.shutdown();
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
     * check is engine syncing
     * @return true if engine is syncing
     */
    public boolean isEngineSyncing() {
        return syncStatus.isEngineSyncing();
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
        if (this.syncStatus == SyncStatus.WillStartEL) {
            var l2Finalized = l2Client.ethGetBlockByNumber(DefaultBlockParameterName.FINALIZED, true)
                    .sendAsync()
                    .get();
            if (l2Finalized.hasError()
                    && (l2Finalized.getError().getMessage().contains("block not found")
                            || l2Finalized.getError().getMessage().contains("Unknown block"))) {
                this.syncStatus = SyncStatus.StartedEL;
                LOGGER.info("Starting EL sync");
            } else if (this.chainConfig.l2Genesis().number().compareTo(BigInteger.ZERO) != 0
                    && l2Finalized
                            .getBlock()
                            .getHash()
                            .equals(this.chainConfig.l2Genesis().hash())) {
                this.syncStatus = SyncStatus.StartedEL;
                LOGGER.info("Starting EL sync");
            } else {
                this.syncStatus = SyncStatus.FinishedEL;
                LOGGER.info("Skipping EL sync and going straight to CL sync because there is a finalized block");
                return;
            }
        }

        this.pushPayload(payload);
        this.unsafeHead = BlockInfo.from(payload);
        L2BlockRef l2BlockInfo = payload.toL2BlockInfo(this.chainConfig);
        if (this.syncStatus == SyncStatus.FinishedELNotFinalized) {
            BlockInfo l2NewHead = new BlockInfo(
                    l2BlockInfo.hash(), l2BlockInfo.number(), l2BlockInfo.parentHash(), l2BlockInfo.timestamp());
            Epoch l1NewEpoch = l2BlockInfo.l1origin();
            this.updateSafeHead(l2NewHead, l1NewEpoch, false);
            this.updateFinalized(l2NewHead, l1NewEpoch);
        }

        this.updateForkchoice();
        LOGGER.info("unsafe head updated: {} {}", this.unsafeHead.number(), this.unsafeHead.hash());
        if (this.syncStatus == SyncStatus.FinishedELNotFinalized) {
            this.syncStatus = SyncStatus.FinishedEL;
            LOGGER.info("EL sync finished");
        }
    }

    /**
     * Start building payload block insertion.
     *
     * @param parent the parent
     * @param attributes the attributes
     * @return the block insertion
     */
    public BlockInsertion startBuildingPayload(L2BlockRef parent, PayloadAttributes attributes) {
        if (this.isEngineSyncing()) {
            return BlockInsertion.TEMPORARY;
        }
        return BlockInsertion.SUCCESS;
    }

    /**
     * Confirm building payload.
     *
     * @return ExecutionPayloadEnvelop and insertion status
     */
    public Tuple2<ExecutionPayloadEnvelop, BlockInsertion> confirmBuildingPayload() {
        return null;
    }

    /**
     * Cancel building payload.
     * @param force if true then reset building state forcefully, otherwise throw exception
     */
    public void cancelPayload(boolean force) {
        if (this.buildingInfo == null) {
            return;
        }
        // e.log.Error("cancelling old block sealing job", "payload", e.buildingInfo.ID)
        LOGGER.error("cancelling old block sealing job: payload = {}", this.buildingInfo.payloadId());
        try {
            this.engine.getPayload(this.buildingInfo.timestamp(), Numeric.toBigInt(this.buildingInfo.payloadId()));
        } catch (Exception e) {
            if (!force) {
                throw new RuntimeException(e);
            }
            LOGGER.error("failed to cancel block building job: payload = {}", this.buildingInfo.payloadId(), e);
        }
        this.resetBuildingState();
    }

    /**
     * Reset building state.
     */
    public void resetBuildingState() {
        this.buildingInfo = null;
        this.buildingOnto = null;
        this.buildingSafe = false;
    }

    /**
     * Gets building payload info.
     *
     * @return the building payload
     */
    public Tuple3<L2BlockRef, String, Boolean> buildingPayload() {
        return new Tuple3<>(buildingOnto, buildingInfo.payloadId(), buildingSafe);
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
        if (this.syncModeEl) {
            // Skip check if EL sync is enabled
            return true;
        }
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
                    () -> l2Client.ethGetBlockByNumber(DefaultBlockParameter.valueOf(blockNumber), true)
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
            LOGGER.info("update unsafe head number({}) to new head({})", this.unsafeHead.number(), newHead.number());
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

            var forkChoiceUpdate = forkChoiceUpdateFuture.get();
            if (forkChoiceUpdate.hasError()) {
                throw new ForkchoiceUpdateException("could not accept new forkchoice: %s"
                        .formatted(forkChoiceUpdate.getError().getMessage()));
            }
            var forkChoiceUpdateStatus = forkChoiceUpdate.getForkChoiceUpdate().payloadStatus();
            var updateStatus = forkChoiceUpdateStatus.getStatus();
            if (this.syncStatus.isEngineSyncing()) {
                if (updateStatus == Status.VALID && this.syncStatus == SyncStatus.StartedEL) {
                    this.syncStatus = SyncStatus.FinishedELNotFinalized;
                }
                // Allow SYNCING if engine P2P sync is enabled
                if (updateStatus == Status.INVALID || forkChoiceUpdateStatus.getStatus() == Status.INVALID_BLOCK_HASH) {
                    throw new ForkchoiceUpdateException(String.format(
                            "could not accept new forkchoice: %s", forkChoiceUpdateStatus.getValidationError()));
                }
            } else {
                if (updateStatus != Status.VALID) {
                    throw new ForkchoiceUpdateException(String.format(
                            "could not accept new forkchoice: %s", forkChoiceUpdateStatus.getValidationError()));
                }
            }
        }
    }

    private void pushPayload(final ExecutionPayload payload) throws InterruptedException, ExecutionException {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            StructuredTaskScope.Subtask<OpEthPayloadStatus> payloadStatusFuture =
                    scope.fork(TracerTaskWrapper.wrap(() -> EngineDriver.this.engine.newPayload(payload)));

            scope.join();
            scope.throwIfFailed();
            OpEthPayloadStatus payloadStatus = payloadStatusFuture.get();
            if (payloadStatus.hasError()) {
                throw new InvalidExecutionPayloadException("the provided checkpoint payload is invalid:"
                        + payloadStatus.getError().getMessage());
            }
            PayloadStatus status = payloadStatus.getPayloadStatus();
            if (syncModeEl) {
                if (status.getStatus() == Status.VALID && this.syncStatus == SyncStatus.StartedEL) {
                    syncStatus = SyncStatus.FinishedELNotFinalized;
                }
                // Allow SYNCING and ACCEPTED if engine EL sync is enabled
                if (status.getStatus() != Status.VALID
                        && status.getStatus() != Status.ACCEPTED
                        && status.getStatus() != Status.SYNCING) {
                    throw new InvalidExecutionPayloadException("the provided checkpoint payload is invalid");
                }
            } else {
                if (status.getStatus() != Status.VALID && status.getStatus() != Status.ACCEPTED) {
                    throw new InvalidExecutionPayloadException("the provided checkpoint payload is invalid");
                }
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
            StructuredTaskScope.Subtask<OpEthExecutionPayload> payloadFuture = scope1.fork(TracerTaskWrapper.wrap(
                    () -> EngineDriver.this.engine.getPayload(attributes.timestamp(), payloadId)));

            scope1.join();
            scope1.throwIfFailed();
            res = payloadFuture.get();
        }
        return res;
    }

    private void skipAttributes(PayloadAttributes attributes, EthBlock block)
            throws ExecutionException, InterruptedException {
        Epoch newEpoch = Epoch.from(attributes.epoch(), attributes.seqNumber());
        BlockInfo newHead = BlockInfo.from(block.getBlock());
        this.updateSafeHead(newHead, newEpoch, false);
        this.updateForkchoice();
    }

    private void processAttributes(PayloadAttributes attributes) throws ExecutionException, InterruptedException {
        Epoch newEpoch = Epoch.from(attributes.epoch(), attributes.seqNumber());
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
