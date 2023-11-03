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

import io.optimism.engine.ExecutionPayload;
import java.math.BigInteger;
import java.util.Objects;
import org.web3j.protocol.core.methods.response.EthBlock.Block;

/**
 * The type BlockInfo.
 *
 * <p>Selected block header info.
 *
 * @param number Block number.
 * @param hash Block hash.
 * @param timestamp Block timestamp.
 * @param parentHash Block parent hash.
 * @author grapebaba
 * @since 0.1.0
 */
public record BlockInfo(String hash, BigInteger number, String parentHash, BigInteger timestamp) {

  /**
   * From block info.
   *
   * @param block the block
   * @return the block info
   */
  public static BlockInfo from(Block block) {
    BigInteger number = block.getNumber();
    if (number == null) {
      throw new BlockNotIncludedException();
    }

    String hash = block.getHash();
    if (hash == null) {
      throw new BlockNotIncludedException();
    }
    return new BlockInfo(hash, number, block.getParentHash(), block.getTimestamp());
  }

  /**
   * From block info.
   *
   * @param payload the payload
   * @return the block info
   */
  public static BlockInfo from(ExecutionPayload payload) {
    return new BlockInfo(
        payload.blockHash(), payload.blockNumber(), payload.parentHash(), payload.timestamp());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BlockInfo blockInfo)) {
      return false;
    }
    return Objects.equals(hash, blockInfo.hash)
        && Objects.equals(number, blockInfo.number)
        && Objects.equals(parentHash, blockInfo.parentHash)
        && Objects.equals(timestamp, blockInfo.timestamp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(hash, number, parentHash, timestamp);
  }
}
