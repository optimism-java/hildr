package io.optimism.derive;

/**
 * The interface PurgeableIterator is an iterator that can purge itself.
 *
 * @param <E> the type parameter
 * @author grapebaba
 * @since 0.1.0
 */
public interface PurgeableIterator<E> {

    /** Purge. */
    void purge();

    /**
     * Next e.
     *
     * @return the e
     */
    E next();
}
