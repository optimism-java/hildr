package io.optimism.exceptions;

/**
 * The type DepositsNotFoundException.
 *
 * @author grapebaba
 * @since 0.1.0
 */
public class DepositsNotFoundException extends RuntimeException {

    /** Instantiates a new Deposits not found exception. */
    public DepositsNotFoundException() {
        super("deposits not found");
    }

    /**
     * Instantiates a new Deposits not found exception.
     *
     * @param message exception message info
     */
    public DepositsNotFoundException(String message) {
        super(message);
    }
}
