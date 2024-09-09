package io.optimism.driver;

/**
 * The type PayloadIdNotReturnedException.
 *
 * @author grapebaba
 * @since 0.1.0
 */
public class PayloadIdNotReturnedException extends RuntimeException {

    /** Instantiates a new Payload id not returned exception. */
    public PayloadIdNotReturnedException() {
        super("engine did not return payload id");
    }
}
