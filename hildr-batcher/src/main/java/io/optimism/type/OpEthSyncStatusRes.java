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

package io.optimism.type;

import java.util.Objects;
import org.web3j.protocol.core.Response;

/**
 * Response for the Optimism Node SyncStatus API.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public class OpEthSyncStatusRes extends Response<OpEthSyncStatusRes.OpEthSyncStatus> {

    /** Constructor of OpEthSyncStatusRes. */
    public OpEthSyncStatusRes() {}

    @Override
    public void setResult(OpEthSyncStatusRes.OpEthSyncStatus result) {
        super.setResult(result);
    }

    /**
     * Returns OpEthSyncStatus data.
     *
     * @return OpEthSyncStatus data
     */
    public OpEthSyncStatus getOpEthSyncStatus() {
        return getResult();
    }

    /**
     * OpEthSyncStatus record class.
     *
     * @param currentL1 The L1 block that the derivation process is currently at in the inner-most
     *     stage. This may not be fully derived into L2 data yet. The safe L2 blocks were
     *     produced/included fully from the L1 chain up to and including this L1 block. If the node is
     *     synced, this matches the HeadL1, minus the verifier confirmation distance.
     * @param currentL1Finalized The L1 block that the derivation process is currently accepting as
     *     finalized. in the inner-most stage, This may not be fully derived into L2 data yet. The
     *     finalized L2 blocks were produced/included fully from the L1 chain up to and including this
     *     L1 block. This may lag behind the FinalizedL1 when the FinalizedL1 could not yet be
     *     verified to be canonical w.r.t. the currently derived L2 chain. It may be zeroed if no
     *     block could be verified yet.
     * @param headL1 HeadL1 is the perceived head of the L1 chain, no confirmation distance. The head
     *     is not guaranteed to build on the other L1 sync status fields, as the node may be in
     *     progress of resetting to adapt to a L1 reorg.
     * @param safeL1 SafeL1 points to the L1 block.
     * @param finalizedL1 Already finalized L1 block.
     * @param unsafeL2 UnsafeL2 is the absolute tip of the L2 chain, pointing to block data that has
     *     not been submitted to L1 yet. The sequencer is building this, and verifiers may also be
     *     ahead of the SafeL2 block if they sync blocks via p2p or other offchain sources.
     * @param safeL2 SafeL2 points to the L2 block that was derived from the L1 chain. This point may
     *     still reorg if the L1 chain reorgs.
     * @param finalizedL2 FinalizedL2 points to the L2 block that was derived fully from finalized L1
     *     information, thus irreversible.
     * @param queuedUnsafeL2 UnsafeL2SyncTarget points to the first unprocessed unsafe L2 block. It
     *     may be zeroed if there is no targeted block.
     */
    public record OpEthSyncStatus(
            L1BlockRef currentL1,
            L1BlockRef currentL1Finalized,
            L1BlockRef headL1,
            L1BlockRef safeL1,
            L1BlockRef finalizedL1,
            L2BlockRef unsafeL2,
            L2BlockRef safeL2,
            L2BlockRef finalizedL2,
            L2BlockRef queuedUnsafeL2) {

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof OpEthSyncStatus that)) return false;
            return Objects.equals(currentL1, that.currentL1)
                    && Objects.equals(currentL1Finalized, that.currentL1Finalized)
                    && Objects.equals(headL1, that.headL1)
                    && Objects.equals(safeL1, that.safeL1)
                    && Objects.equals(finalizedL1, that.finalizedL1)
                    && Objects.equals(unsafeL2, that.unsafeL2)
                    && Objects.equals(safeL2, that.safeL2)
                    && Objects.equals(finalizedL2, that.finalizedL2)
                    && Objects.equals(queuedUnsafeL2, that.queuedUnsafeL2);
        }

        @Override
        public int hashCode() {
            return Objects.hash(
                    currentL1,
                    currentL1Finalized,
                    headL1,
                    safeL1,
                    finalizedL1,
                    unsafeL2,
                    safeL2,
                    finalizedL2,
                    queuedUnsafeL2);
        }

        @Override
        public String toString() {
            return "OpEthSyncStatus{" + "currentL1="
                    + currentL1 + ", currentL1Finalized="
                    + currentL1Finalized + ", headL1="
                    + headL1 + ", safeL1="
                    + safeL1 + ", finalizedL1="
                    + finalizedL1 + ", unsafeL2="
                    + unsafeL2 + ", safeL2="
                    + safeL2 + ", finalizedL2="
                    + finalizedL2 + ", queuedUnsafeL2="
                    + queuedUnsafeL2 + '}';
        }
    }
}
