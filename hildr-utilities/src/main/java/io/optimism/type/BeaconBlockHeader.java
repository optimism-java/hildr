package io.optimism.type;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.Objects;

/**
 * the BeaconBlockHeader class.
 *
 * @author thinkAfCod
 * @since 0.2.6
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

    public BeaconBlockHeader() {}

    public BeaconBlockHeader(String slot, String proposerIndex, String parentRoot, String stateRoot, String bodyRoot) {
        this.slot = slot;
        this.proposerIndex = proposerIndex;
        this.parentRoot = parentRoot;
        this.stateRoot = stateRoot;
        this.bodyRoot = bodyRoot;
    }

    public String getSlot() {
        return slot;
    }

    public void setSlot(String slot) {
        this.slot = slot;
    }

    public String getProposerIndex() {
        return proposerIndex;
    }

    public void setProposerIndex(String proposerIndex) {
        this.proposerIndex = proposerIndex;
    }

    public String getParentRoot() {
        return parentRoot;
    }

    public void setParentRoot(String parentRoot) {
        this.parentRoot = parentRoot;
    }

    public String getStateRoot() {
        return stateRoot;
    }

    public void setStateRoot(String stateRoot) {
        this.stateRoot = stateRoot;
    }

    public String getBodyRoot() {
        return bodyRoot;
    }

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
