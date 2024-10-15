package io.optimism.v2.derive.stages;

import io.optimism.v2.derive.types.Frame;

/**
 * the channel bank provider interface.
 *
 * @author thinkAfCod
 * @since 0.4.6
 */
public interface ChannelBankProvider extends OriginProvider, OriginAdvancer, ResettableStage {
    /**
     * gets the next frame in the current channel
     *
     * @return the next frame in the current channel
     */
    Frame nextFrame();
}
