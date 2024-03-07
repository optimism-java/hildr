package io.optimism.common;

import io.optimism.type.L1BlockInfo;
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
public record Epoch(BigInteger number, String hash, BigInteger timestamp, BigInteger sequenceNumber) {

    /**
     * Create epoch from AttributesDepositedCall.
     *
     * @param call the hex call data
     * @return the epoch
     */
    public static Epoch from(AttributesDepositedCall call) {
        return new Epoch(call.number(), call.hash(), call.timestamp(), call.sequenceNumber());
    }

    /**
     * Create epoch from L1BlockInfo.
     *
     * @param info the L1 block info
     * @return the epoch
     */
    public static Epoch from(L1BlockInfo info) {
        return new Epoch(info.number(), info.blockHash(), info.time(), info.sequenceNumber());
    }

    /**
     * Creates epoch from an another epoch and sets sequence number.
     *
     * @param epoch the epoch info
     * @param sequenceNumber the sequence number
     * @return a new epoch with sequence number
     */
    public static Epoch from(Epoch epoch, BigInteger sequenceNumber) {
        return new Epoch(epoch.number(), epoch.hash(), epoch.timestamp(), sequenceNumber);
    }
}
