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

/** ## ExecutionPayload. */
public record ExecutionPayload(
    /** A 32 byte hash of the parent payload. */
    String parentHash,
    /** A 20 byte hash (aka Address) for the feeRecipient field of the new payload. */
    String feeRecipient,
    /** A 32 byte state root hash. */
    String stateRoot,
    /** A 32 byte receipt root hash. */
    String receiptsRoot,
    /** A 32 byte logs bloom filter. */
    String logsBloom,
    /** A 32 byte beacon chain randomness value. */
    String prevRandom,
    /** A 64 bit number for the current block index. */
    BigInteger blockNumber,
    /** A 64 bit value for the gas limit. */
    BigInteger gasLimit,
    /** A 64 bit value for the gas used. */
    BigInteger gasUsed,
    /** A 64 bit value for the timestamp field of the new payload. */
    BigInteger timestamp,
    /** 0 to 32 byte value for extra data. */
    String extraData,
    /** 256 bits for the base fee per gas. */
    BigInteger baseFeePerGas,
    /** The 32 byte block hash. */
    String blockHash,
    /** An array of transaction objects where each object is a byte list. */
    List<RawTransaction> transactions) {}
