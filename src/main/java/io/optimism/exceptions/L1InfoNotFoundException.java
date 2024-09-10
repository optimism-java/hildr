package io.optimism.exceptions;

/**
 * The type L1InfoNotFoundException.
 *
 * @author grapebaba
 * @since 0.1.0
 */
public class L1InfoNotFoundException extends RuntimeException {

    /** Instantiates a new L1 info not found exception. */
    public L1InfoNotFoundException() {
        super("L1 info not found");
    }
}
