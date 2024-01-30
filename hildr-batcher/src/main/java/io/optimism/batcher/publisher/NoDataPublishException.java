package io.optimism.batcher.publisher;

/**
 * NoDataPublishException class. Throws this when no available data publish to L1.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public class NoDataPublishException extends RuntimeException {

    /**
     * Instantiates a new NoDataPublishException.
     *
     * @param message the message
     */
    public NoDataPublishException(String message) {
        super(message);
    }

    /**
     * Instantiates a new NoDataPublishException.
     *
     * @param message the message
     * @param cause the cause
     */
    public NoDataPublishException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new NoDataPublishException.
     *
     * @param cause the cause
     */
    public NoDataPublishException(Throwable cause) {
        super(cause);
    }
}
