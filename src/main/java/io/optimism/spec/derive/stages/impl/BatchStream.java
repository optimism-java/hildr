package io.optimism.spec.derive.stages.impl;

import io.optimism.config.Config;
import io.optimism.spec.derive.stages.BatchQueueProvider;
import io.optimism.spec.derive.stages.OriginAdvancer;
import io.optimism.spec.derive.stages.OriginProvider;
import io.optimism.spec.derive.stages.ResettableStage;
import io.optimism.spec.derive.types.Batch;
import io.optimism.types.BlockInfo;

public class BatchStream implements BatchQueueProvider, OriginProvider, OriginAdvancer, ResettableStage {
  @Override
  public Batch nextBatch() {
    return null;
  }

  @Override
  public void flush() {

  }

  @Override
  public void advanceOrigin() {

  }

  @Override
  public BlockInfo origin() {
    return null;
  }

  @Override
  public void reset(BlockInfo base, Config.SystemConfig config) {

  }
}
