package io.optimism.types;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.apache.commons.lang3.ArrayUtils;
import org.web3j.crypto.Hash;
import org.web3j.utils.Numeric;

/**
 * L1BlockInfo class. Presents the information stored in a L1Block.setL1BlockValues call.
 *
 * @param number         block number
 * @param time           block time
 * @param baseFee        base fee
 * @param blockHash      Block hash.
 * @param sequenceNumber Represents the number of L2 blocks since the start of the epoch
 * @param batcherAddr    batcher address
 * @param l1FeeOverhead  l1 fee overhead
 * @param l1FeeScalar    l1 fee scalar
 * @param blobBaseFeeScalar  blob base fee scalar
 * @param blobBaseFee    blob base fee
 * @author thinkAfCod
 * @since 0.1.1
 */
public record L1BlockInfo(
        BigInteger number,
        BigInteger time,
        BigInteger baseFee,
        String blockHash,
        BigInteger sequenceNumber,
        String batcherAddr,
        BigInteger l1FeeOverhead,
        BigInteger l1FeeScalar,
        BigInteger blobBaseFeeScalar,
        BigInteger blobBaseFee) {

    private static final String L1_INFO_BEDROCK_SIGNATURE =
            "setL1BlockValues(uint64,uint64,uint256,bytes32,uint64,bytes32,uint256,uint256)";

    private static final String L1_INFO_ECOTONE_SIGNATURE = "setL1BlockValuesEcotone()";

    private static final int L1_INFO_BEDROCK_LENGTH = 4 + 32 * 8;

    private static final int L1_INFO_ECOTONE_LENGTH = 4 + 32 * 5;

    private static final byte[] L1_INFO_BEDROCK_SIGN_BYTES =
            ArrayUtils.subarray(Hash.sha3(L1_INFO_BEDROCK_SIGNATURE.getBytes(StandardCharsets.UTF_8)), 0, 4);

    public static final byte[] L1_INFO_ECOTONE_SIGN_BYTES =
            ArrayUtils.subarray(Hash.sha3(L1_INFO_ECOTONE_SIGNATURE.getBytes(StandardCharsets.UTF_8)), 0, 4);

    /**
     * Create Epoch from L1BlockInfo.
     * @return the Epoch.
     */
    public Epoch toEpoch() {
        return new Epoch(number, blockHash, time, sequenceNumber);
    }

    /**
     * Parse tx data to L1BlockInfo.
     *
     * @param data bytes of tx data
     * @return L1BlockInfo Object
     */
    public static L1BlockInfo from(byte[] data) {
        if (data == null) {
            throw new ParseBlockException(String.format("data is unexpected length: %d", 0));
        }
        if (data.length == L1_INFO_BEDROCK_LENGTH) {
            return fromBedrock(data);
        } else if (data.length == L1_INFO_ECOTONE_LENGTH) {
            return fromEcotone(data);
        } else {
            throw new ParseBlockException(String.format("data is unexpected length: %d", data.length));
        }
    }

    private static L1BlockInfo fromBedrock(byte[] data) {
        if (!Objects.deepEquals(ArrayUtils.subarray(data, 0, 4), L1_INFO_BEDROCK_SIGN_BYTES)) {
            throw new ParseBlockException("not equals signature bytes");
        }
        BigInteger number = Numeric.toBigInt(data, 4, 32);
        BigInteger time = Numeric.toBigInt(data, 36, 32);
        BigInteger baseFee = Numeric.toBigInt(data, 68, 32);
        String blockHash = Numeric.toHexString(ArrayUtils.subarray(data, 100, 132));
        BigInteger sequenceNumber = Numeric.toBigInt(data, 132, 32);
        String batcherAddr = Numeric.toHexString(ArrayUtils.subarray(data, 176, 196));
        BigInteger l1FeeOverhead = Numeric.toBigInt(data, 196, 32);
        BigInteger l1FeeScalar = Numeric.toBigInt(data, 228, 32);
        return new L1BlockInfo(
                number,
                time,
                baseFee,
                blockHash,
                sequenceNumber,
                batcherAddr,
                l1FeeOverhead,
                l1FeeScalar,
                BigInteger.ZERO,
                BigInteger.ZERO);
    }

    private static L1BlockInfo fromEcotone(byte[] data) {
        int offset = 0;
        if (!Objects.deepEquals(ArrayUtils.subarray(data, 0, 4), L1_INFO_ECOTONE_SIGN_BYTES)) {
            throw new ParseBlockException("not equals signature bytes");
        }
        offset += 4;

        BigInteger l1FeeScalar = Numeric.toBigInt(data, offset, 4);
        offset += 4;

        BigInteger blobBaseFeeScalar = Numeric.toBigInt(data, offset, 4);
        offset += 4;

        BigInteger sequenceNumber = Numeric.toBigInt(data, offset, 8);
        offset += 8;

        BigInteger time = Numeric.toBigInt(data, offset, 8);
        offset += 8;

        BigInteger number = Numeric.toBigInt(data, offset, 8);
        offset += 8;

        BigInteger baseFee = Numeric.toBigInt(data, offset, 32);
        offset += 32;

        BigInteger blobBaseFee = Numeric.toBigInt(data, offset, 32);
        offset += 32;

        String blockHash = Numeric.toHexString(ArrayUtils.subarray(data, offset, offset + 32));
        offset += 32;

        String batcherAddr = Numeric.toHexString(ArrayUtils.subarray(data, offset, offset + 32));
        return new L1BlockInfo(
                number,
                time,
                baseFee,
                blockHash,
                sequenceNumber,
                batcherAddr,
                BigInteger.ZERO,
                l1FeeScalar,
                blobBaseFeeScalar,
                blobBaseFee);
    }

    /**
     * L1BlockInfo instance converts to BlockId instance.
     *
     * @return BlockId instance
     */
    public BlockId toId() {
        return new BlockId(blockHash, number);
    }
}
