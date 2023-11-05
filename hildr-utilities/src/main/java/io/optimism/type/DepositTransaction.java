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

  public DepositTransaction() {}

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

  public String getSourceHash() {
    return sourceHash;
  }

  public String getFrom() {
    return from;
  }

  public String getTo() {
    return to;
  }

  public BigInteger getMint() {
    return mint;
  }

  public BigInteger getValue() {
    return value;
  }

  public BigInteger getGas() {
    return gas;
  }

  public boolean isSystemTransaction() {
    return isSystemTransaction;
  }

  public String getData() {
    return data;
  }

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
