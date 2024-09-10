package io.optimism.rpc.response;

import io.optimism.types.L2BlockRef;

/**
 * Output root result.
 *
 * @param version the version
 * @param outputRoot the output root
 * @param blockRef the l2 block ref
 * @param withdrawalStorageRoot the withdrawal storage root
 * @param stateRoot the state root
 * @param syncStatus the l2 sync status info
 */
public record OutputRootResult(
        String version,
        String outputRoot,
        L2BlockRef blockRef,
        String withdrawalStorageRoot,
        String stateRoot,
        SyncStatusResult syncStatus) {}
