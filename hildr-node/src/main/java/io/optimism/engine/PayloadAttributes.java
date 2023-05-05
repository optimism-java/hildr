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

import io.optimism.common.Epoch;
import java.math.BigInteger;
import java.util.List;

/**
 * The type PayloadAttributes.
 *
 * <p>L2 extended payload attributes for Optimism. For more details, visit the [Optimism specs](<a
 * href="https://github.com/ethereum-optimism/optimism/blob/develop/specs/exec-engine.md#extended-payloadattributesv1">...</a>).
 *
 * @param timestamp 64 bit value for the timestamp field of the new payload.
 * @param prevRandao 32 byte value for the prevRandao field of the new payload.
 * @param suggestedFeeRecipient 20 bytes suggested value for the feeRecipient field of the new
 *     payload.
 * @param transactions List of transactions to be included in the new payload.
 * @param noTxPool Boolean value indicating whether the payload should be built without including
 *     transactions from the txpool.
 * @param gasLimit 64 bit value for the gasLimit field of the new payload.The gasLimit is optional
 *     w.r.t. compatibility with L1, but required when used as rollup.This field overrides the gas
 *     limit used during block-building.If not specified as rollup, a STATUS_INVALID is returned.
 * @param epoch The batch epoch number from derivation. This value is not expected by the engine is
 *     skipped during serialization and deserialization.
 * @param l1InclusionBlock The L1 block number when this batch was first fully derived. This value
 *     is not expected by the engine and is skipped during serialization and deserialization.
 * @param seqNumber The L2 sequence number of the block. This value is not expected by the engine
 *     and is skipped during serialization and deserialization.
 *
 * @author zhouop0
 * @since 0.1.0
 */
public record PayloadAttributes(
    BigInteger timestamp,
    String prevRandao,
    String suggestedFeeRecipient,
    List<String> transactions,
    boolean noTxPool,
    BigInteger gasLimit,
    Epoch epoch,
    BigInteger l1InclusionBlock,
    BigInteger seqNumber) {}
