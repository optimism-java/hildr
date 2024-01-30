package io.optimism.batcher.channel;

/**
 * ReorgException class. Throws this when chain occurs reorg.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public class ReorgException extends RuntimeException {

    /**
     * Instantiates a new reorg exception.
     *
     * @param message the message
     */
    public ReorgException(String message) {
        super(message);
    }

    /**
     * Instantiates a new reorg exception.
     *
     * @param message the message
     * @param cause the cause
     */
    public ReorgException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new reorg exception.
     *
     * @param cause the cause
     */
    public ReorgException(Throwable cause) {
        super(cause);
    }
}
