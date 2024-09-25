package io.optimism.spec.derive.datasource;

import io.optimism.spec.derive.stages.DataIter;
import io.optimism.types.BlockInfo;

public interface DataAvailabilityProvider {

  DataIter openData(BlockInfo l1Ref, String batcherAddr);

}
