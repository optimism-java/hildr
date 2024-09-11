package io.optimism.runner;

import com.google.common.util.concurrent.AbstractExecutionThreadService;
import io.optimism.config.Config;
import io.optimism.config.Config.SyncMode;
import io.optimism.config.Config.SystemAccounts;
import io.optimism.driver.Driver;
import io.optimism.driver.ForkchoiceUpdateException;
import io.optimism.driver.InvalidExecutionPayloadException;
import io.optimism.engine.EngineApi;
import io.optimism.engine.OpEthForkChoiceUpdate;
import io.optimism.engine.OpEthPayloadStatus;
import io.optimism.exceptions.BlockNotIncludedException;
import io.optimism.exceptions.DriverInitException;
import io.optimism.exceptions.SyncUrlMissingException;
import io.optimism.exceptions.TransactionNotFoundException;
import io.optimism.exceptions.TrustedPeerAddedException;
import io.optimism.rpc.Web3jProvider;
import io.optimism.rpc.response.OpEthBlock;
import io.optimism.telemetry.TracerTaskWrapper;
import io.optimism.types.ExecutionPayload;
import io.optimism.types.ExecutionPayload.Status;
import io.optimism.types.ForkChoiceUpdate.ForkchoiceState;
import java.math.BigInteger;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.StructuredTaskScope;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.BooleanResponse;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.http.HttpService;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.utils.Numeric;

/** The type Runner. */
public class Runner extends AbstractExecutionThreadService {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(Runner.class);
    private static final String TRUSTED_PEER_ENODE =
            "enode://e85ba0beec172b17f53b373b0ab72238754259aa39f1ae5290e3244e0120882f4cf95acd203661a27"
                    + "c8618b27ca014d4e193266cb3feae43655ed55358eedb06@3.86.143.120:30303?discport=21693";
    private final Config config;

    private SyncMode syncMode;

    private String checkpointHash;

    private final EngineApi engineApi;
    private Driver<EngineApi> driver;

    private boolean isShutdownTriggered = false;

    private final CountDownLatch latch = new CountDownLatch(1);

    private final Executor executor;

    /**
     * Instantiates a new Runner.
     *
     * @param config the config
     * @param syncMode the sync mode
     * @param checkpointHash the checkpoint hash
     */
    public Runner(Config config, SyncMode syncMode, String checkpointHash) {
        this.config = config;
        this.syncMode = syncMode;
        this.checkpointHash = checkpointHash;
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
        this.engineApi = new EngineApi(this.config, this.config.l2EngineUrl(), this.config.jwtSecret());
        try {
            waitReady();
        } catch (InterruptedException e) {
            LOGGER.error("interrupted while waiting for engine to be ready", e);
            Thread.currentThread().interrupt();
            throw new DriverInitException(e);
        } catch (ExecutionException e) {
            LOGGER.error("execution exception while waiting for engine to be ready", e);
            throw new DriverInitException(e);
        }
    }

