package io.optimism.l1;

import com.google.common.util.concurrent.AbstractExecutionThreadService;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.optimism.config.Config;
import io.optimism.config.Config.SystemConfig;
import io.optimism.derive.stages.Attributes;
import io.optimism.derive.stages.Attributes.UserDeposited;
import io.optimism.driver.L1AttributesDepositedTxNotFoundException;
import io.optimism.exceptions.BlockNotIncludedException;
import io.optimism.exceptions.DepositsNotFoundException;
import io.optimism.exceptions.HildrServiceExecutionException;
import io.optimism.rpc.Web3jProvider;
import io.optimism.telemetry.TracerTaskWrapper;
import io.optimism.types.BeaconSignedBlockHeader;
import io.optimism.types.BlobSidecar;
import io.optimism.types.BlockInfo;
import io.optimism.types.BlockUpdate;
import io.optimism.types.BlockUpdate.FinalityUpdate;
import io.optimism.types.L1Info;
import io.optimism.types.SystemConfigUpdate;
import io.optimism.types.enums.Logging;
import io.optimism.utilities.blob.BlobCodec;
import io.reactivex.disposables.Disposable;
import java.math.BigInteger;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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
import org.web3j.protocol.websocket.events.NewHead;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.tuples.generated.Tuple3;
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

    private static final String CONFIG_UPDATE_TOPIC = EventEncoder.encode(new Event(
            "ConfigUpdate",
            Arrays.asList(
                    new TypeReference<Uint256>() {}, new TypeReference<Uint8>() {}, new TypeReference<Bytes>() {})));

    private static final String TRANSACTION_DEPOSITED_TOPIC = EventEncoder.encode(new Event(
            "TransactionDeposited",
            Arrays.asList(
                    new TypeReference<Address>() {},
                    new TypeReference<Address>() {},
                    new TypeReference<Uint256>() {},
                    new TypeReference<Bytes>() {})));

    /**
     * Global Config.
     */
    private final Config config;

    /**
     * Ethers http provider for L1.
     */
    private final Web3j provider;

    private Web3j wsProvider;

    /**
     * Beacon blob fetcher to fetch the beacon blob from the L1 beacon endpoint.
     */
    private final BeaconBlobFetcher beaconFetcher;

    private final BigInteger l2StartBlock;

    /**
     * Channel to send block updates.
     */
    private final MessagePassingQueue<BlockUpdate> blockUpdateQueue;

    /**
     * Most recent ingested block.
     */
    private BigInteger currentBlock;

    /**
     * Most recent ingested block info.
     */
    private volatile BlockInfo currentBlockInfo;

    /**
     * Most recent block.
     */
    private BigInteger headBlock;

    /**
     * Most recent finalized block.
     */
    private BigInteger finalizedBlock;

    /**
     * Most recent finalized block info.
     */
    private volatile BlockInfo l1Finalized;

    private volatile BlockInfo l1Safe;

    private volatile BlockInfo l1Head;

    /**
     * List of blocks that have not been finalized yet.
     */
    private List<BlockInfo> unfinalizedBlocks;

    /**
     * Mapping from block number to user deposits. Past block deposits are removed as they are no
     * longer needed.
     */
    private final HashMap<BigInteger, List<Attributes.UserDeposited>> deposits;

    /**
     * Current system config value.
     */
    private Config.SystemConfig systemConfig;

    /**
     * Next system config if it exists and the L1 block number it activates BigInteger,
     * Config.SystemConfig.
     */
    private volatile Tuple2<BigInteger, Config.SystemConfig> systemConfigUpdate;

    private boolean isShutdownTriggered = false;

    private Disposable l1HeadListener;

    private final boolean devnet;

    /**
     * create a InnerWatcher instance.
     *
     * @param config       the global config
     * @param queue        the Queue to send block updates
     * @param l1StartBlock the start block number of l1
     * @param l2StartBlock the start block number of l2
     */
    public InnerWatcher(
            Config config, MessagePassingQueue<BlockUpdate> queue, BigInteger l1StartBlock, BigInteger l2StartBlock) {
        this.config = config;
        this.provider = Web3jProvider.createClient(config.l1RpcUrl());
        if (StringUtils.isNotEmpty(config.l1WsRpcUrl())) {
            this.wsProvider = Web3jProvider.createClient(config.l1WsRpcUrl());
        }
        this.beaconFetcher = new BeaconBlobFetcher(config.l1BeaconUrl(), config.l1BeaconArchiverUrl());
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
        EthBlock.Block l2Block;
        try {
            l2Block = this.pollBlockByNumber(l2Client, l2StartBlock.subtract(BigInteger.ONE));
            if (l2Block.getTransactions() == null || l2Block.getTransactions().isEmpty()) {
                throw new L1AttributesDepositedTxNotFoundException();
            }

            EthBlock.TransactionObject tx = (EthBlock.TransactionObject)
                    l2Block.getTransactions().getFirst().get();
            final byte[] input = Numeric.hexStringToByteArray(tx.getInput());
            var gasLimit = l2Block.getGasLimit();
            if (this.config.chainConfig().isEcotoneAndNotFirst(l2Block.getTimestamp())) {
                this.systemConfig = Config.SystemConfig.fromEcotoneTxInput(
                        config.chainConfig().systemConfig().unsafeBlockSigner(), gasLimit, input);
            } else {
                this.systemConfig = Config.SystemConfig.fromBedrockTxInput(
                        config.chainConfig().systemConfig().unsafeBlockSigner(), gasLimit, input);
            }
        } catch (InterruptedException | ExecutionException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new HildrServiceExecutionException(e);
        } finally {
            l2Client.shutdown();
        }
    }

    private void subscribeL1NewHeads() {
        if (this.wsProvider == null) {
            return;
        }
        this.l1HeadListener = this.wsProvider
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
    }

    /**
     * try ingest block.
     *
     * @throws ExecutionException   thrown if failed to get data from web3j client
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
            this.unfinalizedBlocks = this.unfinalizedBlocks.stream()
                    .filter(blockInfo -> blockInfo.number().compareTo(InnerWatcher.this.finalizedBlock) > 0)
                    .collect(Collectors.toList());
        }

        if (this.currentBlock.compareTo(this.headBlock) > 0 || this.headBlock.equals(BigInteger.ZERO)) {
            LOGGER.debug("will get head block: currentBlock({}) > headBlock({})", this.currentBlock, this.headBlock);
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
                    "will sleep 250 milliseconds: currentBlock({}) > headBlock({})", this.currentBlock, this.headBlock);
            try {
                Thread.sleep(Duration.ofMillis(250L));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new HildrServiceExecutionException(e);
            }
        }
    }

    private void updateSystemConfigWithNewestLog() throws ExecutionException, InterruptedException {
        EthBlock.Block blockResp = this.pollBlockByNumber(this.provider, this.currentBlock);
        BlockInfo blockInfo = BlockInfo.from(blockResp);
        this.updateSystemConfig(blockInfo);
        final L1Info l1Info = deriveL1Info(blockResp);
        if (l1Info.blockInfo().number().compareTo(this.finalizedBlock) >= 0) {
            this.unfinalizedBlocks.add(blockInfo);
            this.currentBlockInfo = blockInfo;
        }

        BlockUpdate update = this.checkReorg() ? new BlockUpdate.Reorg() : new BlockUpdate.NewBlock(l1Info);
        this.putBlockUpdate(update);
        LOGGER.debug("current block will add one: {}", this.currentBlock);
        this.currentBlock = this.currentBlock.add(BigInteger.ONE);
    }

    private L1Info deriveL1Info(EthBlock.Block l1Block) throws ExecutionException, InterruptedException {
        List<UserDeposited> userDeposits = this.getDeposits(this.currentBlock);
        boolean finalized = this.currentBlock.compareTo(this.finalizedBlock) <= 0;
        var tuple = getBatcherTxAndBlobHeader(l1Block);
        List<String> data = tuple.component1();
        var parentBeaconRoot = l1Block.getParentBeaconBlockRoot();
        return L1Info.create(l1Block, userDeposits, finalized, this.systemConfig, data, parentBeaconRoot);
    }

    private Tuple2<List<String>, BeaconSignedBlockHeader> getBatcherTxAndBlobHeader(EthBlock.Block l1Block) {
        final List<String> data = new ArrayList<>();
        List<Tuple3<Integer, BigInteger, String>> indexedBlobs = new ArrayList<>();
        int blobIndexRec = 0;
        for (EthBlock.TransactionResult<?> txRes : l1Block.getTransactions()) {
            EthBlock.TransactionObject tx = (EthBlock.TransactionObject) txRes.get();
            if (!L1Info.isValidBatcherTx(
                    tx, this.systemConfig.batchSender(), config.chainConfig().batchInbox())) {
                int skipBlobHashSize = tx.getBlobVersionedHashes() == null
                        ? 0
                        : tx.getBlobVersionedHashes().size();
                blobIndexRec += skipBlobHashSize;
                continue;
            }
            if (!tx.getType().equalsIgnoreCase("0x3") && !tx.getType().equalsIgnoreCase("0x03")) {
                data.add(tx.getInput());
                continue;
            }
            if (StringUtils.isNotEmpty(tx.getInput())) {
                LOGGER.warn("blob tx has calldata, which will be ignored: txhash = {}", tx.getHash());
            }
            if (CollectionUtils.isEmpty(tx.getBlobVersionedHashes())) {
                continue;
            }
            for (String blobVersionedHash : tx.getBlobVersionedHashes()) {
                data.add(null);
                indexedBlobs.add(new Tuple3<>(data.size() - 1, BigInteger.valueOf(blobIndexRec), blobVersionedHash));
                blobIndexRec += 1;
            }
        }
        if (indexedBlobs.isEmpty()) {
            return new Tuple2<>(data, null);
        }
        BigInteger slot = this.beaconFetcher.getSlotFromTime(l1Block.getTimestamp());
        List<BigInteger> indices = indexedBlobs.stream().map(Tuple3::component2).collect(Collectors.toList());
        List<BlobSidecar> blobsRes = this.beaconFetcher.getBlobSidecards(slot.toString(), indices);
        if (blobsRes == null || blobsRes.isEmpty()) {
            throw new DepositsNotFoundException(
                    "blobSidecards is empty, and excepted to be %d".formatted(indices.size()));
        }
        for (int i = 0; i < indexedBlobs.size(); i++) {
            Tuple3<Integer, BigInteger, String> indexedBlob = indexedBlobs.get(i);
            BlobSidecar blobRes = blobsRes.get(i);
            if (!BeaconBlobFetcher.verifyBlobSidecar(blobRes, indexedBlob.component3())) {
                throw new IllegalStateException("blob verification failed");
            }
        }
        int blobsResIndex = 0;
        for (int i = 0; i < data.size(); i++) {
            if (blobsResIndex >= blobsRes.size()) {
                throw new IndexOutOfBoundsException("blobIndex >= blobSidecards.size()");
            }
            byte[] decodedBlob = BlobCodec.decode(
                    Numeric.hexStringToByteArray(blobsRes.get(blobsResIndex).getBlob()));
            data.set(i, Numeric.toHexString(decodedBlob));
            blobsResIndex++;
        }
        if (blobsResIndex != blobsRes.size()) {
            throw new IllegalStateException("got too many blobs");
        }

        return new Tuple2<>(data, blobsRes.getFirst().getSignedBlockHeader());
    }

    private void putBlockUpdate(final BlockUpdate update) {
        while (true) {
            boolean isOffered = this.blockUpdateQueue.offer(update);
            if (isOffered) {
                break;
            }
        }
    }

    private void updateSystemConfig(BlockInfo l1BlockInfo) throws ExecutionException, InterruptedException {
        BigInteger preLastUpdateBlock = this.systemConfigUpdate.component1();
        if (preLastUpdateBlock.compareTo(this.currentBlock) < 0 || preLastUpdateBlock.equals(BigInteger.ZERO)) {
            BigInteger fromBlock = preLastUpdateBlock.equals(BigInteger.ZERO)
                    ? BigInteger.ZERO
                    : preLastUpdateBlock.add(BigInteger.ONE);
            if (fromBlock.compareTo(this.headBlock) > 0) {
                fromBlock = this.headBlock;
            }
            BigInteger toBlock = preLastUpdateBlock.add(BigInteger.valueOf(100L));
            if (toBlock.compareTo(this.headBlock) > 0) {
                toBlock = this.headBlock;
            }
            LOGGER.debug(
                    "will get system update eth log: fromBlock={} -> toBlock={}; contract={}",
                    fromBlock,
                    toBlock,
                    InnerWatcher.this.config.chainConfig().systemConfigContract());
            EthLog updates = this.getLog(
                    fromBlock,
                    toBlock,
                    InnerWatcher.this.config.chainConfig().systemConfigContract(),
                    CONFIG_UPDATE_TOPIC);

            if (updates.getLogs().isEmpty()) {
                this.systemConfigUpdate = new Tuple2<>(toBlock, null);
            } else {
                BigInteger updateBlockNum = ((LogObject) updates.getLogs().getFirst()).getBlockNumber();
                SystemConfig updatedConfig = this.systemConfig;
                boolean updated = false;
                for (int i = 0; i < updates.getLogs().size(); i++) {
                    LogObject update = (LogObject) updates.getLogs().get(i);
                    BigInteger updateBlock = update.getBlockNumber();
                    if (updateBlock == null) {
                        break;
                    }
                    if (!updateBlock.equals(updateBlockNum)) {
                        break;
                    }
                    SystemConfigUpdate configUpdate = SystemConfigUpdate.tryFrom(update);
                    updatedConfig = parseSystemConfigUpdate(updatedConfig, l1BlockInfo, configUpdate);
                    updated = true;
                }
                if (!updated) {
                    this.systemConfigUpdate = new Tuple2<>(toBlock, null);
                } else {
                    this.systemConfigUpdate = new Tuple2<>(updateBlockNum, updatedConfig);
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

    private Config.SystemConfig parseSystemConfigUpdate(
            SystemConfig lastSystemConfig, BlockInfo l1BlockInfo, SystemConfigUpdate configUpdate) {
        Config.SystemConfig updateSystemConfig = null;
        if (configUpdate instanceof SystemConfigUpdate.BatchSender) {
            updateSystemConfig = new Config.SystemConfig(
                    ((SystemConfigUpdate.BatchSender) configUpdate).getAddress(),
                    lastSystemConfig.gasLimit(),
                    lastSystemConfig.l1FeeOverhead(),
                    lastSystemConfig.l1FeeScalar(),
                    lastSystemConfig.unsafeBlockSigner());
        } else if (configUpdate instanceof SystemConfigUpdate.Fees) {
            if (this.config.chainConfig().isEcotone(l1BlockInfo.timestamp())) {
                updateSystemConfig = new Config.SystemConfig(
                        lastSystemConfig.batchSender(),
                        lastSystemConfig.gasLimit(),
                        BigInteger.ZERO,
                        ((SystemConfigUpdate.Fees) configUpdate).getFeeScalar(),
                        lastSystemConfig.unsafeBlockSigner());
            } else {
                updateSystemConfig = new Config.SystemConfig(
                        lastSystemConfig.batchSender(),
                        lastSystemConfig.gasLimit(),
                        ((SystemConfigUpdate.Fees) configUpdate).getFeeOverhead(),
                        ((SystemConfigUpdate.Fees) configUpdate).getFeeScalar(),
                        lastSystemConfig.unsafeBlockSigner());
            }
        } else if (configUpdate instanceof SystemConfigUpdate.GasLimit) {
            updateSystemConfig = new Config.SystemConfig(
                    lastSystemConfig.batchSender(),
                    ((SystemConfigUpdate.GasLimit) configUpdate).getGas(),
                    lastSystemConfig.l1FeeOverhead(),
                    lastSystemConfig.l1FeeScalar(),
                    lastSystemConfig.unsafeBlockSigner());
        } else if (configUpdate instanceof SystemConfigUpdate.UnsafeBlockSigner) {
            updateSystemConfig = new Config.SystemConfig(
                    lastSystemConfig.batchSender(),
                    lastSystemConfig.gasLimit(),
                    lastSystemConfig.l1FeeOverhead(),
                    lastSystemConfig.l1FeeScalar(),
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
        var parameter = this.devnet ? DefaultBlockParameterName.LATEST : DefaultBlockParameterName.SAFE;
        return this.pollBlock(this.provider, parameter, false);
    }

    private EthBlock.Block getFinalized() throws ExecutionException, InterruptedException {
        var parameter = this.devnet ? DefaultBlockParameterName.LATEST : DefaultBlockParameterName.FINALIZED;
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
        LOGGER.debug("will poll block: {}", parameter.getValue());
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            var fork = scope.fork(TracerTaskWrapper.wrap(() ->
                    client.ethGetBlockByNumber(parameter, fullTxObjectFlag).send()));
            scope.join();
            scope.throwIfFailed();
            var block = fork.get().getBlock();
            if (block == null) {
                throw new BlockNotIncludedException();
            }
            if (block.getNumber() == null) {
                throw new BlockNotIncludedException();
            }
            return block;
        }
    }

    private EthLog getLog(BigInteger fromBlock, BigInteger toBlock, String contract, String topic)
            throws ExecutionException, InterruptedException {
        final EthFilter ethFilter = new EthFilter(
                        DefaultBlockParameter.valueOf(fromBlock), DefaultBlockParameter.valueOf(toBlock), contract)
                .addSingleTopic(topic);
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            var fork = scope.fork(TracerTaskWrapper.wrap(
                    () -> this.provider.ethGetLogs(ethFilter).send()));
            scope.join();
            scope.throwIfFailed();
            return fork.get();
        }
    }

    private List<Attributes.UserDeposited> getDeposits(BigInteger blockNum)
            throws ExecutionException, InterruptedException {
        final List<Attributes.UserDeposited> removed = this.deposits.remove(blockNum);
        if (removed != null) {
            return removed;
        }
        final BigInteger endBlock = this.headBlock.min(blockNum.add(BigInteger.valueOf(1000L)));
        LOGGER.debug(
                "will get deposit eth logs: fromBlock={} -> toBlock={};contract={}",
                blockNum,
                endBlock,
                this.config.chainConfig().depositContract());

        EthLog result = this.getLog(
                blockNum, endBlock, this.config.chainConfig().depositContract(), TRANSACTION_DEPOSITED_TOPIC);

        result.getLogs().forEach(log -> {
            if (log instanceof LogObject) {
                var userDeposited = UserDeposited.fromLog((LogObject) log);
                final var num = userDeposited.l1BlockNum();
                var userDepositeds = InnerWatcher.this.deposits.computeIfAbsent(num, k -> new ArrayList<>());
                userDepositeds.add(userDeposited);
            } else {
                throw new IllegalStateException("Unexpected result type: %s required LogObject".formatted(log.get()));
            }
        });
        var max = (int) endBlock.subtract(blockNum).add(BigInteger.ONE).longValue();
        for (int i = 0; i < max; i++) {
            InnerWatcher.this.deposits.putIfAbsent(blockNum.add(BigInteger.valueOf(i)), new ArrayList<>());
        }
        var remv = InnerWatcher.this.deposits.remove(blockNum);
        if (remv == null) {
            throw new DepositsNotFoundException();
        }
        return remv;
    }

    @Override
    protected void startUp() {
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
            } catch (Exception e) {
                LOGGER.error(String.format("unexcepted error while fetching L1 data for block %d", currentBlock), e);
                throw new HildrServiceExecutionException(e);
            } finally {
                span.end();
            }
        }
    }

    @Override
    protected void shutDown() {
        this.provider.shutdown();
        if (!this.l1HeadListener.isDisposed()) {
            this.l1HeadListener.dispose();
        }
        if (this.wsProvider != null) {
            this.wsProvider.shutdown();
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
