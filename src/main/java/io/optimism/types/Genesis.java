package io.optimism.types;

import java.math.BigInteger;

/**
 * Genesis info.
 *
 * @param l1 The L1 block that the rollup starts *after* (no derived transactions)
 * @param l2 The L2 block the rollup starts from (no transactions, pre-configured state)
 * @param l2Time Timestamp of L2 block
 * @param systemConfig Initial system configuration values. The L2 genesis block may not include
 *     transactions, and thus cannot encode the config values, unlike later L2 blocks.
 * @author thinkAfCod
 * @since 0.1.1
 */
public record Genesis(Epoch l1, BlockId l2, BigInteger l2Time, SystemConfig systemConfig) {}
