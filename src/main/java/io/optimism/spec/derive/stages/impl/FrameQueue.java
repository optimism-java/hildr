package io.optimism.spec.derive.stages.impl;

import io.optimism.config.Config;
import io.optimism.spec.derive.stages.ChannelBankProvider;
import io.optimism.spec.derive.stages.OriginAdvancer;
import io.optimism.spec.derive.stages.OriginProvider;
import io.optimism.spec.derive.stages.ResettableStage;
import io.optimism.spec.derive.types.Frame;
import io.optimism.types.BlockInfo;

public class FrameQueue implements ChannelBankProvider, OriginProvider, OriginAdvancer, ResettableStage {

  @Override
  public Frame nextFrame() {
    return null;
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
