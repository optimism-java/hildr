package io.optimism.exceptions;

/**
 * The sequencer temporary exception.
 * @author thinkAfCod
 * @since 0.4.1
 */
public class TemporaryException extends SequencerException {

    /**
     * Instantiates a new sequencer temporary exception.
     *
     * @param message the message
     */
    public TemporaryException(String message) {
        super(message);
    }

    /**
     * Instantiates a new sequencer temporary exception.
     *
     * @param message the message
     * @param cause the cause
     */
    public TemporaryException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new sequencer temporary exception.
     *
     * @param cause the cause
     */
    public TemporaryException(Throwable cause) {
        super(cause);
    }
}
