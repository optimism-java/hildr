package io.optimism.v2.derive.types;

import io.optimism.v2.derive.types.enums.BatchType;
import java.math.BigInteger;

public interface Batch {

    BatchType type();

    BigInteger getTimestamp();

    byte[] encode();
}
