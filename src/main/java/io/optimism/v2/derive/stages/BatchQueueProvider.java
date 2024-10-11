package io.optimism.v2.derive.stages;

import io.optimism.v2.derive.types.Batch;

public interface BatchQueueProvider {

    Batch nextBatch();

    void flush();
}
