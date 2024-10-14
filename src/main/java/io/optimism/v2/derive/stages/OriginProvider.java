package io.optimism.v2.derive.stages;

import io.optimism.v2.derive.types.BlockInfo;

/**
 * The origin provider interface.
 *
 * @author thinkAfCod
 * @since 0.4.6
 */
public interface OriginProvider {

    /**
     * get the current l1 origin info.
     * @return the current l1 origin info
     */
    BlockInfo origin();
}
