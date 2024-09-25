package io.optimism.spec.derive.stages;

import io.optimism.config.Config;
import io.optimism.types.BlockInfo;

public interface ResettableStage {

    void reset(BlockInfo base, Config.SystemConfig config);
}
