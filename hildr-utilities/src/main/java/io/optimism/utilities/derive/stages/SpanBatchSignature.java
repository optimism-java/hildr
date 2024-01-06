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
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (SpanBatchSignature) obj;
        return Objects.equals(this.v, that.v) && Objects.equals(this.r, that.r) && Objects.equals(this.s, that.s);
    }

    @Override
    public int hashCode() {
        return Objects.hash(v, r, s);
    }

    @Override
    public String toString() {
        return "SpanBatchSignature[" + "v=" + v + ", " + "r=" + r + ", " + "s=" + s + ']';
    }
}
