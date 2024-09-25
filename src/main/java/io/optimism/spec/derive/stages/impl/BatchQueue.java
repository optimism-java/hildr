package io.optimism.spec.derive.stages.impl;

import io.optimism.config.Config;
import io.optimism.spec.derive.stages.AttributesProvider;
import io.optimism.spec.derive.stages.OriginAdvancer;
import io.optimism.spec.derive.stages.OriginProvider;
import io.optimism.spec.derive.stages.ResettableStage;
import io.optimism.types.BlockInfo;
import io.optimism.types.L2BlockRef;
import io.optimism.types.SingularBatch;

public class BatchQueue implements AttributesProvider, OriginProvider, OriginAdvancer, ResettableStage {
  @Override
  public SingularBatch nextBatch(L2BlockRef parent) {
    return null;
  }

  @Override
  public boolean isLastInSpan() {
    return false;
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
