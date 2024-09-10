package io.optimism.types;

import java.util.Objects;

/**
 * The BeaconSignedBlockHeader class.
 *
 * @author thinkAfCod
 * @since 0.3.0
 */
public class BeaconSignedBlockHeader {

    private BeaconBlockHeader message;

    private String signature;

    /**
     * Instantiates a new Beacon signed block header.
     */
    public BeaconSignedBlockHeader() {}

    /**
     * Instantiates a new Beacon signed block header.
     *
     * @param message   the message
     * @param signature the signature
     */
    public BeaconSignedBlockHeader(BeaconBlockHeader message, String signature) {
        this.message = message;
        this.signature = signature;
    }

    /**
     * Gets message.
     *
     * @return the message
     */
    public BeaconBlockHeader getMessage() {
        return message;
    }

    /**
     * Sets message.
     *
     * @param message the message
     */
    public void setMessage(BeaconBlockHeader message) {
        this.message = message;
    }

    /**
     * Gets signature.
     *
     * @return the signature
     */
    public String getSignature() {
        return signature;
    }

    /**
     * Sets signature.
     *
     * @param signature the signature
     */
    public void setSignature(String signature) {
        this.signature = signature;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BeaconSignedBlockHeader that)) {
            return false;
        }
        return Objects.equals(message, that.message) && Objects.equals(signature, that.signature);
    }

    @Override
    public int hashCode() {
        return Objects.hash(message, signature);
    }
}
