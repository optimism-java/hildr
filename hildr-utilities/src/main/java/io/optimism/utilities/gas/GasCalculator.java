package io.optimism.utilities.gas;

import java.math.BigInteger;

/**
 * Gas util.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class GasCalculator {

    /**
     * Private Constructor of GasCalculator.
     */
    private GasCalculator() {}

    /**
     * The constant TX_GAS_CONTRACT_CREATION.
     */
    public static final long TX_GAS_CONTRACT_CREATION = 53000L;

    /**
     * The constant TX_GAS.
     */
    public static final long TX_GAS = 21000L;

    /**
     * The constant TX_DATA_NON_ZERO_GAS_FRONTIER.
     */
    public static final long TX_DATA_NON_ZERO_GAS_FRONTIER = 68L;

    /**
     * The constant TX_DATA_NON_ZERO_GAS_EIP2028.
     */
    public static final long TX_DATA_NON_ZERO_GAS_EIP2028 = 16L;

    /**
     * The constant TX_DATA_ZERO_GAS.
     */
    public static final long TX_DATA_ZERO_GAS = 4L;

    /**
     * The constant INIT_CODE_WORD_GAS.
     */
    public static final long INIT_CODE_WORD_GAS = 2L;

    /**
     * Calculator gas fee but exclude effective of AccessList.
     *
     * @param data               Tx data
     * @param isContractCreation Is contract creation
     * @param isHomestead        Is home stead
     * @param isEIP2028          Is EIP2028
     * @param isEIP3860          Is EIP3860
     * @return Intrinsic gas
     */
    public static long intrinsicGasWithoutAccessList(
            byte[] data, boolean isContractCreation, boolean isHomestead, boolean isEIP2028, boolean isEIP3860) {
        var gas = isContractCreation && isHomestead ? TX_GAS_CONTRACT_CREATION : TX_GAS;
        if (data.length <= 0) {
            return gas;
        }
        long nz = 0;
        for (var byt : data) {
            if (byt != 0) {
                nz += 1L;
            }
        }
        var nonZeroGas = isEIP2028 ? TX_DATA_NON_ZERO_GAS_EIP2028 : TX_DATA_NON_ZERO_GAS_FRONTIER;
        var gasRange = Long.MAX_VALUE - gas;
        gas += nz * nonZeroGas;

        var z = data.length / nz;
        gas += z * TX_DATA_ZERO_GAS;
        if (isContractCreation && isEIP3860) {
            var lenWords = toWordSize(data.length);
            if (gasRange / INIT_CODE_WORD_GAS < lenWords) {
                throw new RuntimeException("Gas uint overflow");
            }
            gas += lenWords * INIT_CODE_WORD_GAS;
        }
        return gas;
    }

    /**
     * Calculate gas fee cap.
     *
     * @param baseFee   base fee
     * @param gasTipCap gas tip cap
     * @return gas fee
     */
    public static BigInteger calcGasFeeCap(BigInteger baseFee, BigInteger gasTipCap) {
        return gasTipCap.add(baseFee.multiply(BigInteger.TWO));
    }

    private static long toWordSize(int size) {
        return (size + 31L) / 32L;
    }
}
