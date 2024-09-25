package io.optimism.spec.derive.stages;

import io.optimism.spec.derive.types.L2BlockRef;
import io.optimism.spec.derive.types.OpAttributesWithParent;

public interface NextAttributes {

  OpAttributesWithParent nextAttr(L2BlockRef parent);

}
