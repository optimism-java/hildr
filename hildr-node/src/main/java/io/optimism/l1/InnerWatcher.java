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

package io.optimism.l1;

import com.google.common.util.concurrent.AbstractExecutionThreadService;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.optimism.common.BlockInfo;
import io.optimism.common.BlockNotIncludedException;
import io.optimism.common.HildrServiceExecutionException;
import io.optimism.config.Config;
import io.optimism.config.Config.SystemConfig;
import io.optimism.derive.stages.Attributes;
import io.optimism.derive.stages.Attributes.UserDeposited;
import io.optimism.driver.L1AttributesDepositedTxNotFoundException;
import io.optimism.l1.BlockUpdate.FinalityUpdate;
import io.optimism.utilities.rpc.Web3jProvider;
import io.optimism.utilities.telemetry.Logging;
import io.reactivex.disposables.Disposable;
import java.math.BigInteger;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.jctools.queues.MessagePassingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bytes;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.EthLog.LogObject;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.protocol.websocket.events.NewHead;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.utils.Numeric;

/**
 * the InnerWatcher class.
 *
 * @author thinkAfCod
 * @since 0.1.0
 */
@SuppressWarnings("WaitNotInLoop")
public class InnerWatcher extends AbstractExecutionThreadService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InnerWatcher.class);

    private static final String CONFIG_UPDATE_TOPIC =
        EventEncoder.encode(
            new Event(
                "ConfigUpdate",
                Arrays.asList(
                    new TypeReference<Uint256>() {},
                    new TypeReference<Uint8>() {},
                    new TypeReference<Bytes>() {})));

    private static final String TRANSACTION_DEPOSITED_TOPIC =
        EventEncoder.encode(
            new Event(
                "TransactionDeposited",
                Arrays.asList(
                    new TypeReference<Address>() {},
                    new TypeReference<Address>() {},
                    new TypeReference<Uint256>() {},
                    new TypeReference<Bytes>() {})));

    private final ExecutorService executor;

    /** Global Config. */
    private final Config config;

    /** Ethers http provider for L1. */
    private final Web3j provider;

    private final Web3j wsProvider;

    private BigInteger l1StartBlock;

    private BigInteger l2StartBlock;

    /** Channel to send block updates. */
    private final MessagePassingQueue<BlockUpdate> blockUpdateQueue;

    /** Most recent ingested block. */
    private BigInteger currentBlock;

    /** Most recent ingested block info. */
    private volatile BlockInfo currentBlockInfo;

    /** Most recent block. */
    private BigInteger headBlock;

    /** Most recent finalized block. */
    private BigInteger finalizedBlock;

    /** Most recent finalized block info. */
    private volatile BlockInfo l1Finalized;

    private volatile BlockInfo l1Safe;

    private volatile BlockInfo l1Head;

    /** List of blocks that have not been finalized yet. */
    private List<BlockInfo> unfinalizedBlocks;

    /**
     * Mapping from block number to user deposits. Past block deposits are removed as they are no
     * longer needed.
     */
    private final HashMap<BigInteger, List<Attributes.UserDeposited>> deposits;

    /** Current system config value. */
    private Config.SystemConfig systemConfig;

    /**
     * Next system config if it exists and the L1 block number it activates BigInteger,
     * Config.SystemConfig.
     */
    private volatile Tuple2<BigInteger, Config.SystemConfig> systemConfigUpdate;

    private boolean isShutdownTriggered = false;

    private Disposable l1HeadListener;

    private boolean devnet = false;

    /**
     * create a InnerWatcher instance.
     *
     * @param config the global config
     * @param queue the Queue to send block updates
     * @param l1StartBlock the start block number of l1
     * @param l2StartBlock the start block number of l2
     * @param executor the executor for async request
     */
    public InnerWatcher(
        Config config,
        MessagePassingQueue<BlockUpdate> queue,
        BigInteger l1StartBlock,
        BigInteger l2StartBlock,
        ExecutorService executor) {
        this.executor = executor;
        this.config = config;
        this.provider = Web3jProvider.createClient(config.l1RpcUrl());
        this.wsProvider = Web3jProvider.createClient(config.l1WsRpcUrl());
        this.l1StartBlock = l1StartBlock;
        this.l2StartBlock = l2StartBlock;
        this.devnet = config.devnet() != null && config.devnet();

        this.blockUpdateQueue = queue;
        this.currentBlock = l1StartBlock;
        this.headBlock = BigInteger.ZERO;
        this.finalizedBlock = BigInteger.ZERO;
        this.unfinalizedBlocks = new ArrayList<>();
        this.deposits = new HashMap<>();
        this.systemConfigUpdate = new Tuple2<>(l1StartBlock, null);
    }

    private void getMetadataFromL2(BigInteger l2StartBlock) {
        Web3j l2Client = Web3jProvider.createClient(config.l2RpcUrl());
        EthBlock.Block block;
        try {
            block = this.pollBlockByNumber(l2Client, l2StartBlock.subtract(BigInteger.ONE));
        } catch (InterruptedException | ExecutionException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            l2Client.shutdown();
            throw new HildrServiceExecutionException(e);
        }
        if (block.getTransactions().isEmpty()) {
            throw new L1AttributesDepositedTxNotFoundException();
        }
        EthBlock.TransactionObject tx =
            (EthBlock.TransactionObject) block.getTransactions().get(0).get();
        final byte[] input = Numeric.hexStringToByteArray(tx.getInput());

        final String batchSender = Numeric.toHexString(Arrays.copyOfRange(input, 176, 196));
        var l1FeeOverhead = Numeric.toBigInt(Arrays.copyOfRange(input, 196, 228));
        var l1FeeScalar = Numeric.toBigInt(Arrays.copyOfRange(input, 228, 260));
        var gasLimit = block.getGasLimit();
        this.systemConfig =
            new Config.SystemConfig(
                batchSender,
                gasLimit,
                l1FeeOverhead,
                l1FeeScalar,
                config.chainConfig().systemConfig().unsafeBlockSigner());
        l2Client.shutdown();
    }

    private Disposable subscribeL1NewHeads() {
        this.l1HeadListener =
            this.wsProvider
                .newHeadsNotifications()
                .subscribe(
                    notification -> {
                        NewHead header = notification.getParams().getResult();
                        String hash = header.getHash();
                        BigInteger number = Numeric.toBigInt(header.getNumber());
                        String parentHash = header.getParentHash();
                        BigInteger time = Numeric.toBigInt(header.getTimestamp());
                        l1Head = new BlockInfo(hash, number, parentHash, time);
                    },
                    t -> {
                        if (t instanceof WebsocketNotConnectedException) {
                            this.subscribeL1NewHeads();
                        }
                    });
        return this.l1HeadListener;
    }

    /**
     * try ingest block.
     *
     * @throws ExecutionException thrown if failed to get data from web3j client
     * @throws InterruptedException thrown if executor has been shutdown
     */
    public void tryIngestBlock() throws ExecutionException, InterruptedException {
        final EthBlock.Block l1SafeBlock = this.getSafe();
        this.l1Safe = BlockInfo.from(l1SafeBlock);

        if (this.currentBlock.compareTo(this.finalizedBlock) > 0) {
            LOGGER.debug(
                "will get finalized block: currentBlock({}) > finalizedBlock({})",
                this.currentBlock,
                this.finalizedBlock);
            var finalizedBlockDetail = this.getFinalized();
            this.finalizedBlock = finalizedBlockDetail.getNumber();
            this.l1Finalized = BlockInfo.from(finalizedBlockDetail);
            this.putBlockUpdate(new FinalityUpdate(finalizedBlock));
            this.unfinalizedBlocks =
                this.unfinalizedBlocks.stream()
                    .filter(
                        blockInfo -> blockInfo.number().compareTo(InnerWatcher.this.finalizedBlock) > 0)
                    .collect(Collectors.toList());
        }

        if (this.currentBlock.compareTo(this.headBlock) > 0) {
            LOGGER.debug(
                "will get head block: currentBlock({}) > headBlock({})",
                this.currentBlock,
                this.headBlock);
            this.headBlock = this.getHead().getNumber();
        }

        if (this.currentBlock.compareTo(this.headBlock) <= 0) {
            LOGGER.debug(
                "will update system config with newest log: currentBlock({}) <= headBlock({})",
                this.currentBlock,
                this.headBlock);
            updateSystemConfigWithNewestLog();
        } else {
            LOGGER.debug(
                "will sleep 250 milliseconds: currentBlock({}) > headBlock({})",
                this.currentBlock,
                this.headBlock);
            try {
                Thread.sleep(Duration.ofMillis(250L));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new HildrServiceExecutionException(e);
            }
        }
    }

    private void updateSystemConfigWithNewestLog() throws ExecutionException, InterruptedException {
        this.updateSystemConfig();
        EthBlock.Block blockResp = this.pollBlockByNumber(this.provider, this.currentBlock);
        List<UserDeposited> userDeposits = this.getDeposits(this.currentBlock);

        boolean finalized = this.currentBlock.compareTo(this.finalizedBlock) >= 0;
        L1Info l1Info =
            L1Info.create(
                blockResp,
                userDeposits,
                config.chainConfig().batchInbox(),
                finalized,
                this.systemConfig);

        if (l1Info.blockInfo().number().compareTo(this.finalizedBlock) >= 0) {
            BlockInfo blockInfo = BlockInfo.from(blockResp);
            this.unfinalizedBlocks.add(blockInfo);
            this.currentBlockInfo = blockInfo;
        }

        BlockUpdate update =
            this.checkReorg() ? new BlockUpdate.Reorg() : new BlockUpdate.NewBlock(l1Info);
        this.putBlockUpdate(update);
        LOGGER.debug("current block will add one: {}", this.currentBlock);
        this.currentBlock = this.currentBlock.add(BigInteger.ONE);
    }

    private void putBlockUpdate(final BlockUpdate update) {
        while (true) {
            boolean isOffered = this.blockUpdateQueue.offer(update);
            if (isOffered) {
                break;
            }
        }
    }

    private void updateSystemConfig() throws ExecutionException, InterruptedException {
        BigInteger preLastUpdateBlock = this.systemConfigUpdate.component1();
        if (preLastUpdateBlock.compareTo(this.currentBlock) < 0) {
            BigInteger toBlock = preLastUpdateBlock.add(BigInteger.valueOf(1000L));

            EthLog updates =
                this.getLog(
                    preLastUpdateBlock.add(BigInteger.ONE),
                    toBlock,
                    InnerWatcher.this.config.chainConfig().systemConfigContract(),
                    CONFIG_UPDATE_TOPIC);

            if (updates.getLogs().isEmpty()) {
                this.systemConfigUpdate = new Tuple2<>(toBlock, null);
            } else {
                LogResult<?> update = updates.getLogs().iterator().next();
                BigInteger updateBlock = ((LogObject) update).getBlockNumber();
                SystemConfigUpdate configUpdate = SystemConfigUpdate.tryFrom((LogObject) update);
                if (updateBlock == null) {
                    this.systemConfigUpdate = new Tuple2<>(toBlock, null);
                } else {
                    SystemConfig updateSystemConfig = parseSystemConfigUpdate(configUpdate);
                    this.systemConfigUpdate = new Tuple2<>(updateBlock, updateSystemConfig);
                }
            }
        }
        BigInteger lastUpdateBlock = this.systemConfigUpdate.component1();
        SystemConfig nextConfig = this.systemConfigUpdate.component2();
        if (lastUpdateBlock.compareTo(currentBlock) == 0 && nextConfig != null) {
            LOGGER.info("system config updated");
            LOGGER.debug("{}", nextConfig);
            this.systemConfig = nextConfig;
        }
    }

    private Config.SystemConfig parseSystemConfigUpdate(SystemConfigUpdate configUpdate) {
        Config.SystemConfig updateSystemConfig = null;
        if (configUpdate instanceof SystemConfigUpdate.BatchSender) {
            updateSystemConfig =
                new Config.SystemConfig(
                    ((SystemConfigUpdate.BatchSender) configUpdate).getAddress(),
                    this.systemConfig.gasLimit(),
                    this.systemConfig.l1FeeOverhead(),
                    this.systemConfig.l1FeeScalar(),
                    this.systemConfig.unsafeBlockSigner());
        } else if (configUpdate instanceof SystemConfigUpdate.Fees) {
            updateSystemConfig =
                new Config.SystemConfig(
                    this.systemConfig.batchSender(),
                    this.systemConfig.gasLimit(),
                    ((SystemConfigUpdate.Fees) configUpdate).getFeeOverhead(),
                    ((SystemConfigUpdate.Fees) configUpdate).getFeeScalar(),
                    this.systemConfig.unsafeBlockSigner());
        } else if (configUpdate instanceof SystemConfigUpdate.Gas) {
            updateSystemConfig =
                new Config.SystemConfig(
                    this.systemConfig.batchSender(),
                    ((SystemConfigUpdate.Gas) configUpdate).getGas(),
                    this.systemConfig.l1FeeOverhead(),
                    this.systemConfig.l1FeeScalar(),
                    this.systemConfig.unsafeBlockSigner());
        } else if (configUpdate instanceof SystemConfigUpdate.UnsafeBlockSigner) {
            updateSystemConfig =
                new Config.SystemConfig(
                    this.systemConfig.batchSender(),
                    this.systemConfig.gasLimit(),
                    this.systemConfig.l1FeeOverhead(),
                    this.systemConfig.l1FeeScalar(),
                    ((SystemConfigUpdate.UnsafeBlockSigner) configUpdate).getAddress());
        }
        return updateSystemConfig;
    }

    private boolean checkReorg() {
        int size = this.unfinalizedBlocks.size();
        if (size >= 2) {
            BlockInfo last = this.unfinalizedBlocks.get(size - 1);
            BlockInfo parent = this.unfinalizedBlocks.get(size - 2);
            return !last.parentHash().equalsIgnoreCase(parent.hash());
        }
        return false;
    }

    private EthBlock.Block getSafe() throws ExecutionException, InterruptedException {
        return this.pollBlock(this.provider, DefaultBlockParameterName.SAFE, false);
    }

    private EthBlock.Block getFinalized() throws ExecutionException, InterruptedException {
        var parameter =
            this.devnet ? DefaultBlockParameterName.LATEST : DefaultBlockParameterName.FINALIZED;
        return this.pollBlock(this.provider, parameter, false);
    }

    private EthBlock.Block getHead() throws ExecutionException, InterruptedException {
        return this.pollBlock(this.provider, DefaultBlockParameterName.LATEST, false);
    }

    private EthBlock.Block pollBlockByNumber(final Web3j client, final BigInteger blockNum)
        throws ExecutionException, InterruptedException {
        return pollBlock(client, DefaultBlockParameter.valueOf(blockNum), true);
    }

    private EthBlock.Block pollBlock(
        final Web3j client, final DefaultBlockParameter parameter, final boolean fullTxObjectFlag)
        throws ExecutionException, InterruptedException {
        EthBlock.Block block =
            this.executor
                .submit(() -> client.ethGetBlockByNumber(parameter, fullTxObjectFlag).send())
                .get()
                .getBlock();
        if (block == null) {
            throw new BlockNotIncludedException();
        }
        if (block.getNumber() == null) {
            throw new BlockNotIncludedException();
        }
        return block;
    }

    private EthLog getLog(BigInteger fromBlock, BigInteger toBlock, String contract, String topic)
        throws ExecutionException, InterruptedException {
        final EthFilter ethFilter =
            new EthFilter(
                DefaultBlockParameter.valueOf(fromBlock),
                DefaultBlockParameter.valueOf(toBlock),
                contract)
                .addSingleTopic(topic);

        return this.executor.submit(() -> this.provider.ethGetLogs(ethFilter).send()).get();
    }

    private List<Attributes.UserDeposited> getDeposits(BigInteger blockNum)
        throws ExecutionException, InterruptedException {
        final List<Attributes.UserDeposited> removed = this.deposits.remove(blockNum);
        if (removed != null) {
            return removed;
        }
        final BigInteger endBlock = this.headBlock.min(blockNum.add(BigInteger.valueOf(1000L)));

        EthLog result =
            this.getLog(
                blockNum,
                endBlock,
                this.config.chainConfig().depositContract(),
                TRANSACTION_DEPOSITED_TOPIC);

        result
            .getLogs()
            .forEach(
                log -> {
                    if (log instanceof LogObject) {
                        var userDeposited = UserDeposited.fromLog((LogObject) log);
                        final var num = userDeposited.l1BlockNum();
                        var userDepositeds =
                            InnerWatcher.this.deposits.computeIfAbsent(num, k -> new ArrayList<>());
                        userDepositeds.add(userDeposited);
                    } else {
                        throw new IllegalStateException(
                            "Unexpected result type: " + log.get() + " required LogObject");
                    }
                });
        var max = (int) endBlock.subtract(blockNum).longValue();
        for (int i = 0; i < max; i++) {
            InnerWatcher.this.deposits.putIfAbsent(
                blockNum.add(BigInteger.valueOf(i)), new ArrayList<>());
        }
        var remv = InnerWatcher.this.deposits.remove(blockNum);
        if (remv == null) {
            throw new DepositsNotFoundException();
        }
        return remv;
    }

    @Override
    protected void startUp() throws Exception {
        if (this.l2StartBlock.equals(config.chainConfig().l2Genesis().number())) {
            this.systemConfig = config.chainConfig().systemConfig();
        } else {
            this.getMetadataFromL2(this.l2StartBlock);
        }
        this.subscribeL1NewHeads();
    }

    @Override
    protected void run() {
        while (isRunning() && !this.isShutdownTriggered) {
            Tracer tracer = Logging.INSTANCE.getTracer("structure-task-scope");
            Span span = tracer.nextSpan().name("call").start();
            try (var unused = tracer.withSpan(span)) {
                LOGGER.debug("fetching L1 data for block {}", currentBlock);
                this.tryIngestBlock();
                LOGGER.debug("done fetching L1 data for block {}", currentBlock);
            } catch (ExecutionException e) {
                LOGGER.error(String.format("error while fetching L1 data for block %d", currentBlock), e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new HildrServiceExecutionException(e);
            } finally {
                span.end();
            }
        }
    }

    @Override
    protected Executor executor() {
        return this.executor;
    }

    @Override
    protected void shutDown() {
        this.executor.shutdown();
        this.provider.shutdown();
        if (!this.l1HeadListener.isDisposed()) {
            this.l1HeadListener.dispose();
        }
    }

    @Override
    protected void triggerShutdown() {
        this.isShutdownTriggered = true;
    }

    /**
     * Gets Current L1 block.
     *
     * @return Current L1 BlockInfo instance
     */
    public BlockInfo getCurrentL1() {
        return this.currentBlockInfo;
    }

    /**
     * Gets Current L1 finalized block.
     *
     * @return Current L1 finalized BlockInfo instance
     */
    public BlockInfo getCurrentL1Finalized() {
        return this.l1Finalized;
    }

    /**
     * Gets L1 head block.
     *
     * @return L1 head BlockInfo instance
     */
    public BlockInfo getL1HeadBlock() {
        return this.l1Head;
    }

    /**
     * Gets L1 safe block.
     *
     * @return L1 safe BlockInfo instance
     */
    public BlockInfo getL1SafeBlock() {
        return this.l1Safe;
    }

    /**
     * Gets L1 finalized block.
     *
     * @return L1 finalized BlockInfo instance
     */
    public BlockInfo getL1FinalizedBlock() {
        return this.l1Finalized;
    }

    /**
     * Get system config info.
     *
     * @return SystemConfig instance
     */
    public SystemConfig getSystemConfig() {
        return this.systemConfig;
    }
}
