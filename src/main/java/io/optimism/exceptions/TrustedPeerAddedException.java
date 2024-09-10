package io.optimism.exceptions;

/**
 * The type TrustedPeerAddedException.
 *
 * @author grapebaba
 * @since 0.1.0
 */
public class TrustedPeerAddedException extends RuntimeException {

    /** Instantiates a new Trusted peer added exception. */
    public TrustedPeerAddedException() {
        super("Trusted peer added");
    }

    /**
     * Instantiates a new Trusted peer added exception.
     *
     * @param message the message
     */
    public TrustedPeerAddedException(String message) {
        super(message);
    }
}
