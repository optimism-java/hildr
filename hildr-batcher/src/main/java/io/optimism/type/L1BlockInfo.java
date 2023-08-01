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
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.tuweni.crypto.Hash;
import org.web3j.utils.Numeric;

/**
 * L1BlockInfo class. Presents the information stored in a L1Block.setL1BlockValues call.
 *
 * @param number block number
 * @param time block time
 * @param baseFee base fee
 * @param blockHash Block hash.
 * @param sequenceNumber Represents the number of L2 blocks since the start of the epoch
 * @param batcherAddr batcher address
 * @param l1FeeOverhead l1 fee overhead
 * @param l1FeeScalar l1 fee scalar
 * @author thinkAfCod
 * @since 0.1.1
 */
public record L1BlockInfo(
    BigInteger number,
    BigInteger time,
    BigInteger baseFee,
    String blockHash,
    BigInteger sequenceNumber,
    String batcherAddr,
    BigInteger l1FeeOverhead,
    BigInteger l1FeeScalar) {

  private static final String L1_INFO_FUNC_SIGNATURE =
      "setL1BlockValues(uint64,uint64,uint256,bytes32,uint64,bytes32,uint256,uint256)";

  private static final int L1_INFO_LENGTH = 4 + 32 * 8;

  private static final byte[] SIGNATURE_BYTES =
      ArrayUtils.subarray(
          Hash.keccak256(L1_INFO_FUNC_SIGNATURE.getBytes(StandardCharsets.UTF_8)), 0, 4);

  /**
   * Parse tx data to L1BlockInfo.
   *
   * @param data bytes of tx data
   * @return L1BlockInfo Object
   */
  public static L1BlockInfo from(byte[] data) {
    if (data == null || data.length != L1_INFO_LENGTH) {
      throw new RuntimeException(
          String.format("data is unexpected length: %d", data == null ? 0 : data.length));
    }
    if (!Objects.deepEquals(ArrayUtils.subarray(data, 0, 4), SIGNATURE_BYTES)) {
      throw new RuntimeException("");
    }
    BigInteger number = Numeric.toBigInt(data, 4, 32);
    BigInteger time = Numeric.toBigInt(data, 36, 32);
    BigInteger baseFee = Numeric.toBigInt(data, 68, 32);
    String blockHash = Numeric.toHexString(ArrayUtils.subarray(data, 100, 32));
    BigInteger sequenceNumber = Numeric.toBigInt(data, 132, 32);
    String batcherAddr = Numeric.toHexString(ArrayUtils.subarray(data, 176, 20));
    BigInteger l1FeeOverhead = Numeric.toBigInt(data, 196, 32);
    BigInteger l1FeeScalar = Numeric.toBigInt(data, 228, 32);
    return new L1BlockInfo(
        number, time, baseFee, blockHash, sequenceNumber, batcherAddr, l1FeeOverhead, l1FeeScalar);
  }

  /**
   * L1BlockInfo instance converts to BlockId instance.
   *
   * @return BlockId instance
   */
  public BlockId toId() {
    return new BlockId(blockHash, number);
  }
}
