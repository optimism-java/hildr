package io.optimism.v2.derive.stages;

import io.optimism.v2.derive.types.BlockInfo;
import io.optimism.v2.derive.types.SystemConfig;

public interface ResettableStage {

    void reset(BlockInfo base, SystemConfig config);
}
