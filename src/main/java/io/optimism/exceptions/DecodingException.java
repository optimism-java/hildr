package io.optimism.exceptions;

/**
 * The type DecodingException.
 *
 * @author grapebaba
 * @since 0.1.1
 */
public class DecodingException extends Exception {
    /**
     * Instantiates a new Decoding exception.
     *
     * @param message the message
     * @param cause the cause
     */
    public DecodingException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new Decoding exception.
     *
     * @param message the message
     */
    public DecodingException(final String message) {
        super(message);
    }
}
