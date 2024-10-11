package io.optimism.v2.derive.stages.impl;

import io.optimism.v2.derive.stages.BatchQueueProvider;
import io.optimism.v2.derive.stages.OriginAdvancer;
import io.optimism.v2.derive.stages.OriginProvider;
import io.optimism.v2.derive.stages.ResettableStage;
import io.optimism.v2.derive.types.Batch;
import io.optimism.v2.derive.types.BlockInfo;
import io.optimism.v2.derive.types.SystemConfig;

public class ChannelReader implements BatchQueueProvider, OriginProvider, OriginAdvancer, ResettableStage {
    @Override
    public Batch nextBatch() {
        return null;
    }

    @Override
    public void flush() {}

    @Override
    public void advanceOrigin() {}

    @Override
    public BlockInfo origin() {
        return null;
    }

    @Override
    public void reset(BlockInfo base, SystemConfig config) {}
}
