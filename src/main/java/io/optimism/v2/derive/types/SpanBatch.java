package io.optimism.v2.derive.types;

import io.optimism.v2.derive.types.enums.BatchType;
import java.math.BigInteger;

/**
 * the SpanBatch class.
 *
 * @author thinkAfCod
 * @since 0.4.5
 */
public class SpanBatch implements Batch {
    @Override
    public BatchType type() {
        return null;
    }

    @Override
    public BigInteger getTimestamp() {
        return null;
    }

    @Override
    public byte[] encode() {
        return new byte[0];
    }
}
