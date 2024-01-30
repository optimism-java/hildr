package io.optimism.driver;

/**
 * The type L1AttributesDepositedTxNotFoundException.
 *
 * @author grapebaba
 * @since 0.1.0
 */
public class L1AttributesDepositedTxNotFoundException extends RuntimeException {

    /** Instantiates a new L 1 attributes deposited tx not found exception. */
    public L1AttributesDepositedTxNotFoundException() {
        super("Could not find the L1 attributes deposited transaction");
    }
}
