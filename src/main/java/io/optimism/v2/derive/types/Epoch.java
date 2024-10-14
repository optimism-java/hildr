package io.optimism.v2.derive.types;

import java.math.BigInteger;

/**
 * The type Epoch.
 *
 * @param number L1 block number.
 * @param hash L1 block hash.
 * @param timestamp L1 block timestamp.
 * @param sequenceNumber The sequence number of the batcher transactions.
 * @author grapebaba
 * @since 0.1.0
 */
public record Epoch(BigInteger number, String hash, BigInteger timestamp, BigInteger sequenceNumber) {}
