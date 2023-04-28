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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Created by IntelliJ IDEA. Author: kaichen Date: 2023/4/25 Time: 09:56 */
class EpochTest {

  @Test
  @DisplayName("should convert from the deposited transaction calldata")
  void from() throws IOException, URISyntaxException {
    final String callData =
        "0x015d8eb900000000000000000000000000000000000000000000000000000000008768240000000000000000"
            + "000000000000000000000000000000000000000064443450000000000000000000000000000000000000"
            + "000000000000000000000000000e0444c991c5fe1d7291ff34b3f5c3b44ee861f021396d33ba3255b83d"
            + "f30e357d0000000000000000000000000000000000000000000000000000000000000005000000000000"
            + "0000000000007431310e026b69bfc676c0013e12a1a11411eec900000000000000000000000000000000"
            + "000000000000000000000000000008340000000000000000000000000000000000000000000000000000"
            + "0000000f4240";

    final String expectedHash =
        "0x0444c991c5fe1d7291ff34b3f5c3b44ee861f021396d33ba3255b83df30e357d";
    final BigInteger expectedBlockNumber = BigInteger.valueOf(8874020L);
    final BigInteger expectedTimestamp = BigInteger.valueOf(1682191440L);

    final Epoch epoch = Epoch.from(callData);

    assertEquals(expectedHash, epoch.hash());
    assertEquals(expectedBlockNumber, epoch.number());
    assertEquals(expectedTimestamp, epoch.timestamp());
  }
}
