package io.optimism.common;

import java.math.BigInteger;
import java.util.Objects;
import net.osslabz.evm.abi.decoder.AbiDecoder;
import net.osslabz.evm.abi.decoder.DecodedFunctionCall;

/**
 * The type AttributesDepositedCall.
 *
 * @param number the number
 * @param timestamp the timestamp
 * @param baseFee the base fee
 * @param hash the hash
 * @param sequenceNumber the sequence number
 * @param batcherHash the batcher hash
 * @param feeOverhead the fee overhead
 * @param feeScalar the fee scalar
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
        BigInteger feeScalar) {

    private static final AbiDecoder l1BlockAbi;

    static {
        l1BlockAbi = new AbiDecoder(Objects.requireNonNull(Epoch.class.getResourceAsStream("/abi/L1Block.json")));
    }

    /**
     * From attributes deposited call.
     *
     * @param callData the call data
     * @return the attributes deposited call
     */
    public static AttributesDepositedCall from(String callData) {
        DecodedFunctionCall decodedFunctionCall = l1BlockAbi.decodeFunctionCall(callData);
        BigInteger number = (BigInteger) decodedFunctionCall.getParam("_number").getValue();
        BigInteger timestamp =
                (BigInteger) decodedFunctionCall.getParam("_timestamp").getValue();
        BigInteger baseFee =
                (BigInteger) decodedFunctionCall.getParam("_basefee").getValue();
        String hash = (String) decodedFunctionCall.getParam("_hash").getValue();
        BigInteger sequenceNumber =
                (BigInteger) decodedFunctionCall.getParam("_sequencenumber").getValue();
        String batcherHash =
                (String) decodedFunctionCall.getParam("_batcherhash").getValue();
        BigInteger feeOverhead =
                (BigInteger) decodedFunctionCall.getParam("_l1feeoverhead").getValue();
        BigInteger feeScalar =
                (BigInteger) decodedFunctionCall.getParam("_l1feescalar").getValue();

        return new AttributesDepositedCall(
                number, timestamp, baseFee, hash, sequenceNumber, batcherHash, feeOverhead, feeScalar);
    }
}
