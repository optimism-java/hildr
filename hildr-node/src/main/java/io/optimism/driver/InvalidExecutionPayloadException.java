package io.optimism.driver;

/**
 * The type InvalidExecutionPayloadException.
 *
 * @author grapebaba
 * @since 0.1.0
 */
public class InvalidExecutionPayloadException extends RuntimeException {

    /** Instantiates a new Invalid execution payload exception. */
    public InvalidExecutionPayloadException() {
        super("invalid execution payload");
    }

    /**
     * Instantiates a new Invalid execution payload exception.
     *
     * @param message the message
     */
    public InvalidExecutionPayloadException(String message) {
        super(message);
    }
}
