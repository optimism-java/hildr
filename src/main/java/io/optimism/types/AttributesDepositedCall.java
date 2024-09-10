package io.optimism.types;

import java.math.BigInteger;
import org.apache.commons.lang3.StringUtils;
import org.web3j.utils.Numeric;

/**
 * The type AttributesDepositedCall.
 *
 * @param number            the number
 * @param timestamp         the timestamp
 * @param baseFee           the base fee
 * @param hash              the hash
 * @param sequenceNumber    the sequence number
 * @param batcherHash       the batcher hash
 * @param feeOverhead       the fee overhead
 * @param feeScalar         the fee scalar
 * @param blobBaseFeeScalar the blob base fee scalar
 * @param blobBaseFee       the blob base fee
 * @author grapebaba
 * @since 0.1.0
 */
public record AttributesDepositedCall(
        BigInteger number,
        BigInteger timestamp,
        BigInteger baseFee,
        String hash,
        BigInteger sequenceNumber,
        String batcherHash,
        BigInteger feeOverhead,
        BigInteger feeScalar,
        BigInteger blobBaseFeeScalar,
        BigInteger blobBaseFee) {

    /**
     * Create Epoch from attributes deposited call.
     * @return the epoch
     */
    public Epoch toEpoch() {
        return new Epoch(number, hash, timestamp, sequenceNumber);
    }

    /**
     * Create AttributesDepositedCall from attributes deposited call.
     *
     * @param callData the call data from ectone transaction or bedrock transaction
     * @return the attributes deposited call
     */
    public static AttributesDepositedCall from(String callData) {
        if (StringUtils.isEmpty(callData)) {
            throw new RuntimeException();
        }
        var info = L1BlockInfo.from(Numeric.hexStringToByteArray(callData));
        return new AttributesDepositedCall(
                info.number(),
                info.time(),
                info.baseFee(),
                info.blockHash(),
                info.sequenceNumber(),
                info.batcherAddr(),
                info.l1FeeOverhead(),
                info.l1FeeScalar(),
                info.blobBaseFeeScalar(),
                info.blobBaseFee());
    }
}
