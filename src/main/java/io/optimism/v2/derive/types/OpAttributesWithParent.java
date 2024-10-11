package io.optimism.v2.derive.types;

public record OpAttributesWithParent(OpPayloadAttributes attributes, L2BlockRef parent, Boolean isLastInSpan) {}
