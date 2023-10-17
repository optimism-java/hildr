package io.optimism.utilities.rpc.response;

import io.optimism.type.L2BlockRef;

/**
 *
 * @param version
 * @param outputRoot
 * @param blockRef
 * @param withdrawalStorageRoot
 * @param stateRoot
 * @param syncStatus
 */
public record OutputRootResult(
        String version,
        String outputRoot,
        L2BlockRef blockRef,
        String withdrawalStorageRoot,
        String stateRoot,
        SyncStatusResult syncStatus) {}
