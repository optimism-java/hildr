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

package io.optimism.l1;


import java.math.BigInteger;

/**
 * The type L1BlockInfo.
 *
 * @param number L1 block number
 * @param hash L1 block hash
 * @param timestamp L1 block timestamp
 * @param baseFee L1 base fee per gas
 * @param mixHash L1 mix hash (prevrandao)
 * @author grapebaba
 * @since 0.1.0
 */
public record L1BlockInfo(
    BigInteger number, String hash, BigInteger timestamp, BigInteger baseFee, String mixHash) {

  /**
   * Create L1BlockInfo.
   *
   * @param number the number
   * @param hash the hash
   * @param timestamp the timestamp
   * @param baseFee the base fee
   * @param mixHash the mix hash
   * @return the l 1 block info
   */
  public static L1BlockInfo create(
      BigInteger number, String hash, BigInteger timestamp, BigInteger baseFee, String mixHash) {
    return new L1BlockInfo(number, hash, timestamp, baseFee, mixHash);
  }
}
