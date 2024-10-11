package io.optimism.v2.derive.stages.impl;

import io.optimism.v2.derive.stages.ChannelBankProvider;
import io.optimism.v2.derive.stages.OriginAdvancer;
import io.optimism.v2.derive.stages.OriginProvider;
import io.optimism.v2.derive.stages.ResettableStage;
import io.optimism.v2.derive.types.BlockInfo;
import io.optimism.v2.derive.types.Frame;
import io.optimism.v2.derive.types.SystemConfig;

public class FrameQueue implements ChannelBankProvider, OriginProvider, OriginAdvancer, ResettableStage {

    @Override
    public Frame nextFrame() {
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
}
