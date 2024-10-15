package io.optimism.v2.derive.stages;

/**
 * the data iterator interface.
 *
 * @author thinkAfCod
 * @since 0.4.6
 */
public interface DataIter {

    /**
     * get the next data.
     * @return the bytes of next data.
     */
    byte[] next();
}
