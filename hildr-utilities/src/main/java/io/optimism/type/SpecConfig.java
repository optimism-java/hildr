package io.optimism.type;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.math.BigInteger;
import java.util.Objects;

/**
 * @author thinkAfCod
 * @since 0.1.1
 */
public class SpecConfig {

    @JsonAlias("SECONDS_PER_SLOT")
    public String secondsPerSlot;

    public BigInteger getSecondsPerSlot() {
        return new BigInteger(secondsPerSlot);
    }

    public void setSecondsPerSlot(String secondsPerSlot) {
        this.secondsPerSlot = secondsPerSlot;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SpecConfig that = (SpecConfig) o;
        return Objects.equals(secondsPerSlot, that.secondsPerSlot);
    }

    @Override
    public int hashCode() {
        return Objects.hash(secondsPerSlot);
    }
}
