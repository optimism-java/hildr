package io.optimism.types;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.math.BigInteger;
import java.util.Objects;

/**
 * The class of SpecConfig
 *
 * @author thinkAfCod
 * @since 0.3.0
 */
public class SpecConfig {

    /**
     * The seconds per slot.
     */
    @JsonAlias("SECONDS_PER_SLOT")
    public String secondsPerSlot;

    /**
     * The SpecConfig constructor.
     *
     * @return the seconds per slot
     */
    public BigInteger getSecondsPerSlot() {
        return new BigInteger(secondsPerSlot);
    }

    /**
     * The SpecConfig constructor.
     *
     * @param secondsPerSlot the seconds per slot
     */
    public void setSecondsPerSlot(String secondsPerSlot) {
        this.secondsPerSlot = secondsPerSlot;
    }

    /**
     * The SpecConfig constructor.
     */
    public SpecConfig() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SpecConfig that)) {
            return false;
        }
        return Objects.equals(secondsPerSlot, that.secondsPerSlot);
    }

    @Override
    public int hashCode() {
        return Objects.hash(secondsPerSlot);
    }
}
