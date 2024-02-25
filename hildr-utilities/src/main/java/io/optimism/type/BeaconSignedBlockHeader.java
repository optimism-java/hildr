package io.optimism.type;

import java.util.Objects;

/**
 * The BeaconSignedBlockHeader class.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public class BeaconSignedBlockHeader {

    private BeaconBlockHeader message;

    private String signature;

    public BeaconSignedBlockHeader() {}

    public BeaconSignedBlockHeader(BeaconBlockHeader message, String signature) {
        this.message = message;
        this.signature = signature;
    }

    public BeaconBlockHeader getMessage() {
        return message;
    }

    public void setMessage(BeaconBlockHeader message) {
        this.message = message;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BeaconSignedBlockHeader that = (BeaconSignedBlockHeader) o;
        return Objects.equals(message, that.message) && Objects.equals(signature, that.signature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, signature);
    }
}
