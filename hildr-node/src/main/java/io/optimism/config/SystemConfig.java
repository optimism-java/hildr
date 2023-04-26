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

package io.optimism.config;


import java.math.BigInteger;
import org.web3j.utils.Numeric;

/**
 * The type SystemConfig.
 *
 * @param batchSender batch sender address.
 * @param gasLimit gas limit.
 * @param l1FeeOverhead L1 fee overhead.
 * @param l1FeeScalar L1 fee scalar.
 * @author grapebaba
 * @since 0.1.0
 */
public record SystemConfig(
    String batchSender, BigInteger gasLimit, BigInteger l1FeeOverhead, BigInteger l1FeeScalar) {

  /**
   * Batch hash string.
   *
   * @return the string
   */
  public String batchHash() {
    return Numeric.toHexStringWithPrefixZeroPadded(Numeric.toBigInt(batchSender), 64);
  }
}
