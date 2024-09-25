package io.optimism.spec.derive.stages;

import io.optimism.spec.derive.types.Batch;

public interface BatchQueueProvider {

  Batch nextBatch();

  void flush();

}
