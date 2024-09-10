package io.optimism.types;

import static io.optimism.utilities.spanbatch.SpanBatchUtils.putVarLong;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.PooledByteBufAllocator;
import io.optimism.utilities.spanbatch.SpanBatchUtils;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The type SpanBatchPayload.
 *
 * @author grapebaba
 * @since 0.2.4
 */
public class SpanBatchPayload {
    private long blockCount;
    private BigInteger originBits;
    private List<Long> blockTxCounts = new ArrayList<>();
    private SpanBatchTxs txs;

    /**
     * Instantiates a new Span batch payload.
     */
    public SpanBatchPayload() {}

    /**
     * Instantiates a new Span batch payload.
     *
     * @param blockCount    the block count
     * @param originBits    the origin bits
     * @param blockTxCounts the block tx counts
     * @param txs           the txs
     */
    public SpanBatchPayload(long blockCount, BigInteger originBits, List<Long> blockTxCounts, SpanBatchTxs txs) {
        this.blockCount = blockCount;
        this.originBits = originBits;
        this.blockTxCounts = blockTxCounts;
        this.txs = txs;
    }

    /**
     * Block count long.
     *
     * @return the long
     */
    public long blockCount() {
        return blockCount;
    }

    /**
     * Origin bits big integer.
     *
     * @return the big integer
     */
    public BigInteger originBits() {
        return originBits;
    }

    /**
     * Block tx counts list.
     *
     * @return the list
     */
    public List<Long> blockTxCounts() {
        return blockTxCounts;
    }

    /**
     * Txs span batch txs.
     *
     * @return the span batch txs
     */
    public SpanBatchTxs txs() {
        return txs;
    }

    /**
     * Sets block count.
     *
     * @param blockCount the block count
     */
    public void setBlockCount(long blockCount) {
        this.blockCount = blockCount;
    }

    /**
     * Sets origin bits.
     *
     * @param originBits the origin bits
     */
    public void setOriginBits(BigInteger originBits) {
        this.originBits = originBits;
    }

    /**
     * Sets block tx counts.
     *
     * @param blockTxCounts the block tx counts
     */
    public void setBlockTxCounts(List<Long> blockTxCounts) {
        this.blockTxCounts = blockTxCounts;
    }

    /**
     * Sets txs.
     *
     * @param txs the txs
     */
    public void setTxs(SpanBatchTxs txs) {
        this.txs = txs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SpanBatchPayload that)) return false;
        return blockCount == that.blockCount
                && Objects.equals(originBits, that.originBits)
                && Objects.equals(blockTxCounts, that.blockTxCounts)
                && Objects.equals(txs, that.txs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockCount, originBits, blockTxCounts, txs);
    }

    @Override
    public String toString() {
        return "SpanBatchPayload[blockCount=%d, originBits=%s, blockTxCounts=%s, txs=%s]"
                .formatted(blockCount, originBits, blockTxCounts, txs);
    }

    /**
     * Decode origin bits.
     *
     * @param source the source
     */
    public void decodeOriginBits(ByteBuf source) {
        this.originBits = SpanBatchUtils.decodeSpanBatchBits(source, (int) this.blockCount);
    }

    /**
     * Encode origin bits byte [ ].
     *
     * @return the byte [ ]
     */
    public byte[] encodeOriginBits() {
        return SpanBatchUtils.encodeSpanBatchBits((int) this.blockCount, this.originBits);
    }

    /**
     * Decode block count.
     *
     * @param source the source
     */
    public void decodeBlockCount(ByteBuf source) {
        var blockCount = SpanBatchUtils.getVarLong(source);
        if (blockCount > SpanBatchUtils.MaxSpanBatchSize) {
            throw new RuntimeException("span batch size limit reached");
        }

        if (blockCount == 0) {
            throw new RuntimeException("block count cannot be zero");
        }

        this.blockCount = blockCount;
    }

    /**
     * Encode block count byte [ ].
     *
     * @return the byte [ ]
     */
    public byte[] encodeBlockCount() {
        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer(10);
        putVarLong(this.blockCount, buffer);

        return ByteBufUtil.getBytes(buffer);
    }

    /**
     * Decode block tx counts.
     *
     * @param source the source
     */
    public void decodeBlockTxCounts(ByteBuf source) {
        List<Long> blockTxCounts = new ArrayList<>();
        for (int i = 0; i < this.blockCount; i++) {
            var txCount = SpanBatchUtils.getVarLong(source);
            if (txCount > SpanBatchUtils.MaxSpanBatchSize) {
                throw new RuntimeException("span batch size limit reached");
            }
            blockTxCounts.add(txCount);
        }
        this.blockTxCounts = blockTxCounts;
    }

    /**
     * Encode block tx counts byte [ ].
     *
     * @return the byte [ ]
     */
    public byte[] encodeBlockTxCounts() {
        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer(10 * this.blockTxCounts.size());
        for (Long blockTxCount : this.blockTxCounts) {
            putVarLong(blockTxCount, buffer);
        }

        return ByteBufUtil.getBytes(buffer);
    }

    /**
     * Decode txs.
     *
     * @param source the source
     */
    public void decodeTxs(ByteBuf source) {
        if (this.txs == null) {
            this.txs = new SpanBatchTxs();
        }

        if (this.blockTxCounts == null || this.blockTxCounts.isEmpty()) {
            throw new RuntimeException("blockTxCounts cannot be null");
        }

        long totalBlockTxCount = 0;
        for (Long blockTxCount : this.blockTxCounts) {
            long total;
            try {
                total = Math.addExact(totalBlockTxCount, blockTxCount);
            } catch (ArithmeticException e) {
                throw new RuntimeException("totalBlockTxCount overflow");
            }
            totalBlockTxCount = total;
        }

        if (totalBlockTxCount > SpanBatchUtils.MaxSpanBatchSize) {
            throw new RuntimeException("span batch size limit reached");
        }

        this.txs.setTotalBlockTxCount(totalBlockTxCount);
        this.txs.decode(source);
    }

    /**
     * Encode txs byte [ ].
     *
     * @return the byte [ ]
     */
    public byte[] encodeTxs() {
        if (this.txs == null) {
            throw new RuntimeException("txs cannot be null");
        }

        return this.txs.encode();
    }

    /**
     * Decode.
     *
     * @param source the source
     */
    public void decode(ByteBuf source) {
        decodeBlockCount(source);
        decodeOriginBits(source);
        decodeBlockTxCounts(source);
        decodeTxs(source);
    }

    /**
     * Encode byte [ ].
     *
     * @return the byte [ ]
     */
    public byte[] encode() {
        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer();
        buffer.writeBytes(encodeBlockCount());
        buffer.writeBytes(encodeOriginBits());
        buffer.writeBytes(encodeBlockTxCounts());
        buffer.writeBytes(encodeTxs());

        return ByteBufUtil.getBytes(buffer);
    }
}
