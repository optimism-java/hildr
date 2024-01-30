package io.optimism.utilities.derive.stages;

import com.google.common.primitives.Bytes;
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
    public BigInteger timestamp(BigInteger l2genesisTimestamp) {
        return batch.getTimestamp(l2genesisTimestamp);
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
        } else if (batch instanceof RawSpanBatch) {
            var typedBatch = (RawSpanBatch) batch;
            return Bytes.concat(
                    typedBatch.spanbatchPrefix().encode(),
                    typedBatch.spanbatchPayload().encode());
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
     * @param l1InclusionBlock L1 inclusion block
     * @return the batch
     */
    public static Batch decodeRawSpanBatch(final byte[] buf, final BigInteger l1InclusionBlock) {
        final RawSpanBatch rawSpanBatch = new RawSpanBatch();
        rawSpanBatch.decode(Unpooled.wrappedBuffer(buf));
        return new Batch(rawSpanBatch, l1InclusionBlock);
    }
}
