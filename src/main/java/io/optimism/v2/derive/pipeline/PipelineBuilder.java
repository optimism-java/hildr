package io.optimism.v2.derive.pipeline;

/**
 * the derivation pipeline builder.
 *
 * @author thinkAfCod
 * @since 0.4.6
 */
public class PipelineBuilder {

    /**
     * the PipelineBuilder constructor.
     */
    public PipelineBuilder() {}

    /**
     * builds the derivation pipeline.
     *
     * @return the derivation pipeline
     */
    public DerivationPipeline build() {
        return new DerivationPipeline();
    }
}
