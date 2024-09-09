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

import io.netty.buffer.Unpooled;
import java.math.BigInteger;
import org.web3j.rlp.RlpList;

/**
 * The type Batch.
 *
 * @param batch the batch
 * @param l1InclusionBlock L1 inclusion block
 * @author grapebaba
 * @since 0.2.4
 */
public record Batch(IBatch batch, BigInteger l1InclusionBlock) {

    /**
     * Gets timestamp.
     *
     * @return the timestamp
     */
    public BigInteger timestamp() {
        return batch.getTimestamp();
    }

    /**
     * Encode batch.
     *
     * @return encoded bytes by the batch
     */
    public byte[] encode() {
        if (batch instanceof SingularBatch) {
            var typedBatch = (SingularBatch) batch;
            return typedBatch.encode();
        } else if (batch instanceof SpanBatch) {
            throw new IllegalStateException("unsupport batch type: %s".formatted(batch.getBatchType()));
        } else {
            throw new IllegalStateException("unknown batch type");
        }
    }

    /**
     * Decode singular batch.
     *
     * @param rlp the rlp
     * @param l1InclusionBlock L1 inclusion block
     * @return the batch
     */
    public static Batch decodeSingularBatch(final RlpList rlp, final BigInteger l1InclusionBlock) {
        return new Batch(SingularBatch.decode(rlp), l1InclusionBlock);
    }

    /**
     * Decode span batch.
     *
     * @param buf the span batch encoded bytes
     * @param blockTime the block time
     * @param l2genesisTimestamp L2 genesis timestamp
     * @param l2ChainId the L2 chain id
     * @param l1InclusionBlock L1 inclusion block
     * @return the batch
     */
    public static Batch decodeSpanBatch(
            final byte[] buf,
            final BigInteger blockTime,
            final BigInteger l2genesisTimestamp,
            final BigInteger l2ChainId,
            final BigInteger l1InclusionBlock) {
        final RawSpanBatch rawSpanBatch = new RawSpanBatch();
        rawSpanBatch.decode(Unpooled.wrappedBuffer(buf));
        final SpanBatch spanBatch = rawSpanBatch.toSpanBatch(blockTime, l2genesisTimestamp, l2ChainId);
        return new Batch(spanBatch, l1InclusionBlock);
    }
}
