package io.optimism.v2.derive.exception;

/**
 * Exception thrown when there is an error in the pipeline provider.
 */
public class PipelineProviderException extends RuntimeException {

    public PipelineProviderException() {
        super("pipeline provider error");
    }

    /**
     * Constructs a new PipelineProviderException with the specified detail message.
     *
     * @param message the detail message
     */
    public PipelineProviderException(String message) {
        super(message);
    }

    /**
     * Constructs a new PipelineProviderException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public PipelineProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
