package io.optimism.exceptions;

/**
 * The type SyncUrlMissingException.
 *
 * @author grapebaba
 * @since 0.1.0
 */
public class SyncUrlMissingException extends RuntimeException {

    /** Instantiates a new Sync url missing exception. */
    public SyncUrlMissingException() {
        super("Sync url missing");
    }

    /**
     * Instantiates a new Sync url missing exception.
     *
     * @param message the message
     */
    public SyncUrlMissingException(String message) {
        super(message);
    }
}
