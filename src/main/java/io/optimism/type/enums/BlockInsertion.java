package io.optimism.type.enums;

/**
 * Block insertion status.
 */
public enum BlockInsertion {
    /**
     * block insertion is successful.
     */
    SUCCESS,
    /**
     * block insertion is temporary.
     */
    TEMPORARY,
    /**
     * block insertion prestate.
     */
    PRESTATE,
    /**
     * block insertion is invalid.
     */
    INVALID;
}
