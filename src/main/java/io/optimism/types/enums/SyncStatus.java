package io.optimism.types.enums;

/**
 * Engine sync status enum.
 *
 * @author thinkAfCod
 * @since 0.3.4
 */
public enum SyncStatus {
    /**
     * Consensus layer sync mode initial status.
     */
    CL(1),
    /**
     * Engine layer sync mode initial status.
     */
    WillStartEL(2),
    /**
     * Engine layer sync mode started status.
     */
    StartedEL(3),
    /**
     * Engine layer sync mode finished status, but not finalized.
     */
    FinishedELNotFinalized(4),
    /**
     * Engine layer sync mode has finished status.
     */
    FinishedEL(5);

    private final int code;

    SyncStatus(int code) {
        this.code = code;
    }

    /**
     * Get the sync status code.
     *
     * @return the sync status code
     */
    public int getCode() {
        return code;
    }

    /**
     * Check if the engine is syncing.
     *
     * @return true if the engine is syncing, false otherwise
     */
    public boolean isEngineSyncing() {
        return code > 1 && code < 5;
    }
}
