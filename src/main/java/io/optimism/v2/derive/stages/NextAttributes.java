package io.optimism.v2.derive.stages;

import io.optimism.v2.derive.types.L2BlockRef;
import io.optimism.v2.derive.types.OpAttributesWithParent;

/**
 * the next attributes interface.
 *
 * @author thinkAfCod
 * @since 0.4.6
 */
public interface NextAttributes {

    /**
     * get the next payload attributes with the parent block info.
     *
     * @param parent the parent block info of next payload
     * @return the next payload attributes with the parent block info
     */
    OpAttributesWithParent nextAttr(L2BlockRef parent);
}
