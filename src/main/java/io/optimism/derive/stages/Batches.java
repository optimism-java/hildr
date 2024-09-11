package io.optimism.derive.stages;

import com.google.common.collect.Lists;
import io.optimism.config.Config;
import io.optimism.derive.PurgeableIterator;
import io.optimism.derive.State;
import io.optimism.derive.stages.Channels.Channel;
import io.optimism.exceptions.DecompressException;
import io.optimism.types.Batch;
import io.optimism.types.BlockInfo;
import io.optimism.types.Epoch;
import io.optimism.types.IBatch;
import io.optimism.types.L1Info;
import io.optimism.types.SingularBatch;
import io.optimism.types.SpanBatch;
import io.optimism.types.SpanBatchElement;
import io.optimism.types.enums.BatchType;
import io.optimism.utilities.compression.Compressors;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.tuweni.bytes.Bytes;
import org.jctools.queues.atomic.SpscAtomicArrayQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.rlp.RlpDecoder;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpString;
import org.web3j.rlp.RlpType;
import org.web3j.tuples.generated.Tuple2;

/**
 * The type Batches.
 *
 * @param <I> the type parameter
 * @author grapebaba
 * @since 0.1.0
 */
public class Batches<I extends PurgeableIterator<Channel>> implements PurgeableIterator<Batch> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Batches.class);
    private final TreeMap<BigInteger, Batch> batches;

    private final I channelIterator;

    private final AtomicReference<State> state;

    private final Config config;

    private final SpscAtomicArrayQueue<Batch> nextSingularBatches;

    /**
     * Instantiates a new Batches.
     *
     * @param batches the batches
     * @param channelIterator the channel iterator
     * @param state the state
     * @param config the config
     */
    public Batches(TreeMap<BigInteger, Batch> batches, I channelIterator, AtomicReference<State> state, Config config) {
        this.batches = batches;
        this.channelIterator = channelIterator;
        this.state = state;
        this.config = config;
        this.nextSingularBatches = new SpscAtomicArrayQueue<>(1024 * 64);
    }

    @Override
    public void purge() {
        this.channelIterator.purge();
        this.batches.clear();
        if (!this.nextSingularBatches.isEmpty()) {
            LOGGER.warn("batches has element but will be discarded");
        }
        this.nextSingularBatches.clear();
    }

    @Override
    public Batch next() {
        final var nextBatch = nextSingularBatches.poll();
        if (nextBatch != null) {
            return nextBatch;
        }
        Channel channel = this.channelIterator.next();
        if (channel != null) {
            decodeBatches(this.config.chainConfig(), channel).forEach(batch -> {
                Batch prev = this.batches.put(batch.batch().getTimestamp(), batch);
                if (prev != null) {
                    LOGGER.warn(
                            "batch was replaced: timestamp={}", batch.batch().getTimestamp());
                }
            });
        }

        Batch derivedBatch = null;
        loop:
        while (true) {
            if (this.batches.firstEntry() != null) {
                Batch batch = this.batches.firstEntry().getValue();
                switch (batchStatus(batch)) {
                    case Accept:
                        derivedBatch = batch;
                        this.batches.remove(batch.timestamp());
                        break loop;
                    case Drop:
                        LOGGER.warn("dropping invalid batch");
                        this.batches.remove(batch.timestamp());
                        continue;
                    case Future, Undecided:
                        break loop;
                    default:
                        throw new IllegalStateException("Unexpected value: " + batchStatus(batch));
                }
            } else {
                break;
            }
        }

        Batch batch = null;
        if (derivedBatch != null) {
            List<Batch> singularBatches = this.getSingularBatches(derivedBatch.batch(), this.state.get());
            if (!singularBatches.isEmpty()) {
                this.nextSingularBatches.addAll(singularBatches);
                return this.nextSingularBatches.poll();
            }
        } else {
            State state = this.state.get();

            BigInteger currentL1Block = state.getCurrentEpochNum();
            BlockInfo safeHead = state.getSafeHead();
            Epoch epoch = state.getSafeEpoch();
            Epoch nextEpoch = state.epoch(epoch.number().add(BigInteger.ONE));
            BigInteger seqWindowSize = this.config.chainConfig().seqWindowSize();

            if (nextEpoch != null) {
                if (currentL1Block.compareTo(epoch.number().add(seqWindowSize)) > 0) {
                    BigInteger nextTimestamp =
                            safeHead.timestamp().add(this.config.chainConfig().blockTime());
                    Epoch epochRes = null;
                    if (nextTimestamp.compareTo(nextEpoch.timestamp()) < 0) {
                        epochRes = epoch;
                    } else if (currentL1Block.compareTo(nextEpoch.number().add(seqWindowSize)) > 0) {
                        epochRes = nextEpoch;
                    }
                    if (epochRes != null) {
                        var singularBatch = new SingularBatch(
                                safeHead.parentHash(),
                                epochRes.number(),
                                epochRes.hash(),
                                nextTimestamp,
                                Lists.newArrayList());
                        batch = new Batch(singularBatch, currentL1Block);
                    }
                }
            }
        }
        return batch;
    }

    /**
     * Decode batches list.
     *
     * @param chainConfig the chain config
     * @param channel the channel
     * @return the list
     */
    public static List<Batch> decodeBatches(final Config.ChainConfig chainConfig, final Channel channel) {
        byte[] channelData = decompressChannelData(channel.data());
        List<RlpType> batches = RlpDecoder.decode(channelData).getValues();
        return batches.stream()
                .map(rlpType -> {
                    byte[] buffer = ((RlpString) rlpType).getBytes();
                    byte batchType = buffer[0];
                    byte[] batchData = ArrayUtils.subarray(buffer, 1, buffer.length);

                    if (BatchType.SPAN_BATCH_TYPE.getCode() == ((int) batchType)) {
                        return Batch.decodeSpanBatch(
                                batchData,
                                chainConfig.blockTime(),
                                chainConfig.l2Genesis().timestamp(),
                                chainConfig.l2ChainId(),
                                channel.l1InclusionBlock());
                    } else if (BatchType.SINGULAR_BATCH_TYPE.getCode() == ((int) batchType)) {
                        RlpList rlpBatchData = (RlpList)
                                RlpDecoder.decode(batchData).getValues().getFirst();
                        return Batch.decodeSingularBatch(rlpBatchData, channel.l1InclusionBlock());
                    } else {
                        throw new IllegalArgumentException("invalid batch type");
                    }
                })
                .collect(Collectors.toList());
    }

    private static byte[] decompressChannelData(byte[] data) {
        byte compressType = data[0];
        if ((compressType & 0x0F) == Compressors.ZlibCM8 || (compressType & 0x0F) == Compressors.ZlibCM15) {
            try {
                return Compressors.zlibDecompress(data);
            } catch (IOException e) {
                throw new DecompressException(e);
            }
        } else if (compressType == Compressors.ChannelVersionBrotli) {
            try {
                return Compressors.brotliDecompress(ArrayUtils.subarray(data, 1, data.length));
            } catch (IOException e) {
                throw new DecompressException(e);
            }
        } else {
            throw new IllegalArgumentException("invalid compress type");
        }
    }

    @SuppressWarnings("WhitespaceAround")
    private BatchStatus batchStatus(final Batch batch) {
        if (batch.batch() instanceof SingularBatch) {
            return singularBatchStatus(batch);
        } else if (batch.batch() instanceof SpanBatch) {
            return spanBatchStatus(batch);
        } else {
            throw new IllegalStateException("unknown batch type");
        }
    }

    private BatchStatus singularBatchStatus(final Batch batchWrapper) {
        final SingularBatch batch = (SingularBatch) batchWrapper.batch();
        State state = this.state.get();
        Epoch epoch = state.getSafeEpoch();
        Epoch nextEpoch = state.epoch(epoch.number().add(BigInteger.ONE));
        BlockInfo head = state.getSafeHead();
        BigInteger nextTimestamp =
                head.timestamp().add(this.config.chainConfig().blockTime());

        // check timestamp range
        switch (batch.getTimestamp().compareTo(nextTimestamp)) {
            case 1 -> {
                return BatchStatus.Future;
            }
            case -1 -> {
                LOGGER.warn("invalid batch timestamp, excepted={}, actual={}", nextTimestamp, batch.getTimestamp());
                return BatchStatus.Drop;
            }
            default -> {}
        }

        // check that block builds on existing chain
        if (!batch.parentHash().equalsIgnoreCase(head.hash())) {
            LOGGER.warn("invalid parent hash");
            return BatchStatus.Drop;
        }

        // check the inclusion delay
        if (batch.epochNum().add(this.config.chainConfig().seqWindowSize()).compareTo(batchWrapper.l1InclusionBlock())
                < 0) {
            LOGGER.warn("inclusion window elapsed");
            return BatchStatus.Drop;
        }

        Epoch batchOrigin;
        // check and set batch origin epoch
        if (batch.epochNum().compareTo(epoch.number()) == 0) {
            batchOrigin = epoch;
        } else if (batch.epochNum().compareTo(epoch.number().add(BigInteger.ONE)) == 0) {
            batchOrigin = nextEpoch;
        } else {
            LOGGER.warn("invalid batch origin epoch number");
            return BatchStatus.Drop;
        }

        if (batchOrigin != null) {
            if (!batch.epochHash().equalsIgnoreCase(batchOrigin.hash())) {
                LOGGER.warn("invalid epoch hash");
                return BatchStatus.Drop;
            }

            if (batch.timestamp().compareTo(batchOrigin.timestamp()) < 0) {
                LOGGER.warn("batch too old");
                return BatchStatus.Drop;
            }

            BigInteger maxSeqDrift = this.config.chainConfig().maxSequencerDrift(batchOrigin.timestamp());
            // handle sequencer drift
            if (batch.timestamp().compareTo(batchOrigin.timestamp().add(maxSeqDrift)) > 0) {
                if (batch.transactions().isEmpty()) {
                    if (epoch.number().compareTo(batch.epochNum()) == 0) {
                        if (nextEpoch != null) {
                            if (batch.timestamp().compareTo(nextEpoch.timestamp()) >= 0) {
                                LOGGER.warn("sequencer drift too large");
                                return BatchStatus.Drop;
                            }
                        } else {
                            LOGGER.debug("sequencer drift undecided");
                            return BatchStatus.Undecided;
                        }
                    }
                } else {
                    LOGGER.warn("sequencer drift too large");
                    return BatchStatus.Drop;
                }
            }
        } else {
            LOGGER.debug("batch origin not known");
            return BatchStatus.Undecided;
        }

        if (batch.hasInvalidTransactions()) {
            LOGGER.warn("invalid transaction");
            return BatchStatus.Drop;
        }

        return BatchStatus.Accept;
    }

    private BatchStatus spanBatchStatus(final Batch batchWrapper) {
        final SpanBatch spanBatch = (SpanBatch) batchWrapper.batch();
        final State state = this.state.get();
        final Epoch epoch = state.getSafeEpoch();
        final Epoch nextEpoch = state.epoch(epoch.number().add(BigInteger.ONE));
        final BlockInfo l2SafeHead = state.getSafeHead();
        final BigInteger nextTimestamp =
                l2SafeHead.timestamp().add(this.config.chainConfig().blockTime());

        final BigInteger startEpochNum = spanBatch.getStartEpochNum();
        final BigInteger endEpochNum = spanBatch.getBlockEpochNum(spanBatch.getBlockCount() - 1);

        final BigInteger spanStartTimestamp = spanBatch.getTimestamp();
        final BigInteger spanEndTimestamp = spanBatch.getBlockTimestamp(spanBatch.getBlockCount() - 1);

        // check batch timestamp
        if (spanEndTimestamp.compareTo(nextTimestamp) < 0) {
            LOGGER.warn(
                    "past batch: nextTimestamp = l2SafeHead({}) + blockTime({}), spanEndTimestamp({})",
                    l2SafeHead.timestamp(),
                    this.config.chainConfig().blockTime(),
                    spanEndTimestamp);
            return BatchStatus.Drop;
        }
        if (spanStartTimestamp.compareTo(nextTimestamp) > 0) {
            return BatchStatus.Future;
        }

        // check for delta activation
        // startEpoch == (safeEpoch.number + 1)
        final Epoch batchOrigin = startEpochNum.compareTo(epoch.number().add(BigInteger.ONE)) == 0 ? nextEpoch : epoch;

        if (batchOrigin == null) {
            return BatchStatus.Undecided;
        }
        if (this.config.chainConfig().deltaTime().compareTo(BigInteger.ZERO) >= 0
                && batchOrigin.timestamp().compareTo(this.config.chainConfig().deltaTime()) < 0) {
            LOGGER.warn("epoch start time is before delta activation: epochStartTime=%d"
                    .formatted(batchOrigin.timestamp()));
            return BatchStatus.Drop;
        }

        // find previous l2 block
        final BigInteger prevTimestamp =
                spanStartTimestamp.subtract(this.config.chainConfig().blockTime());
        final var prevL2Info = state.l2Info(prevTimestamp);
        if (prevL2Info == null) {
            LOGGER.warn("previous l2 block not found: %d".formatted(prevTimestamp));
            return BatchStatus.Drop;
        }

        final var prevL2Block = prevL2Info.component1();
        final var prevL2Epoch = prevL2Info.component2();

        // check that block builds on existing chain
        final String spanBatchParentCheck = spanBatch.getParentCheck().toHexString();
        if (!spanBatch.checkParentHash(Bytes.fromHexString(prevL2Block.hash()))) {
            LOGGER.warn(
                    "batch parent check failed: batchParent={}; prevL2BlockHash={}",
                    spanBatchParentCheck,
                    prevL2Block.hash());
            return BatchStatus.Drop;
        }

        if (startEpochNum.add(this.config.chainConfig().seqWindowSize()).compareTo(batchWrapper.l1InclusionBlock())
                < 0) {
            LOGGER.warn(
                    "sequence window check failed: startEpochNum={} + seqWindowSize={} < l1InclusionBlock={}",
                    startEpochNum,
                    this.config.chainConfig().seqWindowSize(),
                    batchWrapper.l1InclusionBlock());
            return BatchStatus.Drop;
        }

        if (startEpochNum.compareTo(prevL2Block.number().add(BigInteger.ONE)) > 0) {
            LOGGER.warn("invalid start epoch number");
            return BatchStatus.Drop;
        }

        final Epoch l1Origin = state.epoch(endEpochNum);
        if (l1Origin == null) {
            LOGGER.warn("l1 origin not found");
            return BatchStatus.Drop;
        }
        final String l1OriginCheck = spanBatch.getL1OriginCheck().toHexString();
        if (!spanBatch.checkOriginHash(Bytes.fromHexString(l1Origin.hash()))) {
            LOGGER.warn("l1 origin check failed: l1OriginCheck={}; l1Origin={}", l1OriginCheck, l1Origin.hash());
            return BatchStatus.Drop;
        }

        if (startEpochNum.compareTo(prevL2Epoch.number()) < 0) {
            LOGGER.warn("invalid start epoch number");
            return BatchStatus.Drop;
        }

        // check sequencer drift
        final int blockCount = spanBatch.getBlockCount();
        for (int i = 0; i < blockCount; i++) {
            final var blockTimestamp = spanBatch.getBlockTimestamp(i);
            if (blockTimestamp.compareTo(l2SafeHead.timestamp()) <= 0) {
                continue;
            }
            final BigInteger l1OriginNum = spanBatch.getBlockEpochNum(i);
            final L1Info batchL1Origin = state.l1Info(l1OriginNum);
            if (batchL1Origin == null) {
                LOGGER.warn("l1 origin not found");
                return BatchStatus.Drop;
            }
            if (blockTimestamp.compareTo(batchL1Origin.blockInfo().timestamp()) < 0) {
                LOGGER.warn("block timestamp is less than L1 origin timestamp");
                return BatchStatus.Drop;
            }
            BigInteger maxSeqDrift = this.config
                    .chainConfig()
                    .maxSequencerDrift(batchL1Origin.blockInfo().timestamp());
            final var max = batchL1Origin.blockInfo().timestamp().add(maxSeqDrift);
            if (blockTimestamp.compareTo(max) > 0) {
                if (!spanBatch.getBlockTransactions(i).isEmpty()) {
                    LOGGER.warn(String.format(
                            "batch exceeded sequencer time drift, sequencer must adopt new L1 origin to include transactions again: max=%d",
                            max));
                    return BatchStatus.Drop;
                }
                boolean originAdvanced;
                if (i == 0) {
                    originAdvanced = startEpochNum.compareTo(l2SafeHead.number().add(BigInteger.ONE)) == 0;
                } else {
                    originAdvanced = spanBatch.getBlockEpochNum(i).compareTo(spanBatch.getBlockEpochNum(i - 1)) > 0;
                }

                if (!originAdvanced) {
                    var batchNextEpoch = state.l1Info(l1OriginNum.add(BigInteger.ONE));
                    if (batchNextEpoch != null) {
                        if (blockTimestamp.compareTo(batchNextEpoch.blockInfo().timestamp()) >= 0) {
                            LOGGER.warn(
                                    "batch exceeded sequencer time drift without adopting next origin, and next L1 origin would have been valid");
                            return BatchStatus.Drop;
                        }
                    } else {
                        return BatchStatus.Undecided;
                    }
                }
            }

            if (spanBatch.hasInvalidTransactions(i)) {
                LOGGER.warn(String.format("invalid transaction: empty or deposits into batch data: txIndex=%d", i));
                return BatchStatus.Drop;
            }
        }

        // overlapped block checks
        for (SpanBatchElement element : spanBatch.getBatches()) {
            if (element.timestamp().compareTo(nextTimestamp) >= 0) {
                continue;
            }

            Tuple2<BlockInfo, Epoch> info = state.l2Info(element.timestamp());
            if (info == null) {
                LOGGER.warn("overlapped l2 block not found");
                return BatchStatus.Drop;
            }

            if (element.epochNum().compareTo(info.component2().number()) != 0) {
                LOGGER.warn("epoch mismatch in overlapped blocks");
                return BatchStatus.Drop;
            }
        }
        return BatchStatus.Accept;
    }

    /**
     * Gets singular batche list from IBatch instance.
     *
     * @param batch IBatch instance
     * @param state the state
     * @return the list that inner batch data was singular batch
     */
    public List<Batch> getSingularBatches(final IBatch batch, final State state) {
        Function<SingularBatch, Batch> f = singularBatch -> new Batch(singularBatch, state.getCurrentEpochNum());
        if (batch instanceof SingularBatch typedBatch) {
            return List.of(f.apply(typedBatch));
        } else if (batch instanceof SpanBatch typedBatch) {
            return this.toSingularBatches(typedBatch, state).stream().map(f).collect(Collectors.toList());
        } else {
            throw new IllegalStateException("unknown batch type");
        }
    }

    private List<SingularBatch> toSingularBatches(final SpanBatch batch, final State state) {
        List<SingularBatch> singularBatches = new ArrayList<>();
        for (SpanBatchElement element : batch.getBatches()) {
            if (element.timestamp().compareTo(state.getSafeHead().timestamp()) <= 0) {
                if (!element.transactions().isEmpty()) {
                    LOGGER.warn(
                            "past span batch element: timestamp{{}} <= safeHead.timestamp={{}}",
                            element.timestamp(),
                            state.getSafeHead().timestamp());
                }
                continue;
            }
            SingularBatch singularBatch = new SingularBatch();
            singularBatch.setEpochNum(element.epochNum());
            singularBatch.setTimestamp(element.timestamp());
            singularBatch.setTransactions(element.transactions());

            Epoch l1Origins = state.epoch(element.epochNum());
            if (l1Origins == null) {
                throw new RuntimeException("cannot find origin for epochNum: %d".formatted(element.epochNum()));
            }
            singularBatch.setEpochHash(state.epoch(singularBatch.epochNum()).hash());
            singularBatches.add(singularBatch);
        }
        return singularBatches;
    }

    /**
     * Create batches.
     *
     * @param <I> the type parameter
     * @param channelIterator the channel iterator
     * @param state the state
     * @param config the config
     * @return the batches
     */
    public static <I extends PurgeableIterator<Channel>> Batches<I> create(
            I channelIterator, AtomicReference<State> state, Config config) {
        return new Batches<>(new TreeMap<>(), channelIterator, state, config);
    }

    /**
     * The enum BatchStatus.
     *
     * @author grapebaba
     * @since 0.1.0
     */
    public enum BatchStatus {
        /** Drop batch status. */
        Drop,
        /** Accept batch status. */
        Accept,
        /** Undecided batch status. */
        Undecided,
        /** Future batch status. */
        Future,
    }
}
