package io.optimism.v2.derive.stages.impl;

import io.optimism.v2.derive.stages.ChannelReaderProvider;
import io.optimism.v2.derive.stages.OriginAdvancer;
import io.optimism.v2.derive.stages.OriginProvider;
import io.optimism.v2.derive.stages.ResettableStage;
import io.optimism.v2.derive.types.BlockInfo;
import io.optimism.v2.derive.types.SystemConfig;

public class ChannelBank implements ChannelReaderProvider, OriginProvider, OriginAdvancer, ResettableStage {
    @Override
    public byte[] nextData() {
        return new byte[0];
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
