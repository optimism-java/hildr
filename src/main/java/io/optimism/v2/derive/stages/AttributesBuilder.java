package io.optimism.v2.derive.stages;

import io.optimism.v2.derive.types.BlockInfo;
import io.optimism.v2.derive.types.L2BlockRef;
import io.optimism.v2.derive.types.OpPayloadAttributes;

/**
 * the interface Attributes builder.
 *
 * @author thinkAfCod
 * @since 0.4.6
 */
public interface AttributesBuilder {

    /**
     * prepare next payload attributes.
     *
     * @param parent the parent block info of next payload
     * @param l1Epoch the l1 inclusion block info of next payload
     * @return the next payload attributes
     */
    OpPayloadAttributes preparePayloadAttr(L2BlockRef parent, BlockInfo l1Epoch);
}
