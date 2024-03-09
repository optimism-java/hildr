package io.optimism.common;

import io.optimism.type.L1BlockInfo;
import java.math.BigInteger;

/**
 * The type Epoch.
 *
 * @param number L1 block number.
 * @param hash L1 block hash.
 * @param timestamp L1 block timestamp.
 * @author grapebaba
 * @since 0.1.0
 */
public record Epoch(BigInteger number, String hash, BigInteger timestamp) {

    /**
     * Create epoch from AttributesDepositedCall.
     *
     * @param call the hex call data
     * @return the epoch
     */
    public static Epoch from(AttributesDepositedCall call) {
        return new Epoch(call.number(), call.hash(), call.timestamp());
    }

    /**
     * Create epoch from L1BlockInfo.
     *
     * @param info the L1 block info
     * @return the epoch
     */
    public static Epoch from(L1BlockInfo info) {
        return new Epoch(info.number(), info.blockHash(), info.time());
    }
}
