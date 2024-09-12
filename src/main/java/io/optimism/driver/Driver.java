package io.optimism.driver;

import static java.lang.Thread.sleep;
import static org.web3j.protocol.core.DefaultBlockParameterName.FINALIZED;
import static org.web3j.protocol.core.DefaultBlockParameterName.LATEST;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import io.optimism.config.Config;
import io.optimism.derive.Pipeline;
import io.optimism.engine.Engine;
import io.optimism.engine.EngineApi;
import io.optimism.exceptions.HildrServiceExecutionException;
import io.optimism.l1.ChainWatcher;
import io.optimism.network.OpStackNetwork;
import io.optimism.rpc.RpcMethod;
import io.optimism.rpc.RpcServer;
import io.optimism.rpc.Web3jProvider;
import io.optimism.rpc.internal.result.SyncStatusResult;
import io.optimism.telemetry.InnerMetrics;
import io.optimism.telemetry.TracerTaskWrapper;
import io.optimism.types.BlockId;
import io.optimism.types.BlockInfo;
import io.optimism.types.BlockUpdate;
import io.optimism.types.DepositTransaction;
import io.optimism.types.Epoch;
import io.optimism.types.ExecutionPayload;
import io.optimism.types.ExecutionPayload.PayloadAttributes;
import io.optimism.types.Genesis;
import io.optimism.types.L1BlockInfo;
import io.optimism.types.L2BlockRef;
import io.optimism.types.RollupConfigResult;
import io.optimism.types.SystemConfig;
import io.optimism.utilities.encoding.TxDecoder;
import java.math.BigInteger;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.jctools.queues.MessagePassingQueue;
import org.jctools.queues.MpscUnboundedXaddArrayQueue;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.utils.Numeric;

/**
 * The type Driver.
 *
 * @param <E> the type parameter
 * @author grapebaba
 * @since 0.1.0
 */
public class Driver<E extends Engine> extends AbstractExecutionThreadService {

    private static final Logger LOGGER = LoggerFactory.getLogger(Driver.class);
    private final Pipeline pipeline;

    private final EngineDriver<E> engineDriver;

    private final ISequencer sequencer;

    private final RpcServer rpcServer;

    private List<UnfinalizedBlock> unfinalizedBlocks;

    private BigInteger finalizedL1BlockNumber;

    private List<ExecutionPayload> futureUnsafeBlocks;

    private final BiFunction<DefaultBlockParameter, Boolean, Tuple2<BlockInfo, Epoch>> l2Fetcher;

    private final AtomicReference<io.optimism.derive.State> state;

    private final ChainWatcher chainWatcher;

    private final MessagePassingQueue<ExecutionPayload> unsafeBlockQueue;

    private final ExecutorService executor;

    private volatile boolean isShutdownTriggered;

    private CountDownLatch latch;

    private final Config config;

    private RollupConfigResult cachedRollConfig;

    private final OpStackNetwork opStackNetwork;

    private final AtomicBoolean isP2PNetworkStarted;

    private final AtomicBoolean isElsyncFinished;

