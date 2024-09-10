package io.optimism.exceptions;

/**
 * the sequencer exception.
 * @author thinkAfCod
 * @since 0.4.1
 */
public class SequencerException extends RuntimeException {

    /**
     * Instantiates a new sequencer exception.
     *
     * @param message the message
     */
    public SequencerException(String message) {
        super(message);
    }

    /**
     * Instantiates a new sequencer exception.
     *
     * @param message the message
     * @param cause the cause
     */
    public SequencerException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new sequencer exception.
     *
     * @param cause the cause
     */
    public SequencerException(Throwable cause) {
        super(cause);
    }
}
