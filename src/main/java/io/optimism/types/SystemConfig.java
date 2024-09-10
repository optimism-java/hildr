package io.optimism.types;

import java.math.BigInteger;

/**
 * The system config.
 *
 * @param batcherAddr batcher address
 * @param overhead overhead
 * @param scalar scalar
 * @param gasLimit gas limit
 * @author thinkAfCod
 * @since 0.1.1
 */
public record SystemConfig(String batcherAddr, BigInteger overhead, BigInteger scalar, BigInteger gasLimit) {}
