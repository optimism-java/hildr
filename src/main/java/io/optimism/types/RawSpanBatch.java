package io.optimism.types;

import io.netty.buffer.ByteBuf;
import io.optimism.utilities.spanbatch.SpanBatchUtils;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.ArrayUtils;
import org.web3j.utils.Numeric;

/**
 * The type RawSpanBatch.
 *
 * @author grapebaba
 * @since 0.2.4
 */
public class RawSpanBatch {
    private SpanBatchPrefix spanbatchPrefix;
    private SpanBatchPayload spanbatchPayload;

    /**
     * Instantiates a new Raw span batch.
     */
    public RawSpanBatch() {
        this.spanbatchPrefix = new SpanBatchPrefix();
        this.spanbatchPayload = new SpanBatchPayload();
    }

    /**
     * Instantiates a new Raw span batch.
     *
     * @param spanbatchPrefix  the spanbatch prefix
     * @param spanbatchPayload the spanbatch payload
     */
    public RawSpanBatch(SpanBatchPrefix spanbatchPrefix, SpanBatchPayload spanbatchPayload) {
        this.spanbatchPrefix = spanbatchPrefix;
        this.spanbatchPayload = spanbatchPayload;
    }

    /**
     * Spanbatch prefix span batch prefix.
     *
     * @return the span batch prefix
     */
    public SpanBatchPrefix spanbatchPrefix() {
        return spanbatchPrefix;
    }

    /**
     * Spanbatch payload span batch payload.
     *
     * @return the span batch payload
     */
    public SpanBatchPayload spanbatchPayload() {
        return spanbatchPayload;
    }

    /**
     * Sets spanbatch prefix.
     *
     * @param spanbatchPrefix the spanbatch prefix
     */
    public void setSpanbatchPrefix(SpanBatchPrefix spanbatchPrefix) {
        this.spanbatchPrefix = spanbatchPrefix;
    }

    /**
     * Sets spanbatch payload.
     *
     * @param spanbatchPayload the spanbatch payload
     */
    public void setSpanbatchPayload(SpanBatchPayload spanbatchPayload) {
        this.spanbatchPayload = spanbatchPayload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RawSpanBatch that)) return false;
        return Objects.equals(spanbatchPrefix, that.spanbatchPrefix)
                && Objects.equals(spanbatchPayload, that.spanbatchPayload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(spanbatchPrefix, spanbatchPayload);
    }

    @Override
    public String toString() {
        return "RawSpanBatch[spanbatchPrefix=%s, spanbatchPayload=%s]".formatted(spanbatchPrefix, spanbatchPayload);
    }

    /**
     * Encode raw span batch.
     *
     * @return the encoded raw span batch bytes
     */
    public byte[] encode() {
        return ArrayUtils.addAll(this.spanbatchPrefix.encode(), this.spanbatchPayload.encode());
    }

    /**
     * Decode.
     *
     * @param source the source
     */
    public void decode(ByteBuf source) {
        if (source.readableBytes() > SpanBatchUtils.MaxSpanBatchSize) {
            throw new IllegalArgumentException("SpanBatch is too large");
        }
        this.spanbatchPrefix.decode(source);
        this.spanbatchPayload.decode(source);
    }

    /**
     * Derive span batch.
     *
     * @param blockTime        the block time
     * @param genesisTimestamp the genesis timestamp
     * @param chainID          the chain id
     * @return the span batch
     */
    public SpanBatch derive(BigInteger blockTime, BigInteger genesisTimestamp, BigInteger chainID) {
        if (this.spanbatchPayload.blockCount() == 0) {
            throw new IllegalArgumentException("SpanBatch block count cannot be zero");
        }

        BigInteger[] blockOriginNums = new BigInteger[(int) this.spanbatchPayload.blockCount()];

        BigInteger l1OriginBlockNumber = this.spanbatchPrefix.l1OriginNum();

        for (int i = (int) (this.spanbatchPayload.blockCount() - 1); i >= 0; i--) {
            blockOriginNums[i] = l1OriginBlockNumber;
            if (this.spanbatchPayload.originBits().testBit(i) && i > 0) {
                l1OriginBlockNumber = l1OriginBlockNumber.subtract(BigInteger.ONE);
            }
        }

        this.spanbatchPayload.txs().recoverV(chainID);
        List<byte[]> fullTxs = this.spanbatchPayload.txs().fullTxs(chainID);

        SpanBatch spanBatch = new SpanBatch();
        spanBatch.setParentCheck(this.spanbatchPrefix.parentCheck());
        spanBatch.setL1OriginCheck(this.spanbatchPrefix.l1OriginCheck());

        int txIndex = 0;
        for (int i = 0; i < this.spanbatchPayload.blockCount(); i++) {
            SpanBatchElement spanBatchElement = new SpanBatchElement();
            spanBatchElement.setTimestamp(genesisTimestamp
                    .add(this.spanbatchPrefix.relTimestamp())
                    .add(blockTime.multiply(BigInteger.valueOf(i))));
            spanBatchElement.setEpochNum(blockOriginNums[i]);
            List<String> txs = new ArrayList<>();
            for (int j = 0; j < this.spanbatchPayload.blockTxCounts().get(i); j++) {
                txs.add(Numeric.toHexString(fullTxs.get(txIndex)));
                txIndex++;
            }
            spanBatchElement.setTransactions(txs);
            spanBatch.getBatches().add(spanBatchElement);
        }

        return spanBatch;
    }

    /**
     * To span batch span batch.
     *
     * @param blockTime        the block time
     * @param genesisTimestamp the genesis timestamp
     * @param chainID          the chain id
     * @return the span batch
     */
    public SpanBatch toSpanBatch(BigInteger blockTime, BigInteger genesisTimestamp, BigInteger chainID) {
        return derive(blockTime, genesisTimestamp, chainID);
    }
}
