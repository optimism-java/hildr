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
import java.util.Objects;

/**
 * L1 block brief information.
 *
 * @param hash L1 block hash
 * @param number L1 block number
 * @param parentHash L1 block parent hash
 * @param timestamp L1 Block timestamp
 * @author thinkAfCod
 * @since 0.1.1
 */
public record L1BlockRef(String hash, BigInteger number, String parentHash, BigInteger timestamp) {

  public static final L1BlockRef emptyBlock =
      new L1BlockRef(
          "0x0000000000000000000000000000000000000000000000000000000000000000",
          BigInteger.ZERO,
          "0x0000000000000000000000000000000000000000000000000000000000000000",
          BigInteger.ZERO);

  /**
   * L1BlockRef instance converts to BlockId instance.
   *
   * @return BlockId instance
   */
  public BlockId toId() {
    return new BlockId(hash, number);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || !(o instanceof L1BlockRef)) {
      return false;
    }

    L1BlockRef that = (L1BlockRef) o;

    if (!Objects.equals(hash, that.hash)) {
      return false;
    }
    if (!Objects.equals(number, that.number)) {
      return false;
    }
    if (!Objects.equals(parentHash, that.parentHash)) {
      return false;
    }
    return Objects.equals(timestamp, that.timestamp);
  }

  @Override
  public int hashCode() {
    int result = hash != null ? hash.hashCode() : 0;
    result = 31 * result + (number != null ? number.hashCode() : 0);
    result = 31 * result + (parentHash != null ? parentHash.hashCode() : 0);
    result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "L1BlockRef{"
        + "hash='"
        + hash
        + '\''
        + ", number="
        + number
        + ", parentHash='"
        + parentHash
        + '\''
        + ", timestamp="
        + timestamp
        + '}';
  }
}
