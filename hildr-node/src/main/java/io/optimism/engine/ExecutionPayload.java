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

package io.optimism.engine;

import io.optimism.common.RawTransaction;
import java.math.BigInteger;
import java.util.List;

/**
 * The type ExecutionPayload.
 *
 * @param parentHash A 32 byte hash of the parent payload.
 * @param feeRecipient A 20 byte hash (aka Address) for the feeRecipient field of the new payload.
 * @param stateRoot A 32 byte state root hash.
 * @param receiptsRoot A 32 byte receipt root hash.
 * @param logsBloom A 32 byte logs bloom filter.
 * @param prevRandom A 32 byte beacon chain randomness value.
 * @param blockHash The 32 byte block hash.
 * @param gasLimit A 64 bit value for the gas limit.
 * @param gasUsed A 64 bit value for the gas used.
 * @param timestamp A 64 bit value for the timestamp field of the new payload.
 * @param baseFeePerGas 256 bits for the base fee per gas.
 * @param blockNumber A 64 bit number for the current block index.
 * @param extraData 0 to 32 byte value for extra data.
 * @param transactions An array of transaction objects where each object is a byte list.
 * @author zhouop0
 * @since 0.1.0
 */
public record ExecutionPayload(
    String parentHash,
    String feeRecipient,
    String stateRoot,
    String receiptsRoot,
    String logsBloom,
    String prevRandom,
    BigInteger blockNumber,
    BigInteger gasLimit,
    BigInteger gasUsed,
    BigInteger timestamp,
    String extraData,
    BigInteger baseFeePerGas,
    String blockHash,
    List<RawTransaction> transactions) {}
