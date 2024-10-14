package io.optimism.v2.derive.stages;

/**
 * The frame queue provider interface.
 *
 * @author thinkAfCod
 * @since 0.4.6
 */
public interface FrameQueueProvider {
    /**
     * gets the bytes of the next raw frame.
     *
     * @return the bytes of the next raw frame
     */
    byte[] next();
}
