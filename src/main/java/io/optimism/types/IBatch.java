package io.optimism.types;

import io.optimism.types.enums.BatchType;
import java.math.BigInteger;

/**
 * Batch contains information to build one or multiple L2 blocks.
 * Batcher converts L2 blocks into Batch and writes encoded bytes to Channel.
 * Derivation pipeline decodes Batch from Channel, and converts to one or multiple payload attributes.
 *
 * @author zhouop0
 * @since 0.1.0
 */
public interface IBatch {

    /**
     * Gets batch type.
     *
     * @return the batch type
     */
    BatchType getBatchType();

    /**
     * Gets timestamp.
     *
     * @return the timestamp
     */
    BigInteger getTimestamp();
}
