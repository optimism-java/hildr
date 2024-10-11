package io.optimism.v2.derive.stages.impl;

import io.optimism.types.L2BlockRef;
import io.optimism.types.SingularBatch;
import io.optimism.v2.derive.stages.AttributesProvider;
import io.optimism.v2.derive.stages.OriginAdvancer;
import io.optimism.v2.derive.stages.OriginProvider;
import io.optimism.v2.derive.stages.ResettableStage;
import io.optimism.v2.derive.types.BlockInfo;
import io.optimism.v2.derive.types.SystemConfig;

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
    public void advanceOrigin() {}

    @Override
    public BlockInfo origin() {
        return null;
    }

    @Override
    public void reset(BlockInfo base, SystemConfig config) {}
}
