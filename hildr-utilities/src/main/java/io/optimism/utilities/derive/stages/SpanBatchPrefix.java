package io.optimism.utilities.derive.stages;

import java.math.BigInteger;

public record SpanBatchPrefix(BigInteger relTimestamp, BigInteger l1Origin, String parentCheck, String l1OriginCheck) {}
