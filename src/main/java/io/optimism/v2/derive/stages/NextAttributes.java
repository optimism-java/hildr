package io.optimism.v2.derive.stages;

import io.optimism.v2.derive.types.L2BlockRef;
import io.optimism.v2.derive.types.OpAttributesWithParent;

public interface NextAttributes {

    OpAttributesWithParent nextAttr(L2BlockRef parent);
}
