/*
 * Copyright 2023 281165273grape@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package io.optimism.driver;

import static java.lang.Thread.sleep;
import static org.web3j.protocol.core.DefaultBlockParameterName.FINALIZED;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import io.optimism.common.BlockInfo;
import io.optimism.common.Epoch;
import io.optimism.common.HildrServiceExecutionException;
import io.optimism.config.Config;
import io.optimism.derive.Pipeline;
import io.optimism.engine.Engine;
import io.optimism.engine.EngineApi;
import io.optimism.engine.ExecutionPayload;
import io.optimism.engine.ExecutionPayload.PayloadAttributes;
import io.optimism.l1.BlockUpdate;
import io.optimism.l1.ChainWatcher;
import io.optimism.network.OpStackNetwork;
import io.optimism.rpc.RpcMethod;
import io.optimism.rpc.RpcServer;
import io.optimism.rpc.internal.result.SyncStatusResult;
import io.optimism.telemetry.InnerMetrics;
import io.optimism.type.BlockId;
import io.optimism.type.DepositTransaction;
import io.optimism.type.Genesis;
import io.optimism.type.L1BlockInfo;
import io.optimism.type.L2BlockRef;
import io.optimism.type.RollupConfigResult;
import io.optimism.type.SystemConfig;
import io.optimism.utilities.TxDecoder;
import io.optimism.utilities.rpc.Web3jProvider;
import io.optimism.utilities.telemetry.TracerTaskWrapper;
import java.math.BigInteger;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import jdk.incubator.concurrent.StructuredTaskScope;
import org.jctools.queues.MessagePassingQueue;
import org.jctools.queues.MpscUnboundedXaddArrayQueue;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlock;
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

    private final RpcServer rpcServer;

    private List<UnfinalizedBlock> unfinalizedBlocks;

    private BigInteger finalizedL1BlockNumber;

    private List<ExecutionPayload> futureUnsafeBlocks;

    private final AtomicReference<io.optimism.derive.State> state;

    private final ChainWatcher chainWatcher;

    private final MessagePassingQueue<ExecutionPayload> unsafeBlockQueue;

    private BigInteger channelTimeout;

    private final ExecutorService executor;

    private volatile boolean isShutdownTriggered;

    private CountDownLatch latch;

    private final Config config;

    private RollupConfigResult cachedRollConfig;

    private OpStackNetwork opStackNetwork;

    /**
     * Instantiates a new Driver.
     *
     * @param engineDriver     the engine driver
     * @param pipeline         the pipeline
     * @param state            the state
     * @param chainWatcher     the chain watcher
     * @param unsafeBlockQueue the unsafe block queue
     * @param rpcServer        the rpc server
     * @param latch            the close notifier
     * @param config           the chain config
     */
    @SuppressWarnings("preview")
    public Driver(
            EngineDriver<E> engineDriver,
            Pipeline pipeline,
            AtomicReference<io.optimism.derive.State> state,
            ChainWatcher chainWatcher,
            MessagePassingQueue<ExecutionPayload> unsafeBlockQueue,
            RpcServer rpcServer,
            CountDownLatch latch,
            Config config,
            OpStackNetwork opStackNetwork) {
        this.engineDriver = engineDriver;
        this.rpcServer = rpcServer;
        this.pipeline = pipeline;
        this.state = state;
        this.chainWatcher = chainWatcher;
        this.unsafeBlockQueue = unsafeBlockQueue;
        this.futureUnsafeBlocks = Lists.newArrayList();
        this.unfinalizedBlocks = Lists.newArrayList();
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
        this.latch = latch;
        this.config = config;
        this.channelTimeout = config.chainConfig().channelTimeout();
        this.opStackNetwork = opStackNetwork;
        HashMap<String, Function> rpcHandler = HashMap.newHashMap(1);
        rpcHandler.put(RpcMethod.OP_SYNC_STATUS.getRpcMethodName(), unused -> this.getSyncStatus());
        rpcHandler.put(RpcMethod.OP_ROLLUP_CONFIG.getRpcMethodName(), unused -> this.getRollupConfig());
        this.rpcServer.register(rpcHandler);
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
        Web3j provider = Web3jProvider.createClient(config.l2RpcUrl());

        EthBlock finalizedBlock;
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            Future<EthBlock> finalizedBlockFuture = scope.fork(TracerTaskWrapper.wrap(
                    () -> provider.ethGetBlockByNumber(FINALIZED, true).send()));
            scope.join();
            scope.throwIfFailed();

            finalizedBlock = finalizedBlockFuture.resultNow();
        }

        HeadInfo head;
        if (finalizedBlock == null) {
            LOGGER.warn("could not get head info. Falling back to the genesis head.");
            head = new HeadInfo(
                    config.chainConfig().l2Genesis(), config.chainConfig().l1StartEpoch(), BigInteger.ZERO);
        } else {
            try {
                head = HeadInfo.from(finalizedBlock.getBlock());
            } catch (Throwable throwable) {
                LOGGER.warn("could not get head info. Falling back to the genesis head.");
                head = new HeadInfo(
                        config.chainConfig().l2Genesis(), config.chainConfig().l1StartEpoch(), BigInteger.ZERO);
            }
        }

        BlockInfo finalizedHead = head.l2BlockInfo();
        Epoch finalizedEpoch = head.l1Epoch();
        BigInteger finalizedSeq = head.sequenceNumber();

        LOGGER.info("starting from head: {}", finalizedHead.hash());

        ChainWatcher watcher = new ChainWatcher(
                finalizedEpoch.number().subtract(config.chainConfig().channelTimeout()),
                finalizedHead.number(),
                config);

        AtomicReference<io.optimism.derive.State> state =
                new AtomicReference<>(io.optimism.derive.State.create(finalizedHead, finalizedEpoch, config));

        EngineDriver<EngineApi> engineDriver = new EngineDriver<>(finalizedHead, finalizedEpoch, provider, config);

        Pipeline pipeline = new Pipeline(state, config, finalizedSeq);

        RpcServer rpcServer = new RpcServer(config);
        rpcServer.start();

        MpscUnboundedXaddArrayQueue<ExecutionPayload> unsafeBlockQueue = new MpscUnboundedXaddArrayQueue<>(1024 * 64);
        OpStackNetwork opStackNetwork = new OpStackNetwork(config.chainConfig(), unsafeBlockQueue);

        provider.shutdown();
        return new Driver<>(
                engineDriver, pipeline, state, watcher, unsafeBlockQueue, rpcServer, latch, config, opStackNetwork);
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
                new BlockId(
                        chainConfig.l1StartEpoch().hash(),
                        chainConfig.l1StartEpoch().number()),
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
        this.chainWatcher.start();
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
        if (this.opStackNetwork != null) {
            this.opStackNetwork.stop();
            LOGGER.info("opStackNetwork stopped.");
        }
        this.tryStartNetwork();
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

    @SuppressWarnings("VariableDeclarationUsageDistance")
    private void advance() throws InterruptedException, ExecutionException {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            var voidFuture = scope.fork(TracerTaskWrapper.wrap((Callable<Void>) () -> {
                Driver.this.advanceSafeHead();
                return null;
            }));

            scope.join();
            scope.throwIfFailed();
            voidFuture.resultNow();
        }

        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            var voidFuture = scope.fork(TracerTaskWrapper.wrap((Callable<Void>) () -> {
                Driver.this.advanceUnsafeHead();
                return null;
            }));
            scope.join();
            scope.throwIfFailed();
            voidFuture.resultNow();
        }
        this.updateFinalized();
        this.updateMetrics();
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
            final Epoch newSafeEpoch = Driver.this.engineDriver.getSafeEpoch();

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
            this.futureUnsafeBlocks.add(payload);
        }

        this.futureUnsafeBlocks = this.futureUnsafeBlocks.stream()
                .filter(payload -> {
                    BigInteger unsafeBlockNum = payload.blockNumber();
                    BigInteger syncedBlockNum =
                            Driver.this.engineDriver.getUnsafeHead().number();
                    return unsafeBlockNum.compareTo(syncedBlockNum) > 0
                            && unsafeBlockNum.subtract(syncedBlockNum).compareTo(BigInteger.valueOf(1024L)) < 0;
                })
                .collect(Collectors.toList());

        Optional<ExecutionPayload> nextUnsafePayload = Iterables.tryFind(
                        this.futureUnsafeBlocks, input -> input.parentHash()
                                .equalsIgnoreCase(
                                        Driver.this.engineDriver.getUnsafeHead().hash()))
                .toJavaUtil();

        if (nextUnsafePayload.isPresent()) {
            this.engineDriver.handleUnsafePayload(nextUnsafePayload.get());
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

                Driver.this.chainWatcher.restart(
                        Driver.this.engineDriver.getFinalizedEpoch().number().subtract(this.channelTimeout),
                        Driver.this.engineDriver.getFinalizedHead().number());

                Driver.this.state.getAndUpdate(state -> {
                    state.purge(
                            Driver.this.engineDriver.getFinalizedHead(), Driver.this.engineDriver.getFinalizedEpoch());
                    return state;
                });

                Driver.this.pipeline.purge();
                Driver.this.engineDriver.reorg();
            }
            case BlockUpdate.FinalityUpdate num -> Driver.this.finalizedL1BlockNumber = num.get();
            default -> throw new IllegalArgumentException("unknown block update type");
        }
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
            this.engineDriver.updateFinalized(newFinalized.head(), newFinalized.epoch());
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
        if (this.synced() && this.opStackNetwork != null) {
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
        BlockId l1Origin;
        BigInteger sequenceNumber;
        if (payload.blockNumber().compareTo(genesis.l2Genesis().number()) == 0) {
            if (!payload.blockHash().equals(genesis.l2Genesis().hash())) {
                throw new RuntimeException(String.format(
                        "expected L2 genesis hash to match L2 block at genesis block number %d: %s <> %s",
                        genesis.l2Genesis().number(),
                        payload.blockHash(),
                        genesis.l2Genesis().hash()));
            }
            l1Origin = new BlockId(
                    genesis.l1StartEpoch().hash(), genesis.l1StartEpoch().number());
            sequenceNumber = BigInteger.ZERO;
        } else {
            if (payload.transactions().size() == 0) {
                throw new RuntimeException(
                        String.format("l2 block is missing L1 info deposit tx, block hash: %s", payload.blockHash()));
            }
            DepositTransaction depositTx =
                    TxDecoder.decodeToDeposit(payload.transactions().get(0));
            L1BlockInfo info = L1BlockInfo.from(Numeric.hexStringToByteArray(depositTx.getData()));
            l1Origin = new BlockId(info.blockHash(), info.number());
            sequenceNumber = info.sequenceNumber();
        }

        return new L2BlockRef(
                payload.blockHash(),
                payload.blockNumber(),
                payload.parentHash(),
                payload.timestamp(),
                new BlockId(l1Origin.hash(), l1Origin.number()),
                sequenceNumber);
    }

    /**
     * The type Unfinalized block.
     *
     * @param head             the head
     * @param epoch            the epoch
     * @param l1InclusionBlock the L1 inclusion block
     * @param seqNumber        the seq number
     */
    protected record UnfinalizedBlock(BlockInfo head, Epoch epoch, BigInteger l1InclusionBlock, BigInteger seqNumber) {}
}
