package io.optimism.types;

import java.math.BigInteger;

/**
 * The type Payload info.
 *
 * @param payloadId the payload id
 * @param timestamp the timestamp
 * @author thinkAfCod
 * @since 0.4.1
 */
public record PayloadInfo(String payloadId, BigInteger timestamp) {}
