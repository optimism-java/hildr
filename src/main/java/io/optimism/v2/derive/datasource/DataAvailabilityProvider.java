package io.optimism.v2.derive.datasource;

import io.optimism.v2.derive.stages.DataIter;
import io.optimism.v2.derive.types.BlockInfo;

/**
 * the data availability provider interface.
 *
 * @author thinkAfCod
 * @since 0.4.6
 */
public interface DataAvailabilityProvider {

    DataIter openData(BlockInfo l1Ref);
}
