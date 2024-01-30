package io.optimism.batcher.loader;

/**
 * BlockLoaderException class. Throws this when occurs error while executing BlockLoader.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public class BlockLoaderException extends RuntimeException {

    /**
     * Instantiates a new BlockLoaderException.
     *
     * @param message the message
     */
    public BlockLoaderException(String message) {
        super(message);
    }

    /**
     * Instantiates a new BlockLoaderException.
     *
     * @param message the message
     * @param cause the cause
     */
    public BlockLoaderException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new BlockLoaderException.
     *
     * @param cause the cause
     */
    public BlockLoaderException(Throwable cause) {
        super(cause);
    }
}
