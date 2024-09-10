package io.optimism.rpc.response;

import io.optimism.types.L1BlockRef;
import io.optimism.types.L2BlockRef;

/**
 * The SyncStatusResult type. A snapshot of the driver. Values may be zeroed if not yet initialized.
 *
 * @param currentL1 The L1 block that the driver process is currently at in the inner-most stage.
 * @param currentL1Finalized The L1 block that the driver process is currently accepting as
 *     finalized in the inner-most stage.
 * @param headL1 The perceived head of the L1 chain, no confirmation distance.
 * @param safeL1 The stored L1 safe block or an empty block reference if the L1 safe block has not
 *     been initialized yet.
 * @param finalizedL1 The stored L1 finalized block or an empty block reference if the L1 finalized
 *     block has not been initialized yet.
 * @param unsafeL2 The absolute tip of the L2 chain,
 * @param safeL2 Points to the L2 block that was derived from the L1 chain.
 * @param finalizedL2 Points to the L2 block that was derived fully from finalized L1 information,
 *     thus irreversible.
 * @param unsafeL2SyncTarget Points to the first unprocessed unsafe L2 block. It may be zeroed if
 *     there is no targeted block.
 * @param pendingSafeL2 Points to the L2 block processed from the batch, but not consolidated to
 *                     the safe block yet.
 * @author thinkAfCod
 * @since 0.1.1
 */
public record SyncStatusResult(
        L1BlockRef currentL1,
        L1BlockRef currentL1Finalized,
        L1BlockRef headL1,
        L1BlockRef safeL1,
        L1BlockRef finalizedL1,
        L1BlockRef unsafeL2,
        L1BlockRef safeL2,
        L1BlockRef finalizedL2,
        L2BlockRef unsafeL2SyncTarget,
        L1BlockRef pendingSafeL2) {}
