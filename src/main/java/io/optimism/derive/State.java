package io.optimism.derive;

import io.optimism.config.Config;
import io.optimism.driver.HeadInfo;
import io.optimism.driver.L1AttributesDepositedTxNotFoundException;
import io.optimism.types.BlockInfo;
import io.optimism.types.Epoch;
import io.optimism.types.L1Info;
import java.math.BigInteger;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;
import java.util.function.BiFunction;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.tuples.generated.Tuple2;

/**
 * The type State.
 *
 * @author grapebaba
 * @since 0.1.0
 */
public class State {

    private static final Logger LOGGER = LoggerFactory.getLogger(State.class);

    private final TreeMap<String, L1Info> l1Info;

    private final TreeMap<BigInteger, String> l1Hashes;

    private final TreeMap<BigInteger, Tuple2<BlockInfo, Epoch>> l2Refs;

    private final BiFunction<DefaultBlockParameter, Boolean, Tuple2<BlockInfo, Epoch>> l2Fetcher;

    private BlockInfo safeHead;

    private Epoch safeEpoch;

    private BigInteger currentEpochNum;

    private final Config config;

    /**
     * Instantiates a new State.
     *
     * @param l1Info the L1 info
     * @param l1Hashes the L1 hashes
     * @param l2Refs the L2 block info references
     * @param l2Fetcher the L2 block info fetcher
     * @param safeHead the safe head
     * @param safeEpoch the safe epoch
     * @param currentEpochNum the current epoch num
     * @param config the config
     */
    public State(
            TreeMap<String, L1Info> l1Info,
            TreeMap<BigInteger, String> l1Hashes,
            TreeMap<BigInteger, Tuple2<BlockInfo, Epoch>> l2Refs,
            BiFunction<DefaultBlockParameter, Boolean, Tuple2<BlockInfo, Epoch>> l2Fetcher,
            BlockInfo safeHead,
            Epoch safeEpoch,
            BigInteger currentEpochNum,
            Config config) {
        this.l1Info = l1Info;
        this.l1Hashes = l1Hashes;
        this.l2Refs = l2Refs;
        this.l2Fetcher = l2Fetcher;
        this.safeHead = safeHead;
        this.safeEpoch = safeEpoch;
        this.currentEpochNum = currentEpochNum;
        this.config = config;
    }

    /**
     * Create state.
     *
     * @param l2Refs the L2 block info references
     * @param l2Fetcher the L2 block info fetcher
     * @param finalizedHead the finalized head
     * @param finalizedEpoch the finalized epoch
     * @param config the config
     * @return the state
     */
    public static State create(
            TreeMap<BigInteger, Tuple2<BlockInfo, Epoch>> l2Refs,
            BiFunction<DefaultBlockParameter, Boolean, Tuple2<BlockInfo, Epoch>> l2Fetcher,
            BlockInfo finalizedHead,
            Epoch finalizedEpoch,
            Config config) {
        return new State(
                new TreeMap<>(),
                new TreeMap<>(),
                l2Refs,
                l2Fetcher,
                finalizedHead,
                finalizedEpoch,
                BigInteger.ZERO,
                config);
    }

    /**
     * L1 info l1 info.
     *
     * @param hash the hash
     * @return the l 1 info
     */
    public L1Info l1Info(String hash) {
        return l1Info.get(hash);
    }

    /**
     * L1 info l1 info.
     *
     * @param number the number
     * @return the l 1 info
     */
    public L1Info l1Info(BigInteger number) {
        String hash = l1Hashes.get(number);
        if (StringUtils.isEmpty(hash)) {
            return null;
        }
        return l1Info.get(l1Hashes.get(number));
    }

    /**
     * Gets L2 block info and epoch by block timestamp.
     *
     * @param timestamp the number
     * @return the tuple of L2 block info and epoch
     */
    public Tuple2<BlockInfo, Epoch> l2Info(BigInteger timestamp) {
        final BigInteger blockNum = timestamp
                .subtract(config.chainConfig().l2Genesis().timestamp())
                .divide(config.chainConfig().blockTime())
                .add(config.chainConfig().l2Genesis().number());
        var cache = l2Refs.get(blockNum);
        if (cache != null) {
            return cache;
        }
        LOGGER.warn("L2 refs cache not contains, will fetch from geth: blockNum = {}", blockNum);
        var res = l2Fetcher.apply(DefaultBlockParameter.valueOf(blockNum), true);
        this.l2Refs.put(res.component1().number(), res);
        return res;
    }

    /**
     * Epoch epoch.
     *
     * @param hash the hash
     * @return the epoch
     */
    public Epoch epoch(String hash) {
        L1Info l1Info = l1Info(hash);
        return new Epoch(
                l1Info.blockInfo().number(),
                l1Info.blockInfo().hash(),
                l1Info.blockInfo().timestamp(),
                null);
    }

    /**
     * Epoch epoch.
     *
     * @param number the number
     * @return the epoch
     */
    public Epoch epoch(BigInteger number) {
        L1Info l1Info = l1Info(number);
        if (l1Info == null) {
            return null;
        }
        return new Epoch(
                l1Info.blockInfo().number(),
                l1Info.blockInfo().hash(),
                l1Info.blockInfo().timestamp(),
                null);
    }

