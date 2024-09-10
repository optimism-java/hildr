package io.optimism.types;

import static org.slf4j.LoggerFactory.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.optimism.types.enums.BatchType;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.tuweni.bytes.Bytes;
import org.slf4j.Logger;
import org.web3j.utils.Numeric;

/**
 * SpanBatch is an implementation of Batch interface,
 * containing the input to build a span of L2 blocks in derived form (SpanBatchElement)
 *
 * @author zhouop0
 * @since 0.1.0
 */
public class SpanBatch implements IBatch {

    private static final ObjectMapper mapper = new ObjectMapper();

    private static final Logger LOGGER = getLogger(SpanBatch.class);
    // First 20 bytes of the first block's parent hash
    private Bytes parentCheck;
    // First 20 bytes of the last block's L1 origin hash
    private Bytes l1OriginCheck;
    // List of block input in derived form
    private List<SpanBatchElement> batches = new ArrayList<>();

    /**
     * Instantiates a new Span batch.
     */
    public SpanBatch() {}

    /**
     * Gets parent check.
     *
     * @return the parent check
     */
    public Bytes getParentCheck() {
        return parentCheck;
    }

    /**
     * Gets l 1 origin check.
     *
     * @return the l 1 origin check
     */
    public Bytes getL1OriginCheck() {
        return l1OriginCheck;
    }

    /**
     * Gets batches.
     *
     * @return the batches
     */
    public List<SpanBatchElement> getBatches() {
        return batches;
    }

    /**
     * Sets parent check.
     *
     * @param parentCheck the parent check
     */
    public void setParentCheck(Bytes parentCheck) {
        this.parentCheck = parentCheck;
    }

    /**
     * Sets l 1 origin check.
     *
     * @param l1OriginCheck the l 1 origin check
     */
    public void setL1OriginCheck(Bytes l1OriginCheck) {
        this.l1OriginCheck = l1OriginCheck;
    }

    /**
     * Sets batches.
     *
     * @param batches the batches
     */
    public void setBatches(List<SpanBatchElement> batches) {
        this.batches = batches;
    }

    @Override
    public BatchType getBatchType() {
        return BatchType.SPAN_BATCH_TYPE;
    }

    @Override
    public BigInteger getTimestamp() {
        return batches.getFirst().timestamp();
    }

    /**
     * GetStartEpochNum returns epoch number(L1 origin block number) of the first block in the span.
     *
     * @return the epoch number(L1 origin block number) of the first block in the span.
     */
    public BigInteger getStartEpochNum() {
        return this.batches.getFirst().epochNum();
    }

    /**
     * checks if the parentCheck matches the first 20 bytes of given hash, probably the current L2 safe head.
     *
     * @param hash the first 20 bytes of given hash.
     * @return boolean. boolean
     */
    public boolean checkOriginHash(Bytes hash) {
        return this.l1OriginCheck.equals(hash.slice(0, this.l1OriginCheck.size()));
    }

    /**
     * checks if the parentCheck matches the first 20 bytes of given hash, probably the current L2 safe head.
     *
     * @param hash the first 20 bytes of given hash.
     * @return boolean. boolean
     */
    public boolean checkParentHash(Bytes hash) {
        return this.parentCheck.equals(hash.slice(0, this.parentCheck.size()));
    }

    /**
     * GetBlockEpochNum
     *
     * @param index batches index.
     * @return the epoch number(L1 origin block number) of the block at the given index in the span.
     */
    public BigInteger getBlockEpochNum(int index) {
        return this.batches.get(index).epochNum();
    }

    /**
     * GetBlockTimestamp
     *
     * @param index batches index.
     * @return the timestamp of the block at the given index in the span.
     */
    public BigInteger getBlockTimestamp(int index) {
        return this.batches.get(index).timestamp();
    }

    /**
     * Gets block transactions.
     *
     * @param index the index
     * @return the block transactions
     */
    public List<String> getBlockTransactions(int index) {
        return this.batches.get(index).transactions();
    }

    /**
     * Has invalid transactions boolean.
     *
     * @param index the index
     * @return the boolean
     */
    public boolean hasInvalidTransactions(int index) {
        return this.getBlockTransactions(index).stream()
                .anyMatch(s -> StringUtils.isEmpty(s)
                        || (Numeric.containsHexPrefix(s)
                                ? StringUtils.startsWithIgnoreCase(s, "0x7E")
                                : StringUtils.startsWithIgnoreCase(s, "7E")));
    }

    /**
     * GetBlockCount
     *
     * @return the number of blocks in the span.
     */
    public int getBlockCount() {
        return this.batches.size();
    }

