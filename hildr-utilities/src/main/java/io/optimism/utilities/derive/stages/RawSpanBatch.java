package io.optimism.utilities.derive.stages;

import io.netty.buffer.ByteBuf;
import java.math.BigInteger;
import java.util.Objects;

/**
 * The type RawSpanBatch.
 *
 * @author grapebaba
 * @since 0.2.4
 */
public class RawSpanBatch implements IBatch {
    private final SpanBatchPrefix spanbatchPrefix;
    private final SpanBatchPayload spanbatchPayload;

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

    @Override
    public BatchType getBatchType() {
        return BatchType.SPAN_BATCH_TYPE;
    }

    @Override
    public BigInteger getTimestamp() {
        return null;
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
}
