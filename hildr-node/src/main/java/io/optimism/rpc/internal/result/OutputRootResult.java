package io.optimism.rpc.internal.result;

/**
 * output root result.
 *
 * @param outputRoot output root
 * @param version version
 * @param stateRoot state root
 * @param withdrawalStorageRoot withdrawal storage root
 * @author thinkAfCod
 * @since 2023.06
 */
public record OutputRootResult(String outputRoot, String version, String stateRoot, String withdrawalStorageRoot) {}
