package io.optimism.v2.derive.stages.impl;

import io.optimism.v2.derive.stages.AttributesBuilder;
import io.optimism.v2.derive.stages.NextAttributes;
import io.optimism.v2.derive.stages.OriginAdvancer;
import io.optimism.v2.derive.stages.OriginProvider;
import io.optimism.v2.derive.stages.ResettableStage;
import io.optimism.v2.derive.types.BlockInfo;
import io.optimism.v2.derive.types.L2BlockRef;
import io.optimism.v2.derive.types.OpAttributesWithParent;
import io.optimism.v2.derive.types.OpPayloadAttributes;
import io.optimism.v2.derive.types.SystemConfig;

public class AttributesQueue
        implements NextAttributes, OriginAdvancer, OriginProvider, ResettableStage, AttributesBuilder {
    @Override
    public OpPayloadAttributes preparePayloadAttr(L2BlockRef ref, BlockInfo epoch) {
        return null;
    }

    @Override
    public void advanceOrigin() {}

    @Override
    public BlockInfo origin() {
        return null;
    }

    @Override
    public void reset(BlockInfo base, SystemConfig config) {}

    @Override
    public OpAttributesWithParent nextAttr(L2BlockRef parent) {
        return null;
    }
}
