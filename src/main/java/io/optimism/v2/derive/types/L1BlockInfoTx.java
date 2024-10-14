package io.optimism.v2.derive.types;

import io.optimism.config.Config;
import java.math.BigInteger;
import java.util.Arrays;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.utils.Numeric;

/**
 * Represents the fields within an L1 block info transaction.
 *
 * @param number            The current L1 origin block number
 * @param timestamp         The current L1 origin block's timestamp
 * @param baseFee           The current L1 origin block's basefee
 * @param blockHash         The current L1 origin block's hash
 * @param sequenceNumber    The current sequence number
 * @param batcherAddress    The address of the batch submitter
 * @param l1FeeOverhead     The fee overhead for L1 data
 * @param l1FeeScalar       The fee scalar for L1 data
 * @param blobBaseFee       The current blob base fee on L1
 * @param blobBaseFeeScalar The fee scalar for L1 blobspace data
 * @param baseFeeScalar     The fee scalar for L1 data
 * @param selector          The transaction version selector
 * @author thinkAfCod
 * @since 0.4.5
 */
public record L1BlockInfoTx(
        BigInteger number,
        BigInteger timestamp,
        BigInteger baseFee,
        String blockHash,
        BigInteger sequenceNumber,
        String batcherAddress,
        BigInteger l1FeeOverhead,
        BigInteger l1FeeScalar,
        BigInteger blobBaseFee,
        BigInteger blobBaseFeeScalar,
        BigInteger baseFeeScalar,
        String selector) {

    public static final byte L1_SCALAR_ECOTONE = 1;

    private static final String TX_BEDROCK_SELECTOR = "0x015d8eb9";

    private static final String TX_ECOTONE_SELECTOR = "0x440a5e20";

    private static final byte[] BEDROCK_SIGNATURE = Numeric.hexStringToByteArray(TX_BEDROCK_SELECTOR);

    private static final byte[] ECOTONE_SIGNATURE = Numeric.hexStringToByteArray(TX_ECOTONE_SELECTOR);

    private static final int L1_INFO_TX_LEN_BEDROCK = 260;

    private static final int L1_INFO_TX_LEN_ECOTONE = 164;

    public boolean isEcotone() {
        return selector.equals(TX_ECOTONE_SELECTOR);
    }

    public boolean isBedrock() {
        return selector.equals(TX_BEDROCK_SELECTOR);
    }

    /**
     * Encode the L1InfoTx.
     *
     * @return The bytes of encoded L1InfoTx
     */
    public byte[] encode() {
        if (selector.equals(TX_BEDROCK_SELECTOR)) {
            return encodeForBedrock();
        } else if (selector.equals(TX_ECOTONE_SELECTOR)) {
            return encodeForEcotone();
        } else {
            throw new RuntimeException("Invalid selector: " + selector);
        }
    }

    /**
     * Encode the L1InfoTx for Bedrock.
     *
     * @return The bytes of encoded L1InfoTx
     */
    public byte[] encodeForBedrock() {
        byte[] data = new byte[L1_INFO_TX_LEN_BEDROCK];
        System.arraycopy(BEDROCK_SIGNATURE, 0, data, 0, 4);
        System.arraycopy(Numeric.toBytesPadded(this.number, 32), 0, data, 4, 32);
        System.arraycopy(Numeric.toBytesPadded(this.timestamp, 32), 0, data, 36, 32);
        System.arraycopy(Numeric.toBytesPadded(this.baseFee, 32), 0, data, 68, 32);
        System.arraycopy(Numeric.hexStringToByteArray(this.blockHash), 0, data, 100, 32);
        System.arraycopy(Numeric.toBytesPadded(this.sequenceNumber, 32), 0, data, 132, 32);
        System.arraycopy(Numeric.hexStringToByteArray(this.batcherAddress), 0, data, 164, 32);
        System.arraycopy(Numeric.toBytesPadded(this.l1FeeOverhead, 32), 0, data, 196, 32);
        System.arraycopy(Numeric.toBytesPadded(this.l1FeeScalar, 32), 0, data, 228, 32);
        return data;
    }

    /**
     * Encode the L1InfoTx for Ecotone.
     *
     * @return The bytes of encoded L1InfoTx
     */
    public byte[] encodeForEcotone() {
        byte[] data = new byte[L1_INFO_TX_LEN_ECOTONE];
        System.arraycopy(ECOTONE_SIGNATURE, 0, data, 0, 4);
        System.arraycopy(Numeric.toBytesPadded(this.baseFeeScalar, 4), 0, data, 4, 4);
        System.arraycopy(Numeric.toBytesPadded(this.blobBaseFeeScalar, 4), 0, data, 8, 4);
        System.arraycopy(Numeric.toBytesPadded(this.sequenceNumber, 8), 0, data, 12, 8);
        System.arraycopy(Numeric.toBytesPadded(this.timestamp, 8), 0, data, 20, 8);
        System.arraycopy(Numeric.toBytesPadded(this.number, 8), 0, data, 28, 8);
        System.arraycopy(Numeric.toBytesPadded(this.baseFee, 32), 0, data, 36, 32);
        System.arraycopy(Numeric.toBytesPadded(this.blobBaseFee, 32), 0, data, 68, 32);
        var blockHashBytes = Numeric.hexStringToByteArray(this.blockHash);
        System.arraycopy(new Bytes32(blockHashBytes).getValue(), 0, data, 100, 32);
        var batcherAddressBytes = Numeric.hexStringToByteArray(this.batcherAddress);
        System.arraycopy(new Bytes32(batcherAddressBytes).getValue(), 0, data, 132, 32);
        return data;
    }

    public static L1BlockInfoTx create(
            Config.ChainConfig chainConfig,
            SystemConfig config,
            BigInteger sequenceNumber,
            EthBlock.Block l1Block,
            BigInteger l2BlockTime) {
        if (chainConfig.isEcotoneAndNotFirst(l2BlockTime)) {
            var scalars = Numeric.toBytesPadded(config.scalar(), 32);
            var versionByte = scalars[0];
            var blobBaseFeeScalar = versionByte != L1_SCALAR_ECOTONE
                    ? BigInteger.ZERO
                    : Numeric.toBigInt(Arrays.copyOfRange(scalars, 24, 28));
            var baseFeeScalar = Numeric.toBigInt(Arrays.copyOfRange(scalars, 28, scalars.length));

            return new L1BlockInfoTx(
                    l1Block.getNumber(),
                    l1Block.getTimestamp(),
                    l1Block.getBaseFeePerGas() == null ? BigInteger.ZERO : l1Block.getBaseFeePerGas(),
                    l1Block.getHash(),
                    sequenceNumber,
                    config.batcherAddr(),
                    BigInteger.ZERO,
                    BigInteger.ZERO,
                    l1Block.getExcessBlobGas() == null ? BigInteger.ONE : l1Block.getExcessBlobGas(),
                    blobBaseFeeScalar,
                    baseFeeScalar,
                    TX_ECOTONE_SELECTOR);
        } else {
            return new L1BlockInfoTx(
                    l1Block.getNumber(),
                    l1Block.getTimestamp(),
                    l1Block.getBaseFeePerGas(),
                    l1Block.getHash(),
                    sequenceNumber,
                    config.batcherAddr(),
                    config.overhead(),
                    config.scalar(),
                    BigInteger.ZERO,
                    BigInteger.ZERO,
                    BigInteger.ZERO,
                    TX_BEDROCK_SELECTOR);
        }
    }

    /**
     * Decode tx input to the L1InfoTx.
     *
     * @param input deposit transaction input
     * @return The L1BlockInfoTx instance.
     */
    public static L1BlockInfoTx decodeFrom(String input) {
        if (input.startsWith(TX_BEDROCK_SELECTOR)) {
            return decodeBedrockInput(Numeric.hexStringToByteArray(input));
        } else if (input.startsWith(TX_ECOTONE_SELECTOR)) {
            return decodeEcotoneInput(Numeric.hexStringToByteArray(input));
        } else {
            throw new IllegalArgumentException("Invalid input: " + input);
        }
    }

    /**
     * +---------+--------------------------+
     * | Bytes   | Field                    |
     * +---------+--------------------------+
     * | 4       | Function signature       |
     * | 32      | Number                   |
     * | 32      | Time                     |
     * | 32      | BaseFee                  |
     * | 32      | BlockHash                |
     * | 32      | SequenceNumber           |
     * | 32      | BatcherHash              |
     * | 32      | L1FeeOverhead            |
     * | 32      | L1FeeScalar              |
     * +---------+--------------------------+
     *
     * @param txInput the l2 bedrock tx input
     * @return the l1 block info tx
     */
    public static L1BlockInfoTx decodeBedrockInput(byte[] txInput) {
        if (txInput.length != L1_INFO_TX_LEN_BEDROCK) {
            throw new IllegalArgumentException("bedrock deposit tx input length is not 164 bytes");
        }
        BigInteger l1BlockNum = Numeric.toBigInt(Arrays.copyOfRange(txInput, 28, 36));
        BigInteger l1BlockTime = Numeric.toBigInt(Arrays.copyOfRange(txInput, 60, 68));
        BigInteger baseFee = Numeric.toBigInt(Arrays.copyOfRange(txInput, 92, 100));
        String l1BlockHash = Numeric.toHexString(Arrays.copyOfRange(txInput, 100, 132));
        BigInteger seqNum = Numeric.toBigInt(Arrays.copyOfRange(txInput, 156, 164));
        String batcherHash = Numeric.toHexString(Arrays.copyOfRange(txInput, 176, 196));
        BigInteger l1FeeOverhead = Numeric.toBigInt(Arrays.copyOfRange(txInput, 196, 228));
        BigInteger l1FeeScalar = Numeric.toBigInt(Arrays.copyOfRange(txInput, 228, 260));
        return new L1BlockInfoTx(
                l1BlockNum,
                l1BlockTime,
                baseFee,
                l1BlockHash,
                seqNum,
                batcherHash,
                l1FeeOverhead,
                l1FeeScalar,
                BigInteger.ZERO,
                BigInteger.ZERO,
                BigInteger.ZERO,
                TX_BEDROCK_SELECTOR);
    }

    /**
     * +---------+--------------------------+
     * | Bytes   | Field                    |
     * +---------+--------------------------+
     * | 4       | Function signature       |
     * | 4       | BaseFeeScalar            |
     * | 4       | BlobBaseFeeScalar        |
     * | 8       | SequenceNumber           |
     * | 8       | Timestamp                |
     * | 8       | L1BlockNumber            |
     * | 32      | BaseFee                  |
     * | 32      | BlobBaseFee              |
     * | 32      | BlockHash                |
     * | 32      | BatcherHash              |
     * +---------+--------------------------+
     *
     * @param input the l2 ecotone tx input
     * @return the l1 block info tx
     */
    public static L1BlockInfoTx decodeEcotoneInput(byte[] input) {
        if (input.length != L1_INFO_TX_LEN_ECOTONE) {
            throw new IllegalArgumentException("ecotone deposit tx input length is not 164 bytes");
        }

        BigInteger baseFeeScalar = Numeric.toBigInt(Arrays.copyOfRange(input, 4, 8));
        BigInteger blobBaseFeeScalar = Numeric.toBigInt(Arrays.copyOfRange(input, 8, 12));

        BigInteger sequenceNum = Numeric.toBigInt(Arrays.copyOfRange(input, 12, 20));
        BigInteger l1Timestamp = Numeric.toBigInt(Arrays.copyOfRange(input, 20, 28));
        BigInteger l1BlockNum = Numeric.toBigInt(Arrays.copyOfRange(input, 28, 36));

        BigInteger baseFee = Numeric.toBigInt(Arrays.copyOfRange(input, 60, 68));
        BigInteger blobBaseFee = Numeric.toBigInt(Arrays.copyOfRange(input, 84, 100));
        String l1BlockHash = Numeric.toHexString(Arrays.copyOfRange(input, 100, 132));
        String batcherAddress = Numeric.toHexString(Arrays.copyOfRange(input, 132, 164));
        return new L1BlockInfoTx(
                l1BlockNum,
                l1Timestamp,
                baseFee,
                l1BlockHash,
                sequenceNum,
                batcherAddress,
                BigInteger.ZERO,
                BigInteger.ZERO,
                blobBaseFee,
                blobBaseFeeScalar,
                baseFeeScalar,
                TX_ECOTONE_SELECTOR);
    }
}
