package io.optimism.utilities.derive.stages;

import static io.optimism.utilities.derive.stages.SpanBatchUtils.putVarLong;

import com.google.common.primitives.Longs;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.PooledByteBufAllocator;
import java.math.BigInteger;
import java.util.Objects;
import org.apache.tuweni.bytes.Bytes;
import org.web3j.utils.Numeric;

/**
 * The type SpanBatchPrefix.
 *
 * @author grapebaba
 * @since 0.2.4
 */
public class SpanBatchPrefix {
    private BigInteger relTimestamp;
    private BigInteger l1Origin;
    private Bytes parentCheck;
    private Bytes l1OriginCheck;

    /**
     * Instantiates a new Span batch prefix.
     *
     * @param relTimestamp  the rel timestamp
     * @param l1Origin      the l 1 origin
     * @param parentCheck   the parent check
     * @param l1OriginCheck the l 1 origin check
     */
    public SpanBatchPrefix(BigInteger relTimestamp, BigInteger l1Origin, Bytes parentCheck, Bytes l1OriginCheck) {
        this.relTimestamp = relTimestamp;
        this.l1Origin = l1Origin;
        this.parentCheck = parentCheck;
        this.l1OriginCheck = l1OriginCheck;
    }

    /**
     * Rel timestamp big integer.
     *
     * @return the big integer
     */
    public BigInteger relTimestamp() {
        return relTimestamp;
    }

    /**
     * L 1 origin big integer.
     *
     * @return the big integer
     */
    public BigInteger l1Origin() {
        return l1Origin;
    }

    /**
     * Parent check bytes.
     *
     * @return the bytes
     */
    public Bytes parentCheck() {
        return parentCheck;
    }

    /**
     * L 1 origin check bytes.
     *
     * @return the bytes
     */
    public Bytes l1OriginCheck() {
        return l1OriginCheck;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SpanBatchPrefix that)) return false;
        return Objects.equals(relTimestamp, that.relTimestamp)
                && Objects.equals(l1Origin, that.l1Origin)
                && Objects.equals(parentCheck, that.parentCheck)
                && Objects.equals(l1OriginCheck, that.l1OriginCheck);
    }

    @Override
    public int hashCode() {
        return Objects.hash(relTimestamp, l1Origin, parentCheck, l1OriginCheck);
    }

    @Override
    public String toString() {
        return "SpanBatchPrefix[relTimestamp=%s, l1Origin=%s, parentCheck=%s, l1OriginCheck=%s]"
                .formatted(relTimestamp, l1Origin, parentCheck, l1OriginCheck);
    }

    /**
     * Decode rel timestamp.
     *
     * @param source the source
     */
    public void decodeRelTimestamp(ByteBuf source) {
        this.relTimestamp = Numeric.toBigInt(Longs.toByteArray(SpanBatchUtils.getVarLong(source)));
    }

    /**
     * Encode rel timestamp byte [ ].
     *
     * @return the byte [ ]
     */
    public byte[] encodeRelTimestamp() {
        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer(10);
        putVarLong(this.relTimestamp.longValue(), buffer);

        return ByteBufUtil.getBytes(buffer);
    }

    /**
     * Decode l 1 origin num.
     *
     * @param source the source
     */
    public void decodeL1OriginNum(ByteBuf source) {
        this.l1Origin = Numeric.toBigInt(Longs.toByteArray(SpanBatchUtils.getVarLong(source)));
    }

    /**
     * Encode l 1 origin num byte [ ].
     *
     * @return the byte [ ]
     */
    public byte[] encodeL1OriginNum() {
        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer(10);
        putVarLong(this.l1Origin.longValue(), buffer);

        return ByteBufUtil.getBytes(buffer);
    }

    /**
     * Decode parent check.
     *
     * @param source the source
     */
    public void decodeParentCheck(ByteBuf source) {
        this.parentCheck = Bytes.wrapByteBuf(source.readBytes(20));
    }

    /**
     * Encode parent check byte [ ].
     *
     * @return the byte [ ]
     */
    public byte[] encodeParentCheck() {
        return this.parentCheck.toArrayUnsafe();
    }

    /**
     * Decode l 1 origin check.
     *
     * @param source the source
     */
    public void decodeL1OriginCheck(ByteBuf source) {
        this.l1OriginCheck = Bytes.wrapByteBuf(source.readBytes(20));
    }

    /**
     * Encode l 1 origin check byte [ ].
     *
     * @return the byte [ ]
     */
    public byte[] encodeL1OriginCheck() {
        return this.l1OriginCheck.toArrayUnsafe();
    }

    /**
     * Decode.
     *
     * @param source the source
     */
    public void decode(ByteBuf source) {
        decodeRelTimestamp(source);
        decodeL1OriginNum(source);
        decodeParentCheck(source);
        decodeL1OriginCheck(source);
    }

    /**
     * Encode byte [ ].
     *
     * @return the byte [ ]
     */
    public byte[] encode() {
        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer();
        buffer.writeBytes(encodeRelTimestamp());
        buffer.writeBytes(encodeL1OriginNum());
        buffer.writeBytes(encodeParentCheck());
        buffer.writeBytes(encodeL1OriginCheck());

        return ByteBufUtil.getBytes(buffer);
    }
}
