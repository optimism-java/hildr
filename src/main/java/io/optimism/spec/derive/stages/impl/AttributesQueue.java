package io.optimism.spec.derive.stages.impl;

import io.optimism.config.Config;
import io.optimism.spec.derive.stages.AttributesBuilder;
import io.optimism.spec.derive.stages.NextAttributes;
import io.optimism.spec.derive.stages.OriginAdvancer;
import io.optimism.spec.derive.stages.OriginProvider;
import io.optimism.spec.derive.stages.ResettableStage;
import io.optimism.spec.derive.types.Epoch;
import io.optimism.spec.derive.types.L2BlockRef;
import io.optimism.spec.derive.types.OpAttributesWithParent;
import io.optimism.spec.derive.types.OpPayloadAttributes;
import io.optimism.types.BlockInfo;

public class AttributesQueue implements NextAttributes, OriginAdvancer, OriginProvider, ResettableStage, AttributesBuilder {
  @Override
  public OpPayloadAttributes preparePayloadAttr(L2BlockRef ref, Epoch epoch) {
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

  @Override
  public OpAttributesWithParent nextAttr(L2BlockRef parent) {
    return null;
  }
}
