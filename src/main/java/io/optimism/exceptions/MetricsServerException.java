package io.optimism.exceptions;

/**
 * MetricsServerException class.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public class MetricsServerException extends RuntimeException {

    /**
     * Instantiates a new MetricsServerException.
     *
     * @param message the message
     */
    public MetricsServerException(String message) {
        super(message);
    }

    /**
     * Instantiates a new MetricsServerException.
     *
     * @param message the message
     * @param cause the cause
     */
    public MetricsServerException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new MetricsServerException.
     *
     * @param cause the cause
     */
    public MetricsServerException(Throwable cause) {
        super(cause);
    }
}
