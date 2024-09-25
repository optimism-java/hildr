package io.optimism.spec.derive.stages.impl;

import io.optimism.config.Config;
import io.optimism.spec.derive.stages.ChannelReaderProvider;
import io.optimism.spec.derive.stages.OriginAdvancer;
import io.optimism.spec.derive.stages.OriginProvider;
import io.optimism.spec.derive.stages.ResettableStage;
import io.optimism.types.BlockInfo;

public class ChannelBank implements ChannelReaderProvider, OriginProvider, OriginAdvancer, ResettableStage {
  @Override
  public byte[] nextData() {
    return new byte[0];
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
