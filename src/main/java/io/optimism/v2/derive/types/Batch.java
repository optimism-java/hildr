package io.optimism.v2.derive.types;

import io.optimism.v2.derive.types.enums.BatchType;
import java.math.BigInteger;

/**
 * The Batch interface.
 *
 * @author thinkAfCod
 * @since 0.4.5
 */
public interface Batch {

    BatchType type();

    BigInteger getTimestamp();

    byte[] encode();
}
