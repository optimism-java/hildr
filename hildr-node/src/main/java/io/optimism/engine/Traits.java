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

import java.math.BigInteger;
import java.util.Optional;

/**
 * ## Engine
 *
 * <p>A set of methods that allow a consensus client to interact with an execution engine. This is a
 * modified version of the [Ethereum Execution API
 * Specs](https://github.com/ethereum/execution-apis), as defined in the [Optimism Exec Engine
 * Specs](https://github.com/ethereum-optimism/optimism/blob/develop/specs/exec-engine.md).
 */
public interface Traits {
  /**
   * ## forkchoice_updated Updates were made to
   * [`engine_forkchoiceUpdatedV1`](https://github.com/ethereum/execution-apis/blob/main/src/engine/paris.md#engine_forkchoiceupdatedv1)
   * for L2. This updates which L2 blocks the engine considers to be canonical ([ForkchoiceState]
   * argument), and optionally initiates block production ([PayloadAttributes] argument). ###
   * Specification method: engine_forkchoiceUpdatedV1 params: - [ForkchoiceState] -
   * [PayloadAttributes] timeout: 8s returns: - [ForkChoiceUpdate] potential errors: - code and
   * message set in case an exception happens while the validating payload, updating the forkchoice
   * or initiating the payload build process. ### Reference See more details in the [Optimism
   * Specs](https://github.com/ethereum-optimism/optimism/blob/develop/specs/exec-engine.md#engine_forkchoiceupdatedv1).
   */
  ForkChoiceUpdate forkChoiceUpdated(
      ForkchoiceState forkchoiceState, Optional<PayloadAttributes> payloadAttributes);

  /**
   * ## new_payload No modifications to
   * [`engine_newPayloadV1`](https://github.com/ethereum/execution-apis/blob/main/src/engine/paris.md#engine_newpayloadv1)
   * were made for L2. Applies a L2 block to the engine state. ### Specification method:
   * engine_newPayloadV1 params: - [ExecutionPayload] timeout: 8s returns: - [PayloadStatus]
   * potential errors: - code and message set in case an exception happens while processing the
   * payload. ### Reference See more details in the [Optimism
   * Specs](https://github.com/ethereum-optimism/optimism/blob/develop/specs/exec-engine.md#engine_newPayloadv1).
   */
  PayloadStatus newPayload(ExecutionPayload executionPayload);

  /**
   * ## get_payload No modifications to
   * [`engine_getPayloadV1`](https://github.com/ethereum/execution-apis/blob/main/src/engine/paris.md#engine_getpayloadv1)
   * were made for L2. Retrieves a payload by ID, prepared by
   * [engine_forkchoiceUpdatedV1](EngineApi::engine_forkchoiceUpdatedV1) when called with
   * [PayloadAttributes]. ### Specification method: engine_getPayloadV1 params: - [PayloadId]: DATA,
   * 8 Bytes - Identifier of the payload build process timeout: 1s returns: - [ExecutionPayload]
   * potential errors: - code and message set in case an exception happens while getting the
   * payload. ### Reference See more details in the [Optimism
   * Specs](https://github.com/ethereum-optimism/optimism/blob/develop/specs/exec-engine.md#engine_getPayloadv1).
   */
  ExecutionPayload getPayload(BigInteger payloadId);
}
