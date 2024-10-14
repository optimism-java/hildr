package io.optimism.v2.derive.stages;

import io.optimism.v2.derive.types.Batch;

/**
 * the batch queue provider interface.
 *
 * @author thinkAfCod
 * @since 0.4.6
 */
public interface BatchQueueProvider {

    /**
     * gets the next batch in the queue.
     *
     * @return the next batch in the queue
     */
    Batch nextBatch();

    /**
     * flush the batch queue.
     */
    void flush();
}
