package io.optimism.spec.derive.types;

import io.optimism.spec.derive.types.enums.BatchType;

import java.math.BigInteger;

public interface Batch {

  BatchType type();

  BigInteger getTimestamp();

  byte[] encode();




}
