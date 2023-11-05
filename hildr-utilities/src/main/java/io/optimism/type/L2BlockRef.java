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
import org.web3j.protocol.core.methods.response.EthBlock;

/**
 * L2 block brief information.
 *
 * @param hash L1 block hash
 * @param number L1 block number
 * @param parentHash L1 block parent hash
 * @param timestamp L1 Block timestamp
 * @param l1origin L1 block Id information
 * @param sequenceNumber sequence number that distance to first block of epoch
 * @author thinkAfCod
 * @since 0.1.1
 */
public record L2BlockRef(
    String hash,
    BigInteger number,
    String parentHash,
    BigInteger timestamp,
    BlockId l1origin,
    BigInteger sequenceNumber) {

  public final static L2BlockRef EMPTY = new L2BlockRef(null, null, null, null, null, null);

  /**
   * L2BlockRef instance converts to BlockId instance.
   *
   * @return BlockId instance
   */
  public BlockId toId() {
    return new BlockId(hash, number);
  }

  /**
   * Create a L2BlockRef instance from EthBlock.Block and L1BlockInfo
   *
   * @param block block info
   * @param l1Info l1 block info
   * @return L2BlockRef instance
   */
  public static L2BlockRef fromBlockAndL1Info(EthBlock.Block block, L1BlockInfo l1Info) {
    return new L2BlockRef(
        block.getHash(),
        block.getNumber(),
        block.getParentHash(),
        block.getTimestamp(),
        new BlockId(l1Info.blockHash(), l1Info.number()),
        l1Info.sequenceNumber());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || !(o instanceof L2BlockRef)) {
      return false;
    }

    L2BlockRef that = (L2BlockRef) o;

    if (!Objects.equals(hash, that.hash)) {
      return false;
    }
    if (!Objects.equals(number, that.number)) {
      return false;
    }
    if (!Objects.equals(parentHash, that.parentHash)) {
      return false;
    }
    if (!Objects.equals(timestamp, that.timestamp)) {
      return false;
    }
    if (!Objects.equals(l1origin, that.l1origin)) {
      return false;
    }
    return Objects.equals(sequenceNumber, that.sequenceNumber);
  }

  @Override
  public int hashCode() {
    int result = hash != null ? hash.hashCode() : 0;
    result = 31 * result + (number != null ? number.hashCode() : 0);
    result = 31 * result + (parentHash != null ? parentHash.hashCode() : 0);
    result = 31 * result + (timestamp != null ? timestamp.hashCode() : 0);
    result = 31 * result + (l1origin != null ? l1origin.hashCode() : 0);
    result = 31 * result + (sequenceNumber != null ? sequenceNumber.hashCode() : 0);
    return result;
  }
}
