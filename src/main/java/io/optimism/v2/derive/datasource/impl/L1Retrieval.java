package io.optimism.v2.derive.datasource.impl;

import io.optimism.v2.derive.datasource.DataAvailabilityProvider;
import io.optimism.v2.derive.exception.PipelineEofException;
import io.optimism.v2.derive.stages.DataIter;
import io.optimism.v2.derive.stages.FrameQueueProvider;
import io.optimism.v2.derive.stages.L1RetrievalProvider;
import io.optimism.v2.derive.stages.OriginAdvancer;
import io.optimism.v2.derive.stages.OriginProvider;
import io.optimism.v2.derive.stages.ResettableStage;
import io.optimism.v2.derive.types.BlockInfo;
import io.optimism.v2.derive.types.SystemConfig;
import java.util.Optional;

/**
 * the l1 chain data retrieval.
 *
 * @author thinkAfCod
 * @since 0.4.6
 */
public class L1Retrieval implements FrameQueueProvider {

    private final L1RetrievalProvider prev;

    private final DataAvailabilityProvider provider;

    private Optional<DataIter> data;

    /**
     * L1Retrieval constructor.
     *
     * @param prev the previous stage
     * @param provider the data availability provider
     */
    public L1Retrieval(L1RetrievalProvider prev, DataAvailabilityProvider provider) {
        this.prev = prev;
        this.provider = provider;
        this.data = Optional.empty();
    }

    @Override
    public byte[] next() {
        if (data.isEmpty()) {
            var next = this.prev.nextL1Block();
            this.data = Optional.ofNullable(this.provider.openData(next));
        }
        if (this.data.isEmpty()) {
            throw new PipelineEofException("");
        }

        return this.data.get().next();
    }

    @Override
    public void advanceOrigin() {
        ((OriginAdvancer) this.prev).advanceOrigin();
    }

    @Override
    public BlockInfo origin() {
        return ((OriginProvider) this.prev).origin();
    }

    @Override
    public void reset(BlockInfo base, SystemConfig config) {
        ((ResettableStage) this.prev).reset(base, config);
        this.data = Optional.ofNullable(this.provider.openData(base));
    }
}
