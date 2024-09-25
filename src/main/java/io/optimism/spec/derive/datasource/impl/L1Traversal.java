package io.optimism.spec.derive.datasource.impl;

import io.optimism.config.Config;
import io.optimism.spec.derive.stages.L1RetrievalProvider;
import io.optimism.spec.derive.stages.OriginAdvancer;
import io.optimism.spec.derive.stages.OriginProvider;
import io.optimism.spec.derive.stages.ResettableStage;
import io.optimism.types.BlockInfo;

public class L1Traversal implements L1RetrievalProvider, OriginProvider, OriginAdvancer, ResettableStage {

  public L1Traversal() {

  }


  @Override
  public BlockInfo nextL1Block() {
    return null;
  }

  @Override
  public String batcherAddr() {
    return "";
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
