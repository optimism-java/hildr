package io.optimism.spec.derive.stages;

import io.optimism.types.BlockInfo;

public interface L1RetrievalProvider {

  BlockInfo nextL1Block();

  String batcherAddr();

}
