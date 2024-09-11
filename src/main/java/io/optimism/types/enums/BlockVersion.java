package io.optimism.types.enums;

/**
 * The type BlockVersion.
 *
 * @author grapebaba
 * @since 0.2.6
 */
public enum BlockVersion {
    /**
     * V1 block version.
     */
    V1(0),
    /**
     * V2 block version.
     */
    V2(1),
    /**
     * V3 block version.
     */
    V3(2);

    private final int version;

    BlockVersion(int version) {
        this.version = version;
    }

    /**
     * Has withdrawals boolean.
     *
     * @return the boolean
     */
    public boolean hasWithdrawals() {
        return this == V2 || this == V3;
    }

    /**
     * Has blob properties boolean.
     *
     * @return the boolean
     */
    public boolean hasBlobProperties() {
        return this == V3;
    }

    /**
     * Has parent beacon block root boolean.
     *
     * @return the boolean
     */
    public boolean hasParentBeaconBlockRoot() {
        return this == V3;
    }

    /**
     * Gets version.
     *
     * @return the version
     */
    public int getVersion() {
        return version;
    }
}
