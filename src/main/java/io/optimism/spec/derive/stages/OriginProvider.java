package io.optimism.spec.derive.stages;

import io.optimism.types.BlockInfo;

public interface OriginProvider {

    BlockInfo origin();
}
