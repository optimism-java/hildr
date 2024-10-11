package io.optimism.v2.derive.stages;

import io.optimism.v2.derive.types.BlockInfo;

public interface L1RetrievalProvider {

    BlockInfo nextL1Block();

    String batcherAddr();
}
