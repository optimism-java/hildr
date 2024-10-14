package io.optimism.v2.derive.stages;

import io.optimism.types.L2BlockRef;
import io.optimism.types.SingularBatch;

/**
 * the attributes provider interface.
 */
public interface AttributesProvider {
    /**
     * returns the next valid batch upon the given safe head.
     *
     * @param parent
     * @return the next Singular batch
     */
    SingularBatch nextBatch(L2BlockRef parent);

    /**
     * returns if the batch on the current index is last in the span.
     *
     * @return true if the current batch is the last in the span, false otherwise
     */
    boolean isLastInSpan();
}
