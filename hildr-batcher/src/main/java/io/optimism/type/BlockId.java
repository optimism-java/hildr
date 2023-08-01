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
import org.web3j.protocol.core.methods.response.EthBlock;

/**
 * Block id.
 *
 * @param hash Block hash
 * @param number Block number
 * @author thinkAfCod
 * @since 0.1.1
 */
public record BlockId(String hash, BigInteger number) {

  /**
   * Create BlockId from EthBlock.Block.
   *
   * @param block block data
   * @return BlockId object
   */
  public static BlockId from(EthBlock.Block block) {
    return new BlockId(block.getHash(), block.getNumber());
  }
}
