package io.optimism.exceptions;

/**
 * The sequencer reset exception.
 * @author thinkAfCod
 * @since 0.4.1
 */
public class ResetException extends SequencerException {

    /**
     * Instantiates a new sequencer reset exception.
     *
     * @param message the message
     */
    public ResetException(String message) {
        super(message);
    }

    /**
     * Instantiates a new sequencer reset exception.
     *
     * @param message the message
     * @param cause the cause
     */
    public ResetException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new sequencer reset exception.
     *
     * @param cause the cause
     */
    public ResetException(Throwable cause) {
        super(cause);
    }
}
