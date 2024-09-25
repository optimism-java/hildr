 package io.optimism.spec.derive.stages;

 import io.optimism.spec.derive.types.L2BlockRef;
 import io.optimism.spec.derive.types.OpPayloadAttributes;
 import io.optimism.spec.derive.types.Epoch;

 public interface AttributesBuilder {

  OpPayloadAttributes preparePayloadAttr(L2BlockRef ref, Epoch epoch);

 }
