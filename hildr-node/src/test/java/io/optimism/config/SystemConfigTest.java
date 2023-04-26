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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import org.junit.jupiter.api.Test;
import org.web3j.utils.Numeric;

/**
 * The type SystemConfig test.
 *
 * @author grapebaba
 * @since 0.1.0
 */
class SystemConfigTest {

  /** Batch hash. */
  @Test
  void batchHash() {
    SystemConfig systemConfig =
        new SystemConfig(
            "0x2d679b567db6187c0c8323fa982cfb88b74dbcc7",
            BigInteger.valueOf(25_000_000L),
            BigInteger.valueOf(2100),
            BigInteger.valueOf(1000000));

    assertTrue(
        systemConfig.batchHash().contains(Numeric.cleanHexPrefix(systemConfig.batchSender())));
  }
}