    /**
     * AppendSingularBatch appends a SingularBatch into the span batch
     * updates l1OriginCheck or parentCheck if needed.
     *
     * @param singularBatch SingularBatch
     */
    public void appendSingularBatch(SingularBatch singularBatch) {
        if (batches.isEmpty()) {
            this.parentCheck =
                    Bytes.fromHexStringLenient(singularBatch.parentHash().substring(0, 40));
        }
        this.batches.add(SpanBatchElement.singularBatchToElement(singularBatch)); // add the batch to the list
        this.l1OriginCheck =
                Bytes.fromHexStringLenient(singularBatch.epochHash().substring(0, 40)); // update l1OriginCheck
    }

    /**
     * To raw span batch raw span batch.
     *
     * @param originChangedBit the origin changed bit
     * @param genesisTimestamp the genesis timestamp
     * @param chainID          the chain id
     * @return the raw span batch
     */
    public RawSpanBatch toRawSpanBatch(int originChangedBit, BigInteger genesisTimestamp, BigInteger chainID) {
        if (this.batches.isEmpty()) {
            throw new RuntimeException("cannot merge empty singularBatch list");
        }

        RawSpanBatch rawSpanBatch = new RawSpanBatch();

        this.batches.sort(Comparator.comparing(SpanBatchElement::timestamp));

        SpanBatchElement spanStart = this.batches.getFirst();
        SpanBatchElement spanEnd = this.batches.getLast();

        rawSpanBatch.spanbatchPrefix().setRelTimestamp(spanStart.timestamp().subtract(genesisTimestamp));
        rawSpanBatch.spanbatchPrefix().setL1OriginNum(spanEnd.epochNum());
        rawSpanBatch.spanbatchPrefix().setParentCheck(this.parentCheck);
        rawSpanBatch.spanbatchPrefix().setL1OriginCheck(this.l1OriginCheck);

        rawSpanBatch.spanbatchPayload().setBlockCount(this.batches.size());
        BigInteger originBits = BigInteger.ZERO;
        if (originChangedBit == 1) {
            originBits = originBits.setBit(0);
        }
        for (int i = 1; i < this.batches.size(); i++) {
            if (this.batches.get(i - 1).epochNum().compareTo(this.batches.get(i).epochNum()) < 0) {
                originBits = originBits.setBit(i);
            } else {
                originBits = originBits.clearBit(i);
            }
        }
        rawSpanBatch.spanbatchPayload().setOriginBits(originBits);

        List<Long> blockTxCounts = new ArrayList<>();
        List<String> txs = new ArrayList<>();
        for (SpanBatchElement batch : this.batches) {
            blockTxCounts.add((long) batch.transactions().size());
            txs.addAll(batch.transactions());
        }
        rawSpanBatch.spanbatchPayload().setBlockTxCounts(blockTxCounts);
        SpanBatchTxs spanBatchTxs = SpanBatchTxs.newSpanBatchTxs(
                txs.stream().map(Numeric::hexStringToByteArray).collect(Collectors.toList()), chainID);
        rawSpanBatch.spanbatchPayload().setTxs(spanBatchTxs);

        return rawSpanBatch;
    }

    /**
     * Gets singular batches.
     *
     * @param l1Origins  the l 1 origins
     * @param l2SafeHead the l 2 safe head
     * @return the singular batches
     */
    public List<SingularBatch> getSingularBatches(List<L1BlockRef> l1Origins, L2BlockRef l2SafeHead) {
        List<SingularBatch> singularBatches = new ArrayList<>();
        int originIdx = 0;
        for (SpanBatchElement batch : this.batches) {
            if (batch.timestamp().compareTo(l2SafeHead.timestamp()) <= 0) {
                continue;
            }

            SingularBatch singularBatch = new SingularBatch();
            singularBatch.setEpochNum(batch.epochNum());
            singularBatch.setTimestamp(batch.timestamp());
            singularBatch.setTransactions(batch.transactions());

            boolean originFound = false;
            for (int i = originIdx; i < l1Origins.size(); i++) {
                if (l1Origins.get(i).number().compareTo(batch.epochNum()) == 0) {
                    originIdx = i;
                    singularBatch.setEpochHash(l1Origins.get(i).hash());
                    originFound = true;
                    break;
                }
            }

            if (!originFound) {
                throw new RuntimeException("cannot find origin for epochNum: %s".formatted(batch.epochNum()));
            }

            singularBatches.add(singularBatch);
        }
        return singularBatches;
    }

