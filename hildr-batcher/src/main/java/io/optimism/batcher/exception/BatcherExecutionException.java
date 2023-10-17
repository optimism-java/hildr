package io.optimism.batcher.exception;

/**
 * The bathcer execution exception.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public class BatcherExecutionException extends RuntimeException {

    /**
     * Instantiates a new Hildr Batcher execution exception.
     *
     * @param message the message
     */
    public BatcherExecutionException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Hildr Batcher execution exception.
     *
     * @param message the message
     * @param cause the cause
     */
    public BatcherExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new Hildr Batcher execution exception.
     *
     * @param cause the cause
     */
    public BatcherExecutionException(Throwable cause) {
        super(cause);
    }
}
