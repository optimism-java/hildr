package io.optimism.proposer.exception;

/**
 * output submitter execution exception.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public class OutputSubmitterExecution extends RuntimeException {

    /**
     * Instantiates a new output execution exception.
     *
     * @param message the message
     */
    public OutputSubmitterExecution(String message) {
        super(message);
    }

    /**
     * Instantiates a new output execution exception.
     *
     * @param message the message
     * @param cause the cause
     */
    public OutputSubmitterExecution(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new output execution exception.
     *
     * @param cause the cause
     */
    public OutputSubmitterExecution(Throwable cause) {
        super(cause);
    }
}
