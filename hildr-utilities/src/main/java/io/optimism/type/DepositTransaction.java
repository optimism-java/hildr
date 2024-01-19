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

package io.optimism.type;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.web3j.rlp.RlpString;
import org.web3j.rlp.RlpType;

/**
 * Class of DepositTransaction.
 * Only declared in Optimism.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public class DepositTransaction {

    /**
     * Uniquely identifies the source of the deposit
     */
    private String sourceHash;

    /**
     * Exposed through the types.Signer, not through TxData
     */
    private String from;

    /**
     * Means contract creation
     */
    private String to;

    /**
     * Minted on L2, locked on L1, null if no minting.
     */
    private BigInteger mint;

    /**
     * Transferred from L2 balance, executed after Mint (if any)
     */
    private BigInteger value;

    /**
     * Gas limit
     */
    private BigInteger gas;

    /**
     * Field indicating if this transaction is exempt from the L2 gas limit.
     */
    private boolean isSystemTransaction;

    /**
     * Normal Tx data
     */
    private String data;

    /**
     * Instantiates a new Deposit transaction.
     */
    public DepositTransaction() {}

    /**
     * Instantiates a new Deposit transaction.
     *
     * @param sourceHash          the source hash
     * @param from                the from
     * @param to                  the to
     * @param mint                the mint
     * @param value               the value
     * @param gas                 the gas
     * @param isSystemTransaction the is system transaction
     * @param data                the data
     */
    public DepositTransaction(
            String sourceHash,
            String from,
            String to,
            BigInteger mint,
            BigInteger value,
            BigInteger gas,
            boolean isSystemTransaction,
            String data) {
        this.sourceHash = sourceHash;
        this.from = from;
        this.to = to;
        this.mint = mint;
        this.value = value;
        this.gas = gas;
        this.isSystemTransaction = isSystemTransaction;
        this.data = data;
    }

    /**
     * Gets source hash.
     *
     * @return the source hash
     */
    public String getSourceHash() {
        return sourceHash;
    }

    /**
     * Gets from.
     *
     * @return the from
     */
    public String getFrom() {
        return from;
    }

    /**
     * Gets to.
     *
     * @return the to
     */
    public String getTo() {
        return to;
    }

    /**
     * Gets mint.
     *
     * @return the mint
     */
    public BigInteger getMint() {
        return mint;
    }

    /**
     * Gets value.
     *
     * @return the value
     */
    public BigInteger getValue() {
        return value;
    }

    /**
     * Gets gas.
     *
     * @return the gas
     */
    public BigInteger getGas() {
        return gas;
    }

    /**
     * Is system transaction boolean.
     *
     * @return the boolean
     */
    public boolean isSystemTransaction() {
        return isSystemTransaction;
    }

    /**
     * Gets data.
     *
     * @return the data
     */
    public String getData() {
        return data;
    }

    /**
     * As rlp values list.
     *
     * @return the list
     */
    public List<RlpType> asRlpValues() {
        List<RlpType> result = new ArrayList<>();
        result.add(RlpString.create(getSourceHash()));
        result.add(RlpString.create(getFrom()));
        result.add(RlpString.create(getTo()));
        result.add(RlpString.create(getMint()));
        result.add(RlpString.create(getValue()));
        result.add(RlpString.create(getGas()));
        result.add(RlpString.create(isSystemTransaction() ? 1 : 0));
        result.add(RlpString.create(getData()));
        return result;
    }
}
