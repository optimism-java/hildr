package io.optimism.utilities.derive.stages;

import java.math.BigInteger;
import java.util.List;

/**
 * The type SpanBatchElement.
 *
 * @param epochNum     the epoch num
 * @param timestamp    the timestamp
 * @param transactions the transactions
 * @author grapebaba
 * @since 0.2.4
 */
public record SpanBatchElement(BigInteger epochNum, BigInteger timestamp, List<String> transactions) {

    /**
     * Singular batch to element span batch element.
     *
     * @param singularBatch the singular batch
     * @return the span batch element
     */
    public static SpanBatchElement singularBatchToElement(SingularBatch singularBatch) {
        return new SpanBatchElement(singularBatch.epochNum(), singularBatch.timestamp(), singularBatch.transactions());
    }
}