    /**
     * Wait ready.
     *
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException the execution exception
     */
    public void waitReady() throws InterruptedException, ExecutionException {
        boolean isAvailable;
        while (!Thread.interrupted()) {
            try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
                StructuredTaskScope.Subtask<Boolean> isAvailableFuture =
                        scope.fork(TracerTaskWrapper.wrap(engineApi::isAvailable));

                scope.join();
                scope.throwIfFailed();
                isAvailable = isAvailableFuture.get();
            }
            if (isAvailable) {
                break;
            } else {
                Thread.sleep(Duration.ofSeconds(3L));
            }
        }
    }

    /**
     * Sets sync mode.
     *
     * @param syncMode the sync mode
     * @return the sync mode
     */
    public Runner setSyncMode(SyncMode syncMode) {
        this.syncMode = syncMode;
        return this;
    }

    /**
     * Sets checkpoint hash.
     *
     * @param checkpointHash the checkpoint hash
     * @return the checkpoint hash
     */
    public Runner setCheckpointHash(String checkpointHash) {
        this.checkpointHash = checkpointHash;
        return this;
    }

    /** Fast sync. */
    public void fastSync() {
        LOGGER.error("fast sync is not implemented yet");
        throw new UnsupportedOperationException("fast sync is not implemented yet");
    }

    /** Challenge sync. */
    public void challengeSync() {
        LOGGER.error("challenge sync is not implemented yet");
        throw new UnsupportedOperationException("challenge sync is not implemented yet");
    }

    /**
     * Full sync.
     *
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException the execution exception
     */
    public void fullSync() throws InterruptedException, ExecutionException {
        LOGGER.info("starting full sync");
        waitDriverRunning();
    }

    private void waitDriverRunning() throws InterruptedException, ExecutionException {
        this.driver = Driver.from(this.config, this.latch);
        this.startDriver();
    }

    /**
     * Checkpoint sync.
     *
     * @throws ExecutionException the execution exception
     * @throws InterruptedException the interrupted exception
     */
    public void checkpointSync() throws ExecutionException, InterruptedException {
        Web3j l2Provider = Web3j.build(new HttpService(this.config.l2RpcUrl()));
        if (StringUtils.isEmpty(this.config.checkpointSyncUrl())) {
            throw new SyncUrlMissingException("a checkpoint sync rpc url is required for checkpoint sync");
        }
        Web3jService checkpointSyncUrl =
                Web3jProvider.create(this.config.checkpointSyncUrl()).component2();

        OpEthBlock checkpointBlock = null;
        if (StringUtils.isNotEmpty(this.checkpointHash)) {
            Tuple2<Boolean, OpEthBlock> isEpochBoundary = isEpochBoundary(this.checkpointHash, checkpointSyncUrl);
            if (!isEpochBoundary.component1() || isEpochBoundary.component2() == null) {
                LOGGER.error("could not get checkpoint block");
                throw new BlockNotIncludedException("could not get checkpoint block");
            }
            checkpointBlock = isEpochBoundary.component2();
        } else {
            LOGGER.info("finding the latest epoch boundary to use as checkpoint");
            BigInteger blockNumber = getEthBlockNumber(checkpointSyncUrl);

            while (isRunning() && !this.isShutdownTriggered) {
                Tuple2<Boolean, OpEthBlock> isEpochBoundary =
                        isEpochBoundary(DefaultBlockParameter.valueOf(blockNumber), checkpointSyncUrl);
                if (isEpochBoundary.component1()) {
                    checkpointBlock = isEpochBoundary.component2();
                    break;
                } else {
                    blockNumber = blockNumber.subtract(BigInteger.ONE);
                }
            }
        }

        if (checkpointBlock == null) {
            throw new BlockNotIncludedException("could not find checkpoint block");
        }
        String checkpointHash = checkpointBlock.getBlock().getHash();
        if (StringUtils.isEmpty(checkpointHash)) {
            throw new BlockNotIncludedException("block hash is missing");
        }
        LOGGER.info("found checkpoint block {}", checkpointHash);

        EthBlock l2CheckpointBlock;
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            StructuredTaskScope.Subtask<EthBlock> l2CheckpointBlockFuture = scope.fork(TracerTaskWrapper.wrap(
                    () -> l2Provider.ethGetBlockByHash(checkpointHash, true).send()));

            scope.join();
            scope.throwIfFailed();
            l2CheckpointBlock = l2CheckpointBlockFuture.get();
        }
        if (l2CheckpointBlock != null && l2CheckpointBlock.getBlock() != null) {
            LOGGER.warn("finalized head is above the checkpoint block");
            ForkchoiceState forkchoiceState = ForkchoiceState.fromSingleHead(checkpointHash);
            updateForkChoiceState(forkchoiceState);
            waitDriverRunning();
            return;
        }

        // this is a temporary fix to allow execution layer peering to work
        addTrustedPeerToL2Engine(l2Provider);

        ExecutionPayload checkpointPayload =
                ExecutionPayload.fromL2Block(checkpointBlock.getBlock(), this.config.chainConfig());
        ForkchoiceState forkchoiceState = ForkchoiceState.fromSingleHead(checkpointHash);

        newPayloadToCheckpoint(checkpointPayload);
        updateForkChoiceState(forkchoiceState);

        LOGGER.info("syncing execution client to the checkpoint block...");
        while (isRunning() && !this.isShutdownTriggered) {
            BigInteger blockNumber;
            try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
                StructuredTaskScope.Subtask<BigInteger> blockNumberFuture = scope.fork(TracerTaskWrapper.wrap(
                        () -> l2Provider.ethBlockNumber().send().getBlockNumber()));

                scope.join();
                scope.throwIfFailed();
                blockNumber = blockNumberFuture.get();
            }
            if (blockNumber.compareTo(checkpointPayload.blockNumber()) >= 0) {
                break;
            } else {
                Thread.sleep(Duration.ofSeconds(3L));
            }
        }

        // after syncing to the checkpoint block, update the forkchoice state
        updateForkChoiceState(forkchoiceState);
        LOGGER.info("execution client successfully synced to the checkpoint block");
        waitDriverRunning();
    }

    private static void addTrustedPeerToL2Engine(Web3j l2Provider) throws InterruptedException, ExecutionException {
        // TODO: use a list of whitelisted bootnodes instead
        LOGGER.info("adding trusted peer to the execution layer");
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            StructuredTaskScope.Subtask<BooleanResponse> isPeerAddedFuture = scope.fork(TracerTaskWrapper.wrap(
                    () -> l2Provider.adminAddPeer(TRUSTED_PEER_ENODE).send()));
            scope.join();
            scope.throwIfFailed();
            BooleanResponse isPeerAdded = isPeerAddedFuture.get();
            if (!isPeerAdded.success()) {
                throw new TrustedPeerAddedException("could not add peer");
            }
        }
    }

    /**
     * snap sync.
     *
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException the execution exception
     */
    public void executionLayerSync() throws InterruptedException, ExecutionException {
        LOGGER.info("execution layer sync");
        waitDriverRunning();
    }

    private void startDriver() throws InterruptedException {
        driver.startAsync().awaitRunning();
        latch.await();
    }

    private BigInteger getEthBlockNumber(Web3jService web3jService) throws InterruptedException, ExecutionException {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            StructuredTaskScope.Subtask<BigInteger> blockNumberFuture =
                    scope.fork(TracerTaskWrapper.wrap(() -> new Request<>(
                                    "eth_blockNumber",
                                    Collections.<String>emptyList(),
                                    web3jService,
                                    EthBlockNumber.class)
                            .send()
                            .getBlockNumber()));
            scope.join();
            scope.throwIfFailed();
            return blockNumberFuture.get();
        }
    }

    private void newPayloadToCheckpoint(ExecutionPayload checkpointPayload)
            throws InterruptedException, ExecutionException {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            StructuredTaskScope.Subtask<OpEthPayloadStatus> payloadStatusFuture =
                    scope.fork(TracerTaskWrapper.wrap(() -> engineApi.newPayload(checkpointPayload)));
            scope.join();
            scope.throwIfFailed();
            OpEthPayloadStatus payloadStatus = payloadStatusFuture.get();
            if (payloadStatus.getPayloadStatus().getStatus() == Status.INVALID
                    || payloadStatus.getPayloadStatus().getStatus() == Status.INVALID_BLOCK_HASH) {
                LOGGER.error("the provided checkpoint payload is invalid, exiting");
                throw new InvalidExecutionPayloadException("the provided checkpoint payload is invalid");
            }
        }
    }

    private void updateForkChoiceState(ForkchoiceState forkchoiceState)
            throws InterruptedException, ExecutionException {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            StructuredTaskScope.Subtask<OpEthForkChoiceUpdate> forkChoiceUpdateFuture =
                    scope.fork(TracerTaskWrapper.wrap(() -> engineApi.forkchoiceUpdated(forkchoiceState, null)));

            scope.join();
            scope.throwIfFailed();
            OpEthForkChoiceUpdate forkChoiceUpdate = forkChoiceUpdateFuture.get();

            if (forkChoiceUpdate.getForkChoiceUpdate().payloadStatus().getStatus() == Status.INVALID
                    || forkChoiceUpdate.getForkChoiceUpdate().payloadStatus().getStatus()
                            == Status.INVALID_BLOCK_HASH) {
                LOGGER.error("could not accept forkchoice, exiting");
                throw new ForkchoiceUpdateException("could not accept forkchoice, exiting");
            }
        }
    }

    private Tuple2<Boolean, OpEthBlock> isEpochBoundary(String blockHash, Web3jService checkpointSyncUrl)
            throws InterruptedException, ExecutionException {
        OpEthBlock block;
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            StructuredTaskScope.Subtask<OpEthBlock> blockFuture = scope.fork(TracerTaskWrapper.wrap(() -> new Request<>(
                            "eth_getBlockByHash", Arrays.asList(blockHash, true), checkpointSyncUrl, OpEthBlock.class)
                    .send()));
            scope.join();
            scope.throwIfFailed();
            block = blockFuture.get();
            if (block == null) {
                throw new BlockNotIncludedException("could not find block from checkpoint sync url");
            }
        }

        return isBlockBoundary(block);
    }

    private Tuple2<Boolean, OpEthBlock> isEpochBoundary(
            DefaultBlockParameter blockParameter, Web3jService checkpointSyncUrl)
            throws InterruptedException, ExecutionException {
        OpEthBlock block;
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            StructuredTaskScope.Subtask<OpEthBlock> blockFuture = scope.fork(TracerTaskWrapper.wrap(() -> new Request<>(
                            "eth_getBlockByNumber",
                            Arrays.asList(blockParameter.getValue(), true),
                            checkpointSyncUrl,
                            OpEthBlock.class)
                    .send()));
            scope.join();
            scope.throwIfFailed();
            block = blockFuture.get();
            if (block == null) {
                throw new RuntimeException("could not find block from checkpoint sync url");
            }
        }

        return isBlockBoundary(block);
    }

    @NotNull private Tuple2<Boolean, OpEthBlock> isBlockBoundary(OpEthBlock block) {
        String txInput = ((OpEthBlock.TransactionObject) block.getBlock().getTransactions().stream()
                        .filter(transactionResult -> ((OpEthBlock.TransactionObject) transactionResult)
                                .getTo()
                                .equalsIgnoreCase(
                                        SystemAccounts.defaultSystemAccounts().attributesPreDeploy()))
                        .findFirst()
                        .orElseThrow(() -> new TransactionNotFoundException(
                                "could not find setL1BlockValues tx in the epoch boundary" + " search")))
                .getInput();
        byte[] sequenceNumber;
        if (this.config.chainConfig().isEcotone(block.getBlock().getTimestamp())) {
            // this is ecotone block, read sequence number from 12 to 20
            sequenceNumber = ArrayUtils.subarray(Numeric.hexStringToByteArray(txInput), 12, 20);
        } else {
            // this is ecotone block, read sequence number from 132 to 164
            sequenceNumber = ArrayUtils.subarray(Numeric.hexStringToByteArray(txInput), 132, 164);
        }
        if (Arrays.equals(sequenceNumber, new byte[32]) || Arrays.equals(sequenceNumber, new byte[8])) {
            return new Tuple2<>(true, block);
        } else {
            return new Tuple2<>(false, null);
        }
    }

    /**
     * Create runner.
     *
     * @param config the config
     * @return the runner
     */
    public static Runner create(Config config) {
        return new Runner(config, SyncMode.Full, null);
    }

    @Override
    protected void run() throws Exception {
        switch (this.syncMode) {
            case Fast -> this.fastSync();
            case Challenge -> this.challengeSync();
            case Full -> this.fullSync();
            case Checkpoint -> this.checkpointSync();
            case ExecutionLayer -> this.executionLayerSync();
            default -> throw new RuntimeException("unknown sync mode");
        }
    }

    @Override
    protected void shutDown() {
        LOGGER.info("runner shut down");
        if (driver != null) {
            driver.stopAsync().awaitTerminated();
        }
        LOGGER.info("stopped driver");
    }

    @Override
    protected void triggerShutdown() {
        LOGGER.info("runner trigger shut down");
        this.isShutdownTriggered = true;
        this.latch.countDown();
    }

    @Override
    protected Executor executor() {
        return this.executor;
    }
}
