package io.optimism.v2.derive.datasource;

import io.optimism.types.BlockInfo;
import io.optimism.v2.derive.stages.DataIter;

public interface DataAvailabilityProvider {

    DataIter openData(BlockInfo l1Ref, String batcherAddr);
}
