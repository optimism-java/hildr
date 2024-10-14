package io.optimism.v2.derive.stages;

/**
 * The channel reader provider interface.
 *
 * @author thinkAfCod
 * @since 0.4.6
 */
public interface ChannelReaderProvider {

    /**
     * get the bytes of the next raw batch.
     *
     * @return the bytes of the next raw batch
     */
    byte[] nextData();
}
