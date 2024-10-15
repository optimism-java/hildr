package io.optimism.v2.derive.exception;

/**
 * The frame parse exception.
 *
 * @author thinkAfCod
 * @since 0.4.6
 */
public class FrameParseException extends RuntimeException {
    /** Constructs a PipelineEofException. */
    public FrameParseException() {
        super("parses frame failed");
    }

    /**
     * Constructs a PipelineEofException with a custom message.
     *
     * @param message the custom error message
     */
    public FrameParseException(String message) {
        super(message);
    }

    /**
     * Constructs a new PipelineEofException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public FrameParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
