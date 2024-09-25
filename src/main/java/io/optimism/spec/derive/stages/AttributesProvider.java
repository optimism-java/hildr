package io.optimism.spec.derive.stages;

import io.optimism.types.L2BlockRef;
import io.optimism.types.SingularBatch;

public interface AttributesProvider {
    /**
     * returns the next valid batch upon the given safe head.
     * @param parent
     * @return
     */
    SingularBatch nextBatch(L2BlockRef parent);

    /**
     *
     * @return
     */
    boolean isLastInSpan();
}