    /**
     * Marshal json bytes.
     *
     * @return the bytes
     */
    public Bytes marshalJSON() {
        SpanBatchJSON spanBatchJSON = new SpanBatchJSON(this);
        try {
            return Bytes.wrap(mapper.writeValueAsBytes(spanBatchJSON));
        } catch (Exception e) {
            LOGGER.error("Failed to marshal SpanBatchJSON to JSON", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * The type Span batch json.
     */
    public static class SpanBatchJSON {
        /**
         * The Parent check.
         */
        public String parentCheck;
        /**
         * The L 1 origin check.
         */
        public String l1OriginCheck;
        /**
         * The Batches.
         */
        public List<SpanBatchElement> batches;

        /**
         * Instantiates a new Span batch json.
         */
        public SpanBatchJSON() {}

        /**
         * Instantiates a new Span batch json.
         *
         * @param parentCheck   the parent check
         * @param l1OriginCheck the l 1 origin check
         * @param batches       the batches
         */
        public SpanBatchJSON(String parentCheck, String l1OriginCheck, List<SpanBatchElement> batches) {
            this.parentCheck = parentCheck;
            this.l1OriginCheck = l1OriginCheck;
            this.batches = batches;
        }

        /**
         * Instantiates a new Span batch json.
         *
         * @param spanBatch the span batch
         */
        public SpanBatchJSON(SpanBatch spanBatch) {
            this.parentCheck = spanBatch.parentCheck.toHexString();
            this.l1OriginCheck = spanBatch.l1OriginCheck.toHexString();
            this.batches = spanBatch.batches;
        }

        /**
         * Unmarshal json span batch.
         *
         * @return the span batch
         */
        public SpanBatch unmarshalJSON() {
            SpanBatch spanBatch = new SpanBatch();
            spanBatch.parentCheck = Bytes.fromHexStringLenient(this.parentCheck);
            spanBatch.l1OriginCheck = Bytes.fromHexStringLenient(this.l1OriginCheck);
            spanBatch.batches = this.batches;
            return spanBatch;
        }
    }

    /**
     * New span batch span batch.
     *
     * @param singularBatches the singular batches
     * @return the span batch
     */
    public static SpanBatch newSpanBatch(List<SingularBatch> singularBatches) {
        SpanBatch spanBatch = new SpanBatch();
        if (singularBatches.isEmpty()) {
            return spanBatch;
        }
        spanBatch.batches = singularBatches.stream()
                .map(SpanBatchElement::singularBatchToElement)
                .collect(Collectors.toList());
        spanBatch.parentCheck = Bytes.fromHexStringLenient(
                singularBatches.getFirst().parentHash().substring(0, 40));
        spanBatch.l1OriginCheck =
                Bytes.fromHexStringLenient(singularBatches.getLast().epochHash().substring(0, 40));
        return spanBatch;
    }

    /**
     * Derive raw span batch to span batch.
     *
     * @param batch            the batch
     * @param blockTime        the block time
     * @param genesisTimestamp the genesis timestamp
     * @param chainID          the chain id
     * @return the span batch
     */
    public static SpanBatch deriveSpanBatch(
            RawSpanBatch batch, BigInteger blockTime, BigInteger genesisTimestamp, BigInteger chainID) {
        return batch.toSpanBatch(blockTime, genesisTimestamp, chainID);
    }

    /**
     * The type SpanBatchBuilder.
     */
    public static class SpanBatchBuilder {
        private BigInteger genesisTimestamp;
        private BigInteger chainID;

        private SpanBatch spanBatch;

        private int originChangedBit;

        /**
         * Instantiates a new Span batch builder.
         *
         * @param genesisTimestamp the genesis timestamp
         * @param chainID          the chain id
         */
        public SpanBatchBuilder(BigInteger genesisTimestamp, BigInteger chainID) {
            this.genesisTimestamp = genesisTimestamp;
            this.chainID = chainID;
            this.spanBatch = new SpanBatch();
        }

        /**
         * Append singular batch.
         *
         * @param singularBatch the singular batch
         * @param seqNum        the seq num
         */
        public void appendSingularBatch(SingularBatch singularBatch, BigInteger seqNum) {
            if (this.getBlockCount() == 0) {
                this.originChangedBit = 0;
                if (seqNum.compareTo(BigInteger.ZERO) == 0) {
                    this.originChangedBit = 1;
                }
            }

            this.spanBatch.appendSingularBatch(singularBatch);
        }

        /**
         * Gets raw span batch.
         *
         * @return the raw span batch
         */
        public RawSpanBatch getRawSpanBatch() {
            return this.spanBatch.toRawSpanBatch(this.originChangedBit, this.genesisTimestamp, this.chainID);
        }

        /**
         * Gets block count.
         *
         * @return the block count
         */
        public int getBlockCount() {
            return this.spanBatch.getBatches().size();
        }

        /**
         * Reset.
         */
        public void reset() {
            this.spanBatch = new SpanBatch();
        }
    }
}
