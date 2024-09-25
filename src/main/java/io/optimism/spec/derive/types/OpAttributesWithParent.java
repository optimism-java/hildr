package io.optimism.spec.derive.types;

public record OpAttributesWithParent(
    OpPayloadAttributes attributes,
    L2BlockRef parent,
    Boolean isLastInSpan) {}