    /**
     * Instantiates a new Driver.
     *
     * @param engineDriver     the engine driver
     * @param sequencer        the sequencer
     * @param pipeline         the pipeline
     * @param l2Fetcher        the L2 HeadInfo fetcher
     * @param state            the state
     * @param chainWatcher     the chain watcher
     * @param unsafeBlockQueue the unsafe block queue
     * @param rpcServer        the rpc server
     * @param latch            the close notifier
     * @param config           the chain config
     * @param opStackNetwork   the op stack network
     */
    @SuppressWarnings("preview")
    public Driver(
            EngineDriver<E> engineDriver,
            ISequencer sequencer,
            Pipeline pipeline,
            BiFunction<DefaultBlockParameter, Boolean, Tuple2<BlockInfo, Epoch>> l2Fetcher,
            AtomicReference<io.optimism.derive.State> state,
            ChainWatcher chainWatcher,
            MessagePassingQueue<ExecutionPayload> unsafeBlockQueue,
            RpcServer rpcServer,
            CountDownLatch latch,
            Config config,
            OpStackNetwork opStackNetwork) {
        this.engineDriver = engineDriver;
        this.sequencer = sequencer;
        this.rpcServer = rpcServer;
        this.pipeline = pipeline;
        this.l2Fetcher = l2Fetcher;
        this.state = state;
        this.chainWatcher = chainWatcher;
        this.unsafeBlockQueue = unsafeBlockQueue;
        this.futureUnsafeBlocks = Lists.newArrayList();
        this.unfinalizedBlocks = Lists.newArrayList();
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
        this.latch = latch;
        this.config = config;
        this.opStackNetwork = opStackNetwork;
        HashMap<String, Function> rpcHandler = HashMap.newHashMap(1);
        rpcHandler.put(RpcMethod.OP_SYNC_STATUS.getRpcMethodName(), unused -> this.getSyncStatus());
        rpcHandler.put(RpcMethod.OP_ROLLUP_CONFIG.getRpcMethodName(), unused -> this.getRollupConfig());
        this.rpcServer.register(rpcHandler);
        this.isP2PNetworkStarted = new AtomicBoolean(false);
        this.isElsyncFinished = new AtomicBoolean(false);
    }

    /**
     * Gets engine driver.
     *
     * @return the engine driver
     */
    public EngineDriver<E> getEngineDriver() {
        return engineDriver;
    }

    /**
     * From driver.
     *
     * @param config the config
     * @param latch  the latch
     * @return the driver
     * @throws InterruptedException the interrupted exception
     * @throws ExecutionException   the execution exception
     */
    public static Driver<EngineApi> from(Config config, CountDownLatch latch)
            throws InterruptedException, ExecutionException {
        final Web3j l2Provider = Web3jProvider.createClient(config.l2RpcUrl());

        EthBlock finalizedBlock;
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            var parameter = config.devnet() != null && config.devnet() ? LATEST : FINALIZED;
            StructuredTaskScope.Subtask<EthBlock> finalizedBlockFuture = scope.fork(TracerTaskWrapper.wrap(
                    () -> l2Provider.ethGetBlockByNumber(parameter, true).send()));
            scope.join();
            scope.throwIfFailed();

            finalizedBlock = finalizedBlockFuture.get();
        }

        HeadInfo head;
        if (finalizedBlock == null
                || finalizedBlock.getBlock() == null
                || CollectionUtils.isEmpty(finalizedBlock.getBlock().getTransactions())) {
            LOGGER.warn("could not get head info. Falling back to the genesis head.");
            head = new HeadInfo(
                    config.chainConfig().l2Genesis(), config.chainConfig().l1StartEpoch(), BigInteger.ZERO);
        } else {
            try {
                head = HeadInfo.from(finalizedBlock.getBlock());
            } catch (Throwable throwable) {
                LOGGER.warn("could not parse head info. Falling back to the genesis head.", throwable);
                head = new HeadInfo(
                        config.chainConfig().l2Genesis(), config.chainConfig().l1StartEpoch(), BigInteger.ZERO);
            }
        }

        BlockInfo finalizedHead = head.l2BlockInfo();
        Epoch finalizedEpoch = head.l1Epoch();
        BigInteger finalizedSeq = head.sequenceNumber();

        LOGGER.info("starting from head: number={}, hash={}", finalizedHead.number(), finalizedHead.hash());
        BigInteger l1StartBlock =
                finalizedEpoch.number().subtract(config.chainConfig().channelTimeout(finalizedEpoch.timestamp()));
        ChainWatcher watcher = new ChainWatcher(
                l1StartBlock.compareTo(BigInteger.ZERO) < 0 ? BigInteger.ZERO : l1StartBlock,
                finalizedHead.number(),
                config);
        TreeMap<BigInteger, Tuple2<BlockInfo, Epoch>> l2Refs;
        if (config.syncMode().isEl()) {
            finalizedHead = BlockInfo.EMPTY;
            l2Refs = new TreeMap<>();
        } else {
            l2Refs = io.optimism.derive.State.initL2Refs(
                    finalizedHead.number(), finalizedEpoch.timestamp(), config.chainConfig(), l2Provider);
        }
        var l2Fetcher = Driver.l2Fetcher(l2Provider);
        AtomicReference<io.optimism.derive.State> state = new AtomicReference<>(
                io.optimism.derive.State.create(l2Refs, l2Fetcher, finalizedHead, finalizedEpoch, config));

