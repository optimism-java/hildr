package io.optimism.types;

import io.netty.buffer.Unpooled;
import java.math.BigInteger;
import org.web3j.rlp.RlpList;

/**
 * The type Batch.
 *
 * @param batch the batch
 * @param l1InclusionBlock L1 inclusion block
 * @author grapebaba
 * @since 0.2.4
 */
public record Batch(IBatch batch, BigInteger l1InclusionBlock) {

    /**
     * Gets timestamp.
     *
     * @return the timestamp
     */
    public BigInteger timestamp() {
        return batch.getTimestamp();
    }

    /**
     * Encode batch.
     *
     * @return encoded bytes by the batch
     */
    public byte[] encode() {
        if (batch instanceof SingularBatch) {
            var typedBatch = (SingularBatch) batch;
            return typedBatch.encode();
        } else if (batch instanceof SpanBatch) {
            throw new IllegalStateException("unsupport batch type: %s".formatted(batch.getBatchType()));
        } else {
            throw new IllegalStateException("unknown batch type");
        }
    }

    /**
     * Decode singular batch.
     *
     * @param rlp the rlp
     * @param l1InclusionBlock L1 inclusion block
     * @return the batch
     */
    public static Batch decodeSingularBatch(final RlpList rlp, final BigInteger l1InclusionBlock) {
        return new Batch(SingularBatch.decode(rlp), l1InclusionBlock);
    }

    /**
     * Decode span batch.
     *
     * @param buf the span batch encoded bytes
     * @param blockTime the block time
     * @param l2genesisTimestamp L2 genesis timestamp
     * @param l2ChainId the L2 chain id
     * @param l1InclusionBlock L1 inclusion block
     * @return the batch
     */
    public static Batch decodeSpanBatch(
            final byte[] buf,
            final BigInteger blockTime,
            final BigInteger l2genesisTimestamp,
            final BigInteger l2ChainId,
            final BigInteger l1InclusionBlock) {
        final RawSpanBatch rawSpanBatch = new RawSpanBatch();
        rawSpanBatch.decode(Unpooled.wrappedBuffer(buf));
        final SpanBatch spanBatch = rawSpanBatch.toSpanBatch(blockTime, l2genesisTimestamp, l2ChainId);
        return new Batch(spanBatch, l1InclusionBlock);
    }
}
