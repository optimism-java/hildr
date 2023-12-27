package io.optimism.utilities.derive.stages;

import java.math.BigInteger;

public record SpanBatchSignature(BigInteger v, BigInteger r, BigInteger s) {}