        EngineDriver<EngineApi> engineDriver = new EngineDriver<>(finalizedHead, finalizedEpoch, l2Provider, config);

        Pipeline pipeline = new Pipeline(state, config, finalizedSeq);

        RpcServer rpcServer = new RpcServer(config);
        rpcServer.start();

        MpscUnboundedXaddArrayQueue<ExecutionPayload> unsafeBlockQueue = new MpscUnboundedXaddArrayQueue<>(1024 * 64);
        OpStackNetwork opStackNetwork = new OpStackNetwork(config, unsafeBlockQueue);
        ISequencer sequencer = null;
        if (config.sequencerEnable()) {
            sequencer = new Sequencer(engineDriver, config.chainConfig());
        }

        l2Provider.shutdown();
        return new Driver<>(
                engineDriver,
                sequencer,
                pipeline,
                l2Fetcher,
                state,
                watcher,
                unsafeBlockQueue,
                rpcServer,
                latch,
                config,
                opStackNetwork);
    }

    /**
     * Get rollupConfig.
     *
     * @return The rollup config
     */
    public RollupConfigResult getRollupConfig() {
        var chainConfig = this.config.chainConfig();
        if (this.cachedRollConfig == null) {
            var rollupConfig = getRollupConfig(chainConfig);
            var curSysConfig = chainConfig.systemConfig();
            var latestGenesis = getLatestGenesis(curSysConfig, chainConfig);
            this.cachedRollConfig.setGenesis(latestGenesis);
            this.cachedRollConfig = rollupConfig;
        }
        return this.cachedRollConfig;
    }

    @NotNull private static Genesis getLatestGenesis(Config.SystemConfig curSysConfig, Config.ChainConfig chainConfig) {
        var sc = new SystemConfig(
                curSysConfig.batchSender(),
                curSysConfig.l1FeeOverhead(),
                curSysConfig.l1FeeScalar(),
                curSysConfig.gasLimit());
        var latestGenesis = new Genesis(
                chainConfig.l1StartEpoch(),
                new BlockId(
                        chainConfig.l2Genesis().hash(), chainConfig.l2Genesis().number()),
                chainConfig.l2Genesis().timestamp(),
                sc);
        return latestGenesis;
    }

    @NotNull private static RollupConfigResult getRollupConfig(Config.ChainConfig chainConfig) {
        var rollupConfig = new RollupConfigResult();
        rollupConfig.setBlockTime(chainConfig.blockTime());
        rollupConfig.setMaxSequencerDrift(chainConfig.maxSeqDrift());
        rollupConfig.setSeqWindowSize(chainConfig.seqWindowSize());
        rollupConfig.setChannelTimeout(chainConfig.channelTimeout());
        rollupConfig.setL1ChainId(chainConfig.l1ChainId());
        rollupConfig.setL2ChainId(chainConfig.l2ChainId());
        rollupConfig.setRegolithTime(chainConfig.regolithTime());
        rollupConfig.setBatchInboxAddress(chainConfig.batchInbox());
        rollupConfig.setDepositContractAddress(chainConfig.depositContract());
        rollupConfig.setL1SystemConfigAddress(chainConfig.systemConfigContract());
        return rollupConfig;
    }

    /**
     * Get sync status.
     *
     * @return result of sync status.
     */
    public SyncStatusResult getSyncStatus() {
        if (this.engineDriver.isEngineSyncing()) {
            return null;
        }
        // CurrentL1
        final var currentL1 = this.chainWatcher.getCurrentL1();
        // CurrentL1Finalized
        final var currentL1Finalized = this.chainWatcher.getCurrentL1Finalized();

        final var l1HeadBlock = this.chainWatcher.getL1HeadBlock();
        final var l1SafeBlock = this.chainWatcher.getL1SafeBlock();
        final var l1FinalizedBlock = this.chainWatcher.getL1FinalizedBlock();

        // FinalizedL2
        final var finalizedHead = this.engineDriver.getFinalizedHead();
        // SafeL2
        final var safeHead = this.engineDriver.getSafeHead();
        // UnsafeL2
        final var unsafeHead = this.engineDriver.getUnsafeHead();

        final var unsafeL2SyncTarget = this.unsafeL2SyncTarget();
        return new SyncStatusResult(
                currentL1,
                currentL1Finalized,
                l1HeadBlock,
                l1SafeBlock,
                l1FinalizedBlock,
                unsafeHead,
                safeHead,
                finalizedHead,
                unsafeL2SyncTarget,
                unsafeHead);
    }

    @Override
    protected void run() {
        while (isRunning() && !isShutdownTriggered) {
            try {
                this.advance();
                this.sequencerAction();
            } catch (InterruptedException e) {
                LOGGER.error("driver run interrupted", e);
                this.latch.countDown();
                Thread.currentThread().interrupt();
                throw new HildrServiceExecutionException(e);
            } catch (Throwable e) {
                LOGGER.error("driver run fatal error", e);
                this.latch.countDown();
                throw new HildrServiceExecutionException(e);
            }
        }
    }

    @Override
    protected void startUp() {
        try {
            this.awaitEngineReady();
        } catch (InterruptedException e) {
            LOGGER.error("driver start interrupted", e);
            Thread.currentThread().interrupt();
            throw new HildrServiceExecutionException(e);
        } catch (Throwable e) {
            LOGGER.error("driver start fatal error", e);
            throw new HildrServiceExecutionException(e);
        }
        if (!Driver.this.config.syncMode().isEl()) {
            this.chainWatcher.start();
        }
    }

    @Override
    protected Executor executor() {
        return this.executor;
    }

    @Override
    protected void shutDown() {
        LOGGER.info("driver shut down.");
        this.chainWatcher.stop();
        LOGGER.info("chainWatcher shut down.");
        this.executor.shutdown();
        LOGGER.info("executor shut down.");
        this.engineDriver.stop();
        LOGGER.info("engineDriver shut down.");
        this.rpcServer.stop();
        LOGGER.info("driver stopped.");
        if (this.opStackNetwork != null && this.isP2PNetworkStarted.compareAndExchange(true, false)) {
            this.opStackNetwork.stop();
            LOGGER.info("opStackNetwork stopped.");
        }
    }

    @Override
    protected void triggerShutdown() {
        LOGGER.info("driver trigger shut down");
        this.isShutdownTriggered = true;
    }

    private void awaitEngineReady() throws InterruptedException {
        while (!this.engineDriver.engineReady()) {
            sleep(Duration.ofSeconds(1));
        }
    }

    private void sequencerAction() throws InterruptedException, ExecutionException {
        if (sequencer == null || !this.isP2PNetworkStarted.get()) {
            return;
        }
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            var task = scope.fork(TracerTaskWrapper.wrap((Callable<Void>) () -> {
                Driver.this.sequencer.runNextSequencerAction();
                return null;
            }));
            scope.join();
            scope.throwIfFailed();
            task.get();
        }
    }

    @SuppressWarnings("VariableDeclarationUsageDistance")
    private void advance() throws InterruptedException, ExecutionException {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            var voidFuture = scope.fork(TracerTaskWrapper.wrap((Callable<Void>) () -> {
                Driver.this.advanceSafeHead();
                return null;
            }));

            scope.join();
            scope.throwIfFailed();
            voidFuture.get();
        }

        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            var voidFuture = scope.fork(TracerTaskWrapper.wrap((Callable<Void>) () -> {
                Driver.this.advanceUnsafeHead();
                return null;
            }));
            scope.join();
            scope.throwIfFailed();
            voidFuture.get();
        }
        this.updateFinalized();
        this.updateMetrics();

        this.tryStartNetwork();
    }

    private void advanceSafeHead() throws ExecutionException, InterruptedException {
        this.handleNextBlockUpdate();
        this.updateStateHead();

        for (PayloadAttributes payloadAttributes = this.pipeline.next();
                payloadAttributes != null;
                payloadAttributes = this.pipeline.next()) {
            BigInteger l1InclusionBlock = payloadAttributes.l1InclusionBlock();
            if (l1InclusionBlock == null) {
                throw new InvalidAttributesException("attributes without inclusion block");
            }
            if (Driver.this.engineDriver.isEngineSyncing()) {
                LOGGER.info("engine is syncing, skipping payload");
                continue;
            }
            BigInteger seqNumber = payloadAttributes.seqNumber();
            if (seqNumber == null) {
                throw new InvalidAttributesException("attributes without seq number");
            }

            Driver.this.engineDriver.handleAttributes(payloadAttributes);
            LOGGER.info(
                    "safe head updated: {} {}",
                    Driver.this.engineDriver.getSafeHead().number(),
                    Driver.this.engineDriver.getSafeHead().hash());

            final BlockInfo newSafeHead = Driver.this.engineDriver.getSafeHead();
            final Epoch safeEpoch = Driver.this.engineDriver.getSafeEpoch();
            final Epoch newSafeEpoch = Epoch.from(safeEpoch, seqNumber);

            Driver.this.state.getAndUpdate(state -> {
                state.updateSafeHead(newSafeHead, newSafeEpoch);
                return state;
            });

            UnfinalizedBlock newUnfinalizedBlock =
                    new UnfinalizedBlock(newSafeHead, newSafeEpoch, l1InclusionBlock, seqNumber);

            Driver.this.unfinalizedBlocks.add(newUnfinalizedBlock);
        }
    }

    private void advanceUnsafeHead() throws ExecutionException, InterruptedException {
        for (ExecutionPayload payload = this.unsafeBlockQueue.poll();
                payload != null;
                payload = this.unsafeBlockQueue.poll()) {
            BigInteger unsafeBlockNum = payload.blockNumber();
            BigInteger syncedBlockNum = Driver.this.engineDriver.getUnsafeHead().number();

            if (this.engineDriver.isEngineSyncing()
                    && (syncedBlockNum.compareTo(BigInteger.ZERO) == 0
                            || unsafeBlockNum.compareTo(syncedBlockNum) > 0)) {
                this.futureUnsafeBlocks.add(payload);
            } else {
                this.futureUnsafeBlocks = this.futureUnsafeBlocks.stream()
                        .filter(executionPayload ->
                                executionPayload.blockNumber().compareTo(syncedBlockNum) > 0
                                        && executionPayload
                                                        .blockNumber()
                                                        .subtract(syncedBlockNum)
                                                        .compareTo(BigInteger.valueOf(1024L))
                                                < 0)
                        .collect(Collectors.toList());
            }
        }
        if (this.futureUnsafeBlocks.isEmpty()) {
            return;
        }
        LOGGER.debug("will handle future unsafe blocks: size={}", this.futureUnsafeBlocks.size());
        Optional<ExecutionPayload> nextUnsafePayload;
        if (Driver.this.engineDriver.isEngineSyncing()) {
            nextUnsafePayload = Optional.of(this.futureUnsafeBlocks.removeFirst());
        } else {
            nextUnsafePayload = Iterables.tryFind(this.futureUnsafeBlocks, input -> input.parentHash()
                            .equalsIgnoreCase(
                                    Driver.this.engineDriver.getUnsafeHead().hash()))
                    .toJavaUtil();
        }

        if (nextUnsafePayload.isEmpty()) {
            return;
        }
        try {
            LOGGER.debug(
                    "will handle unsafe payload block hash: {}",
                    nextUnsafePayload.get().blockHash());
            this.engineDriver.handleUnsafePayload(nextUnsafePayload.get());
        } catch (ForkchoiceUpdateException | InvalidExecutionPayloadException e) {
            if (!this.config.syncMode().isEl()) {
                throw e;
            }
            // Ignore fork choice update exception during EL syncing
            LOGGER.warn("Failed to insert unsafe payload for EL sync: ", e);
        }
        if (!this.config.syncMode().isEl() || this.engineDriver.isEngineSyncing()) {
            return;
        }
        if (!this.isElsyncFinished.compareAndExchange(false, true)) {
            LOGGER.info("execution layer syncing is done, restarting chain watcher.");
            this.fetchAndUpdateFinalizedHead();
            this.restartChainWatcher();
        }
    }

    private void updateStateHead() {
        this.state.getAndUpdate(state -> {
            state.updateSafeHead(this.engineDriver.getSafeHead(), this.engineDriver.getSafeEpoch());
            return state;
        });
    }

    @SuppressWarnings("preview")
    private void handleNextBlockUpdate() {
        BlockUpdate next = this.chainWatcher.getBlockUpdateQueue().poll();
        if (next == null) {
            return;
        }

        switch (next) {
            case BlockUpdate.NewBlock l1info -> {
                BigInteger num = l1info.get().blockInfo().number();
                Driver.this.pipeline.pushBatcherTransactions(
                        l1info.get().batcherTransactions().stream()
                                .map(Numeric::hexStringToByteArray)
                                .collect(Collectors.toList()),
                        num);

                Driver.this.state.getAndUpdate(state -> {
                    state.updateL1Info(((BlockUpdate.NewBlock) next).get());
                    return state;
                });
            }
            case BlockUpdate.Reorg ignored -> {
                LOGGER.warn("reorg detected, purging pipeline");
                Driver.this.unfinalizedBlocks.clear();
                Driver.this.restartChainWatcher();
            }
            case BlockUpdate.FinalityUpdate num -> Driver.this.finalizedL1BlockNumber = num.get();
            default -> throw new IllegalArgumentException("unknown block update type");
        }
    }

    private void restartChainWatcher() {
        BigInteger channelTimeout = this.config
                .chainConfig()
                .channelTimeout(Driver.this.engineDriver.getFinalizedEpoch().timestamp());
        Driver.this.chainWatcher.restart(
                Driver.this.engineDriver.getFinalizedEpoch().number().subtract(channelTimeout),
                Driver.this.engineDriver.getFinalizedHead().number());

        Driver.this.state.getAndUpdate(state -> {
            state.purge(Driver.this.engineDriver.getFinalizedHead(), Driver.this.engineDriver.getFinalizedEpoch());
            return state;
        });
        Driver.this.pipeline.purge();
        Driver.this.engineDriver.reorg();
    }

    private void updateFinalized() {
        UnfinalizedBlock newFinalized = Iterables.getLast(
                this.unfinalizedBlocks.stream()
                        .filter(unfinalizedBlock ->
                                unfinalizedBlock.l1InclusionBlock().compareTo(Driver.this.finalizedL1BlockNumber) <= 0
                                        && unfinalizedBlock.seqNumber().compareTo(BigInteger.ZERO) == 0)
                        .collect(Collectors.toList()),
                null);

        if (newFinalized != null) {
            this.engineDriver.updateFinalized(newFinalized.head(), newFinalized.epochWithSeq());
        }

        this.unfinalizedBlocks = this.unfinalizedBlocks.stream()
                .filter(unfinalizedBlock ->
                        unfinalizedBlock.l1InclusionBlock().compareTo(Driver.this.finalizedL1BlockNumber) > 0)
                .collect(Collectors.toList());
    }

    private void updateMetrics() {
        InnerMetrics.setFinalizedHead(this.engineDriver.getFinalizedHead().number());
        InnerMetrics.setSafeHead(this.engineDriver.getSafeHead().number());
        InnerMetrics.setSynced(this.unfinalizedBlocks.isEmpty() ? BigInteger.ZERO : BigInteger.ONE);
    }

    private boolean synced() {
        return !this.unfinalizedBlocks.isEmpty();
    }

    private void tryStartNetwork() {
        if ((this.synced() || this.config.syncMode().isEl())
                && this.opStackNetwork != null
                && !this.isP2PNetworkStarted.compareAndExchange(false, true)) {
            this.opStackNetwork.start();
        }
    }

    /**
     * Retrieves the first queued-up L2 unsafe payload, or a zeroed reference if there is none.
     *
     * @return the L2BlockRef instance
     */
    private L2BlockRef unsafeL2SyncTarget() {
        ExecutionPayload unsafePayload = this.unsafeBlockQueue.peek();
        if (unsafePayload == null) {
            return L2BlockRef.EMPTY;
        }
        return Driver.payloadToRef(unsafePayload, this.config.chainConfig());
    }

    /**
     * Read L2BlockRef info from Execution payload.
     *
     * @param payload l2 execution payload info
     * @param genesis L2 genesis info
     * @return L2BlockRef instance
     */
    public static L2BlockRef payloadToRef(ExecutionPayload payload, Config.ChainConfig genesis) {
        Epoch l1Origin;
        BigInteger sequenceNumber;
        if (payload.blockNumber().compareTo(genesis.l2Genesis().number()) == 0) {
            if (!payload.blockHash().equals(genesis.l2Genesis().hash())) {
                throw new RuntimeException(String.format(
                        "expected L2 genesis hash to match L2 block at genesis block number %d: %s <> %s",
                        genesis.l2Genesis().number(),
                        payload.blockHash(),
                        genesis.l2Genesis().hash()));
            }
            l1Origin = genesis.l1StartEpoch();
            sequenceNumber = BigInteger.ZERO;
        } else {
            if (payload.transactions().isEmpty()) {
                throw new RuntimeException(
                        String.format("l2 block is missing L1 info deposit tx, block hash: %s", payload.blockHash()));
            }
            DepositTransaction depositTx =
                    TxDecoder.decodeToDeposit(payload.transactions().get(0));
            L1BlockInfo info = L1BlockInfo.from(Numeric.hexStringToByteArray(depositTx.getData()));
            l1Origin = info.toEpoch();
            sequenceNumber = info.sequenceNumber();
        }

        return new L2BlockRef(
                payload.blockHash(),
                payload.blockNumber(),
                payload.parentHash(),
                payload.timestamp(),
                l1Origin,
                sequenceNumber);
    }

    private void fetchAndUpdateFinalizedHead() {
        DefaultBlockParameter blockParameter;
        if (this.engineDriver.getFinalizedHead().number().compareTo(BigInteger.ZERO) == 0) {
            blockParameter = FINALIZED;
        } else {
            blockParameter = DefaultBlockParameter.valueOf(
                    this.engineDriver.getFinalizedHead().number());
        }
        Tuple2<BlockInfo, Epoch> finalizedHead = l2Fetcher.apply(blockParameter, true);
        this.engineDriver.updateFinalized(finalizedHead.component1(), finalizedHead.component2());
    }

    private static BiFunction<DefaultBlockParameter, Boolean, Tuple2<BlockInfo, Epoch>> l2Fetcher(
            final Web3j l2Provider) {
        return (blockParameter, returnFull) -> {
            try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
                StructuredTaskScope.Subtask<EthBlock> blockTask = scope.fork(TracerTaskWrapper.wrap(() -> l2Provider
                        .ethGetBlockByNumber(blockParameter, returnFull)
                        .send()));
                scope.join();
                scope.throwIfFailed();

                var block = blockTask.get();
                if (block == null || block.getBlock() == null) {
                    return null;
                }
                final HeadInfo l2BlockInfo = HeadInfo.from(block.getBlock());
                return new Tuple2<>(l2BlockInfo.l2BlockInfo(), l2BlockInfo.l1Epoch());
            } catch (Exception e) {
                LOGGER.error("failed to fetch L2 block", e);
                return null;
            }
        };
    }

    /**
     * The type Unfinalized block.
     *
     * @param head             the head
     * @param epoch            the epoch
     * @param l1InclusionBlock the L1 inclusion block
     * @param seqNumber        the seq number
     */
    protected record UnfinalizedBlock(BlockInfo head, Epoch epoch, BigInteger l1InclusionBlock, BigInteger seqNumber) {
        /**
         * create Epoch with sequence number.
         * @return a new Epoch.
         */
        public Epoch epochWithSeq() {
            return Epoch.from(epoch, seqNumber);
        }
    }
}
