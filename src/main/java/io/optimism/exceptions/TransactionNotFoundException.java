package io.optimism.exceptions;

/**
 * The type TransactionNotFoundException.
 *
 * @author grapebaba
 * @since 0.1.0
 */
public class TransactionNotFoundException extends RuntimeException {

    /** Instantiates a new Transaction not found exception. */
    public TransactionNotFoundException() {
        super("Transaction not found");
    }

    /**
     * Instantiates a new Transaction not found exception.
     *
     * @param message the message
     */
    public TransactionNotFoundException(String message) {
        super(message);
    }
}
