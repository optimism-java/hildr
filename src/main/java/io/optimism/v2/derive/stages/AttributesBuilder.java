package io.optimism.v2.derive.stages;

import io.optimism.v2.derive.types.Epoch;
import io.optimism.v2.derive.types.L2BlockRef;
import io.optimism.v2.derive.types.OpPayloadAttributes;

public interface AttributesBuilder {

    OpPayloadAttributes preparePayloadAttr(L2BlockRef ref, Epoch epoch);
}
