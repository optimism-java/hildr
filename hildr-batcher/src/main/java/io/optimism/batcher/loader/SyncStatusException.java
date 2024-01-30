package io.optimism.batcher.loader;

/**
 * Sync status exception. Throws this when the call to the SyncStatus API fails.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public class SyncStatusException extends RuntimeException {

    /**
     * Instantiates a new sync status exception.
     *
     * @param message the message
     */
    public SyncStatusException(String message) {
        super(message);
    }

    /**
     * Instantiates a new sync status exception.
     *
     * @param message the message
     * @param cause the cause
     */
    public SyncStatusException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new sync status exception.
     *
     * @param cause the cause
     */
    public SyncStatusException(Throwable cause) {
        super(cause);
    }
}
