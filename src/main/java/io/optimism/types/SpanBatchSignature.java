package io.optimism.types;

import java.math.BigInteger;
import java.util.Objects;

/**
 * The type SpanBatchSignature.
 *
 * @author grapebaba
 * @since 0.2.4
 */
public class SpanBatchSignature {
    private BigInteger v;
    private BigInteger r;
    private BigInteger s;

    /**
     * Instantiates a new Span batch signature.
     *
     * @param v the v
     * @param r the r
     * @param s the s
     */
    public SpanBatchSignature(BigInteger v, BigInteger r, BigInteger s) {
        this.v = v;
        this.r = r;
        this.s = s;
    }

    /**
     * V big integer.
     *
     * @return the big integer
     */
    public BigInteger v() {
        return v;
    }

    /**
     * R big integer.
     *
     * @return the big integer
     */
    public BigInteger r() {
        return r;
    }

    /**
     * S big integer.
     *
     * @return the big integer
     */
    public BigInteger s() {
        return s;
    }

    /**
     * Sets v.
     *
     * @param v the v
     */
    public void setV(BigInteger v) {
        this.v = v;
    }

    /**
     * Sets r.
     *
     * @param r the r
     */
    public void setR(BigInteger r) {
        this.r = r;
    }

    /**
     * Sets s.
     *
     * @param s the s
     */
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
