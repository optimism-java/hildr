/*
 * Copyright 2023 q315xia@163.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

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

    public static final long TX_GAS_CONTRACT_CREATION = 53000L;

    public static final long TX_GAS = 21000L;

    public static final long TX_DATA_NON_ZERO_GAS_FRONTIER = 68L;

    public static final long TX_DATA_NON_ZERO_GAS_EIP2028 = 16L;

    public static final long TX_DATA_ZERO_GAS = 4L;

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
