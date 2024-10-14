package io.optimism.v2.derive.datasource.impl;

import io.optimism.v2.derive.stages.FrameQueueProvider;
import io.optimism.v2.derive.stages.OriginAdvancer;
import io.optimism.v2.derive.stages.OriginProvider;
import io.optimism.v2.derive.stages.ResettableStage;
import io.optimism.v2.derive.types.BlockInfo;
import io.optimism.v2.derive.types.SystemConfig;

/**
 * the l1 chain data retrieval.
 *
 * @author thinkAfCod
 * @since 0.4.6
 */
public class L1Retrieval implements FrameQueueProvider, OriginProvider, OriginAdvancer, ResettableStage {

    @Override
    public void advanceOrigin() {}

    @Override
    public BlockInfo origin() {
        return null;
    }

    @Override
    public void reset(BlockInfo base, SystemConfig config) {}

    @Override
    public byte[] next() {
        return new byte[0];
    }
}