    /**
     * Update l 1 info.
     *
     * @param l1Info the l 1 info
     */
    public void updateL1Info(L1Info l1Info) {
        this.currentEpochNum = l1Info.blockInfo().number();
        this.l1Hashes.put(l1Info.blockInfo().number(), l1Info.blockInfo().hash());
        this.l1Info.put(l1Info.blockInfo().hash(), l1Info);

        this.prune();
    }

    /**
     * Purge.
     *
     * @param safeHead the safe head
     * @param safeEpoch the safe epoch
     */
    public void purge(BlockInfo safeHead, Epoch safeEpoch) {
        LOGGER.info("purge state: safeHead.number={}, safeEpoch. ={}", safeHead.number(), safeEpoch.hash());
        this.safeHead = safeHead;
        this.safeEpoch = safeEpoch;
        this.l1Info.clear();
        this.l1Hashes.clear();
        this.currentEpochNum = BigInteger.ZERO;
        this.updateSafeHead(safeHead, safeEpoch);
    }

    /**
     * Update safe head.
     *
     * @param safeHead the safe head
     * @param safeEpoch the safe epoch
     */
    public void updateSafeHead(BlockInfo safeHead, Epoch safeEpoch) {
        this.safeHead = safeHead;
        this.safeEpoch = safeEpoch;
        this.l2Refs.put(safeHead.number(), new Tuple2<>(safeHead, safeEpoch));
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
     * Sets safe head.
     *
     * @param safeHead the safe head
     */
    public void setSafeHead(BlockInfo safeHead) {
        this.safeHead = safeHead;
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
     * Sets safe epoch.
     *
     * @param safeEpoch the safe epoch
     */
    public void setSafeEpoch(Epoch safeEpoch) {
        this.safeEpoch = safeEpoch;
    }

    /**
     * Gets current epoch num.
     *
     * @return the current epoch num
     */
    public BigInteger getCurrentEpochNum() {
        return currentEpochNum;
    }

    /**
     * Sets current epoch num.
     *
     * @param currentEpochNum the current epoch num
     */
    public void setCurrentEpochNum(BigInteger currentEpochNum) {
        this.currentEpochNum = currentEpochNum;
    }

    private void prune() {
        BigInteger pruneUntil =
                this.safeEpoch.number().subtract(config.chainConfig().seqWindowSize());
        pruneUntil = pruneUntil.compareTo(BigInteger.ZERO) < 0 ? BigInteger.ZERO : pruneUntil;
        Entry<BigInteger, String> blockNumAndHash;
        while ((blockNumAndHash = this.l1Hashes.firstEntry()) != null) {
            if (blockNumAndHash.getKey().compareTo(pruneUntil) >= 0) {
                break;
            }
            this.l1Info.remove(blockNumAndHash.getValue());
            this.l1Hashes.pollFirstEntry();
        }

        BigInteger maxSeqDrift = this.config.chainConfig().maxSequencerDrift(this.safeEpoch.timestamp());
        pruneUntil = this.safeHead
                .number()
                .subtract(maxSeqDrift.divide(this.config.chainConfig().blockTime()));

        Entry<BigInteger, Tuple2<BlockInfo, Epoch>> blockRefEntry;
        while ((blockRefEntry = this.l2Refs.firstEntry()) != null) {
            if (blockRefEntry.getKey().compareTo(pruneUntil) >= 0) {
                break;
            }
            this.l2Refs.pollFirstEntry();
        }
    }

    /**
     * Init L2 refs tree map.
     *
     * @param headNum the l2 head block number
     * @param chainConfig the chain config
     * @param l2Client the l2 web3j client
     * @param l1OriginTime the l1 origin time
     * @return the L2 refs tree map.
     * @throws ExecutionException throws the ExecutionException when the Task has been failed
     * @throws InterruptedException throws the InterruptedException when the thread has been interrupted
     */
    public static TreeMap<BigInteger, Tuple2<BlockInfo, Epoch>> initL2Refs(
            BigInteger headNum, BigInteger l1OriginTime, Config.ChainConfig chainConfig, Web3j l2Client)
            throws ExecutionException, InterruptedException {
        final BigInteger lookback = chainConfig.maxSequencerDrift(l1OriginTime).divide(chainConfig.blockTime());
        BigInteger start;
        if (headNum.compareTo(lookback) < 0) {
            start = chainConfig.l2Genesis().number();
        } else {
            start = headNum.subtract(lookback).max(chainConfig.l2Genesis().number());
        }
        final TreeMap<BigInteger, Tuple2<BlockInfo, Epoch>> l2Refs = new TreeMap<>();
        for (BigInteger i = start; i.compareTo(headNum) <= 0; i = i.add(BigInteger.ONE)) {
            try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
                var l2Num = i;
                var blockTask =
                        scope.fork(() -> l2Client.ethGetBlockByNumber(DefaultBlockParameter.valueOf(l2Num), true)
                                .send()
                                .getBlock());
                scope.join();
                scope.throwIfFailed();
                EthBlock.Block block = blockTask.get();
                if (block == null) {
                    continue;
                }
                try {
                    final HeadInfo l2BlockInfo = HeadInfo.from(block);
                    l2Refs.put(
                            l2BlockInfo.l2BlockInfo().number(),
                            new Tuple2<>(l2BlockInfo.l2BlockInfo(), l2BlockInfo.l1Epoch()));
                } catch (L1AttributesDepositedTxNotFoundException ignore) {
                    LOGGER.debug("Can't found deposited transaction (at blockNum = %d)".formatted(i));
                }
            }
        }
        return l2Refs;
    }
}
