package io.optimism.exceptions;

/**
 * The type HildrServiceExecutionException.
 *
 * @author grapebaba
 * @since 0.1.0
 */
public class HildrServiceExecutionException extends RuntimeException {

    /**
     * Instantiates a new Hildr service execution exception.
     *
     * @param message the message
     */
    public HildrServiceExecutionException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Hildr service execution exception.
     *
     * @param message the message
     * @param cause the cause
     */
    public HildrServiceExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new Hildr service execution exception.
     *
     * @param cause the cause
     */
    public HildrServiceExecutionException(Throwable cause) {
        super(cause);
    }
}
