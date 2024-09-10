package io.optimism.types;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.Objects;

/**
 * the BeaconBlockHeader class.
 *
 * @author thinkAfCod
 * @since 0.3.0
 */
public class BeaconBlockHeader {

    private String slot;

    @JsonAlias("proposer_index")
    private String proposerIndex;

    @JsonAlias("parent_root")
    private String parentRoot;

    @JsonAlias("state_root")
    private String stateRoot;

    @JsonAlias("body_root")
    private String bodyRoot;

    /**
     * Instantiates a new Beacon block header.
     */
    public BeaconBlockHeader() {}

    /**
     * Instantiates a new Beacon block header.
     * @param slot the slot
     * @param proposerIndex the proposer index
     * @param parentRoot the parent root
     * @param stateRoot the state root
     * @param bodyRoot the body root
     */
    public BeaconBlockHeader(String slot, String proposerIndex, String parentRoot, String stateRoot, String bodyRoot) {
        this.slot = slot;
        this.proposerIndex = proposerIndex;
        this.parentRoot = parentRoot;
        this.stateRoot = stateRoot;
        this.bodyRoot = bodyRoot;
    }

    /**
     * Gets slot.
     *
     * @return the slot
     */
    public String getSlot() {
        return slot;
    }

    /**
     * Sets slot value.
     *
     * @param slot the slot
     */
    public void setSlot(String slot) {
        this.slot = slot;
    }

    /**
     * Gets proposer index.
     *
     * @return the proposer index
     */
    public String getProposerIndex() {
        return proposerIndex;
    }

    /**
     * Sets proposer index value.
     *
     * @param proposerIndex the proposer index
     */
    public void setProposerIndex(String proposerIndex) {
        this.proposerIndex = proposerIndex;
    }

    /**
     * Gets beacon parent root.
     *
     * @return the beacon parent root
     */
    public String getParentRoot() {
        return parentRoot;
    }

    /**
     * Sets parent root value.
     *
     * @param parentRoot the parent root
     */
    public void setParentRoot(String parentRoot) {
        this.parentRoot = parentRoot;
    }

    /**
     * Gets state root.
     *
     * @return the state root
     */
    public String getStateRoot() {
        return stateRoot;
    }

    /**
     * Sets state root value.
     *
     * @param stateRoot the state root
     */
    public void setStateRoot(String stateRoot) {
        this.stateRoot = stateRoot;
    }

    /**
     * Gets body root.
     *
     * @return the body root
     */
    public String getBodyRoot() {
        return bodyRoot;
    }

    /**
     * Sets body root value.
     *
     * @param bodyRoot the body root
     */
    public void setBodyRoot(String bodyRoot) {
        this.bodyRoot = bodyRoot;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BeaconBlockHeader that)) return false;
        return Objects.equals(slot, that.slot)
                && Objects.equals(proposerIndex, that.proposerIndex)
                && Objects.equals(parentRoot, that.parentRoot)
                && Objects.equals(stateRoot, that.stateRoot)
                && Objects.equals(bodyRoot, that.bodyRoot);
    }

    @Override
    public int hashCode() {
        return Objects.hash(slot, proposerIndex, parentRoot, stateRoot, bodyRoot);
    }
}
