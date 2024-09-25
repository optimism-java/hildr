package io.optimism.spec.derive.datasource.impl;

import io.optimism.config.Config;
import io.optimism.spec.derive.stages.FrameQueueProvider;
import io.optimism.spec.derive.stages.OriginAdvancer;
import io.optimism.spec.derive.stages.OriginProvider;
import io.optimism.spec.derive.stages.ResettableStage;
import io.optimism.types.BlockInfo;

public class L1Retrieval implements FrameQueueProvider, OriginProvider, OriginAdvancer, ResettableStage {

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

  @Override
  public byte[] next() {
    return new byte[0];
  }
}
