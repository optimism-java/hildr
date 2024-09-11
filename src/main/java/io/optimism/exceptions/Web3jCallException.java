package io.optimism.exceptions;

/**
 * Web3jCallException class. Throws it when the call of web3j request task failed.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public class Web3jCallException extends RuntimeException {

    /**
     * Instantiates a new Web3jCallException.
     *
     * @param message the message
     */
    public Web3jCallException(String message) {
        super(message);
    }

    /**
     * Instantiates a new Web3jCallException.
     *
     * @param message the message
     * @param cause the cause
     */
    public Web3jCallException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new Web3jCallException.
     *
     * @param cause the cause
     */
    public Web3jCallException(Throwable cause) {
        super(cause);
    }
}
