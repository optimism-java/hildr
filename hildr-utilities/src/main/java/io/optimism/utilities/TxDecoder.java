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

package io.optimism.utilities;

import io.optimism.type.DepositTransaction;
import java.math.BigInteger;
import java.util.Arrays;
import org.web3j.rlp.RlpDecoder;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpString;
import org.web3j.utils.Numeric;

/**
 * Tx decoder.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public class TxDecoder {

    public static DepositTransaction decodeToDeposit(final String hexTransaction) {
        final byte[] transaction = Numeric.hexStringToByteArray(hexTransaction);
        if (transaction.length > 0 && transaction[0] != ((byte) 0x7E)) {
            throw new RuntimeException("tx is not type of deposit tx");
        }
        final byte[] encodedTx = Arrays.copyOfRange(transaction, 1, transaction.length);
        final RlpList rlpList = RlpDecoder.decode(encodedTx);
        var values = ((RlpList) rlpList.getValues().get(0)).getValues();
        final String sourceHash = ((RlpString) values.get(0)).asString();
        final String from = ((RlpString) values.get(0)).asString();
        final String to = ((RlpString) values.get(0)).asString();
        final BigInteger mint = ((RlpString) values.get(0)).asPositiveBigInteger();
        final BigInteger value = ((RlpString) values.get(0)).asPositiveBigInteger();
        final BigInteger gas = ((RlpString) values.get(0)).asPositiveBigInteger();
        final boolean isSystemTx =
                ((RlpString) values.get(0)).asPositiveBigInteger().compareTo(BigInteger.ONE) == 0;
        final String data = ((RlpString) values.get(0)).asString();
        return new DepositTransaction(sourceHash, from, to, mint, value, gas, isSystemTx, data);
    }
}
