package io.optimism.v2.derive.exception;

/**
 * Exception thrown when there is an error in the pipeline.
 */
public class PipelineEofException extends RuntimeException {
    /** Constructs a PipelineEofException. */
    public PipelineEofException() {
        super("pipeline EOF error");
    }

    /**
     * Constructs a PipelineEofException with a custom message.
     *
     * @param message the custom error message
     */
    public PipelineEofException(String message) {
        super(message);
    }

    /**
     * Constructs a new PipelineEofException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public PipelineEofException(String message, Throwable cause) {
        super(message, cause);
    }
}
