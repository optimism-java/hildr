/*
 * Copyright 2023 q315xia@163.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package io.optimism.derive.stages;

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
