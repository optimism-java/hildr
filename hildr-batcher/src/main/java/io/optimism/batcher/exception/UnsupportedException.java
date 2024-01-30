package io.optimism.batcher.exception;

/**
 * UnsupportedException class. Throws This when field or operation not supported.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public class UnsupportedException extends RuntimeException {

    /**
     * Instantiates a new UnsupportedException.
     *
     * @param message the message
     */
    public UnsupportedException(String message) {
        super(message);
    }

    /**
     * Instantiates a new UnsupportedException.
     *
     * @param message the message
     * @param cause the cause
     */
    public UnsupportedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new UnsupportedException.
     *
     * @param cause the cause
     */
    public UnsupportedException(Throwable cause) {
        super(cause);
    }
}
