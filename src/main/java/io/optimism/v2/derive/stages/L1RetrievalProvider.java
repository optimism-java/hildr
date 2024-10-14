package io.optimism.v2.derive.stages;

import io.optimism.v2.derive.types.BlockInfo;

/**
 * the l1 retrieval provider interface.
 *
 * @author thinkAfCod
 * @since 0.4.6
 */
public interface L1RetrievalProvider {
    /**
     * get the next L1 block info.
     *
     * @return the next L1 block info
     */
    BlockInfo nextL1Block();

    /**
     * get the current batcher address.
     *
     * @return the batcher address
     */
    String batcherAddr();
}
