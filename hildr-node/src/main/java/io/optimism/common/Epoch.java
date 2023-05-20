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

package io.optimism.common;

import java.math.BigInteger;

/**
 * The type Epoch.
 *
 * @param number L1 block number.
 * @param hash L1 block hash.
 * @param timestamp L1 block timestamp.
 * @author grapebaba
 * @since 0.1.0
 */
public record Epoch(BigInteger number, String hash, BigInteger timestamp) {

  /**
   * From epoch.
   *
   * @param call the hex call data
   * @return the epoch
   */
  public static Epoch from(AttributesDepositedCall call) {
    return new Epoch(call.number(), call.hash(), call.timestamp());
  }
}
