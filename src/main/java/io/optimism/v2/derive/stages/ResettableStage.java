package io.optimism.v2.derive.stages;

import io.optimism.v2.derive.types.BlockInfo;
import io.optimism.v2.derive.types.SystemConfig;

/**
 * the interface Resettable stage.
 *
 * @author thinkAfCod
 * @since 0.4.6
 */
public interface ResettableStage {

    void reset(BlockInfo base, SystemConfig config);
}
