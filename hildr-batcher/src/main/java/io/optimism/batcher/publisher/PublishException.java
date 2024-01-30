package io.optimism.batcher.publisher;

/**
 * PublishException class. Throws this when publish data to L1.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public class PublishException extends RuntimeException {

    /**
     * Instantiates a new PublishException.
     *
     * @param message the message
     */
    public PublishException(String message) {
        super(message);
    }

    /**
     * Instantiates a new PublishException.
     *
     * @param message the message
     * @param cause the cause
     */
    public PublishException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new PublishException.
     *
     * @param cause the cause
     */
    public PublishException(Throwable cause) {
        super(cause);
    }
}
