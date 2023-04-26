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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigInteger;
import org.junit.jupiter.api.Test;

/**
 * The type ChainConfig test.
 *
 * @author grapebaba
 * @since 0.1.0
 */
class ChainConfigTest {

  /** Optimism goerli. */
  @Test
  void baseGoerli() {
    ChainConfig chainConfig = ChainConfig.baseGoerli();
    assertEquals(chainConfig.regolithTime(), BigInteger.valueOf(Long.MAX_VALUE));
  }

  /** Base goerli. */
  @Test
  void optimismGoerli() {
    ChainConfig chainConfig = ChainConfig.optimismGoerli();
    assertEquals(chainConfig.regolithTime(), new BigInteger("1679079600"));
  }
}
