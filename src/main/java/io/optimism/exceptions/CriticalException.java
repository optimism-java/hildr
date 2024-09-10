package io.optimism.exceptions;

/**
 * The sequencer critical exception.
 * @author thinkAfCod
 * @since 0.4.1
 */
public class CriticalException extends SequencerException {

    /**
     * Instantiates a new sequencer critical exception.
     *
     * @param message the message
     */
    public CriticalException(String message) {
        super(message);
    }

    /**
     * Instantiates a new sequencer critical exception.
     *
     * @param message the message
     * @param cause the cause
     */
    public CriticalException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new sequencer critical exception.
     *
     * @param cause the cause
     */
    public CriticalException(Throwable cause) {
        super(cause);
    }
}
