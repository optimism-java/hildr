package io.optimism.types;

/**
 * ParseBlockException class.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public class ParseBlockException extends RuntimeException {

    /**
     * Instantiates a new ParseBlockException.
     *
     * @param message the message
     */
    public ParseBlockException(String message) {
        super(message);
    }

    /**
     * Instantiates a new ParseBlockException.
     *
     * @param message the message
     * @param cause the cause
     */
    public ParseBlockException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new ParseBlockException.
     *
     * @param cause the cause
     */
    public ParseBlockException(Throwable cause) {
        super(cause);
    }
}
