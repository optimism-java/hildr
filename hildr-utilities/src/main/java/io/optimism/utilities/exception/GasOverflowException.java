package io.optimism.utilities.exception;

/**
 * GasOverflowException class.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public class GasOverflowException extends RuntimeException {

    /**
     * Instantiates a new GasOverflowException.
     *
     * @param message the message
     */
    public GasOverflowException(String message) {
        super(message);
    }

    /**
     * Instantiates a new GasOverflowException.
     *
     * @param message the message
     * @param cause the cause
     */
    public GasOverflowException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new GasOverflowException.
     *
     * @param cause the cause
     */
    public GasOverflowException(Throwable cause) {
        super(cause);
    }
}
