/*
 * Copyright 2023 281165273grape@gmail.com
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

package io.optimism.derive.stages;


import java.math.BigInteger;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpString;

/**
 * The type Batch.
 *
 * @param parentHash the parent hash
 * @param epochNum the epoch num
 * @param epochHash the epoch hash
 * @param timestamp the timestamp
 * @param transactions the transactions
 * @param l1InclusionBlock L1 inclusion block
 * @author grapebaba
 * @since 0.1.0
 */
public record Batch(
    String parentHash,
    BigInteger epochNum,
    String epochHash,
    BigInteger timestamp,
    List<String> transactions,
    BigInteger l1InclusionBlock) {

  /**
   * Decode batch.
   *
   * @param rlp the rlp
   * @param l1InclusionBlock L1 inclusion block
   * @return the batch
   */
  public static Batch decode(RlpList rlp, BigInteger l1InclusionBlock) {
    String parentHash = ((RlpString) rlp.getValues().get(0)).asString();
    BigInteger epochNum = ((RlpString) rlp.getValues().get(1)).asPositiveBigInteger();
    String epochHash = ((RlpString) rlp.getValues().get(2)).asString();
    BigInteger timestamp = ((RlpString) rlp.getValues().get(3)).asPositiveBigInteger();
    List<String> transactions =
        ((RlpList) rlp.getValues().get(4))
            .getValues().stream().map(rlpString -> ((RlpString) rlpString).asString()).toList();
    return new Batch(parentHash, epochNum, epochHash, timestamp, transactions, l1InclusionBlock);
  }

  /**
   * Has invalid transactions boolean.
   *
   * @return the boolean
   */
  public boolean hasInvalidTransactions() {
    return this.transactions.stream()
        .anyMatch(s -> StringUtils.isEmpty(s) || StringUtils.startsWithIgnoreCase(s, "0x7E"));
  }
}
