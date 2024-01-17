package io.optimism.utilities.derive.stages;

import java.math.BigInteger;
import java.util.Objects;

public class SpanBatchSignature {
    private BigInteger v;
    private BigInteger r;
    private BigInteger s;

    public SpanBatchSignature(BigInteger v, BigInteger r, BigInteger s) {
        this.v = v;
        this.r = r;
        this.s = s;
    }

    public BigInteger v() {
        return v;
    }

    public BigInteger r() {
        return r;
    }

    public BigInteger s() {
        return s;
    }

    public void setV(BigInteger v) {
        this.v = v;
    }

    public void setR(BigInteger r) {
        this.r = r;
    }

    public void setS(BigInteger s) {
        this.s = s;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SpanBatchSignature that)) return false;
        return Objects.equals(v, that.v) && Objects.equals(r, that.r) && Objects.equals(s, that.s);
    }

    @Override
    public int hashCode() {
        return Objects.hash(v, r, s);
    }

    @Override
    public String toString() {
        return "SpanBatchSignature[v=%s, r=%s, s=%s]".formatted(v, r, s);
    }
}
