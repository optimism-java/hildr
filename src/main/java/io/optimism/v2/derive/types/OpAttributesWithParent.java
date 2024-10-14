package io.optimism.v2.derive.types;

/**
 * The OpAttributesWithParent class.
 *
 * @param attributes the attributes instance.
 * @param parent parent l2 block info.
 * @param isLastInSpan last in span flag.
 *
 * @author thinkAfCod
 * @since 0.4.5
 */
public record OpAttributesWithParent(OpPayloadAttributes attributes, L2BlockRef parent, Boolean isLastInSpan) {}
