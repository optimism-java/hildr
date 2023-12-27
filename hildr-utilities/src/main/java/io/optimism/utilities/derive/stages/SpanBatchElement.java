package io.optimism.utilities.derive.stages;

import java.math.BigInteger;
import java.util.List;

public record SpanBatchElement(BigInteger epochNum, BigInteger timestamp, List<String> transactions) {

    public static SpanBatchElement singularBatchToElement(SingularBatch singularBatch) {
        return new SpanBatchElement(singularBatch.epochNum(), singularBatch.timestamp(), singularBatch.transactions());
    }
}
