package io.optimism.type.enums;

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

    public int getCode() {
        return code;
    }

    public boolean isEngineSyncing() {
        return code > 1 && code < 5;
    }
}
