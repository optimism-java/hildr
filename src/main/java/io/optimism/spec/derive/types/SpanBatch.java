package io.optimism.spec.derive.types;

import io.optimism.spec.derive.types.enums.BatchType;

import java.math.BigInteger;

public class SpanBatch implements Batch{
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
