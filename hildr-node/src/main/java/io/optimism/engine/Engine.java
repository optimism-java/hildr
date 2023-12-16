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

import io.optimism.engine.ExecutionPayload.PayloadAttributes;
import io.optimism.engine.ForkChoiceUpdate.ForkchoiceState;
import java.io.IOException;
import java.math.BigInteger;

/**
 * The type Engine.
 *
 * <p>A set of methods that allow a consensus client to interact with an execution engine. This is a
 * modified version of the <a href="https://github.com/ethereum/execution-apis">Ethereum Execution
 * API Specs</a>, as defined in the <a
 * href="https://github.com/ethereum-optimism/optimism/blob/develop/specs/exec-engine.md">Optimism
 * Exec Engine Specs</a>.
 *
 * @author zhouop0
 * @since 0.1.0
 */
public interface Engine {

    /**
     * This updates which L2 blocks the engine considers to be canonical {@code forkchoiceState}, and
     * optionally initiates block production {@code payloadAttributes}.
     *
     * <p>Specification method: engine_forkchoiceUpdatedV1 params: - [ForkchoiceState] -
     * [PayloadAttributes] timeout: 8s returns: - [ForkChoiceUpdate] potential errors: - code and
     * message set in case an exception happens while the validating payload, updating the forkchoice
     * or initiating the payload build process. Refer to <a
     * href="https://github.com/ethereum-optimism/optimism/blob/develop/specs/exec-engine.md#engine_forkchoiceupdatedv1">Optimism
     * Specs</a>.
     *
     * @param forkchoiceState the forkchoice state
     * @param payloadAttributes the payload attributes
     * @return the fork choice update
     * @throws IOException the io exception
     * @see <a
     *     href="https://github.com/ethereum/execution-apis/blob/main/src/engine/paris.md#engine_forkchoiceupdatedv1">engine_forkchoiceUpdatedV1</a>
     */
    OpEthForkChoiceUpdate forkchoiceUpdated(ForkchoiceState forkchoiceState, PayloadAttributes payloadAttributes)
            throws IOException;

    /**
     * Applies a L2 block to the engine state.
     *
     * <p>Specification method:engine_newPayloadV1 params: - [ExecutionPayload] timeout: 8s returns: -
     * [PayloadStatus] potential errors: - code and message set in case an exception happens while
     * processing the payload. Refer to <a
     * href="https://github.com/ethereum-optimism/optimism/blob/develop/specs/exec-engine.md#engine_newPayloadv1">Optimism
     * Specs</a>
     *
     * @param executionPayload the execution payload
     * @return the payload status
     * @throws IOException the io exception
     * @see <a
     *     href="https://github.com/ethereum/execution-apis/blob/main/src/engine/paris.md#engine_newpayloadv1">engine_newPayloadV1</a>
     */
    OpEthPayloadStatus newPayload(ExecutionPayload executionPayload) throws IOException;

    /**
     * Retrieves a payload by ID, prepared by {forkChoiceUpdated} when called with {@code
     * payloadAttributes}.
     *
     * <p>Specification method: engine_getPayloadV2 params: - [PayloadId]: DATA, 8 Bytes - Identifier
     * of the payload build process timeout: 1s returns: - [ExecutionPayload] potential errors: - code
     * and message set in case an exception happens while getting the payload. Refer to <a
     *     href="https://github.com/ethereum-optimism/optimism/blob/develop/specs/exec-engine.md#engine_getpayloadv2">Optimism Specs</a>
     *
     * @param payloadId the payload id
     * @return the payload v2
     * @throws IOException the io exception
     * @see <a
     *     href="https://github.com/ethereum/execution-apis/blob/main/src/engine/shanghai.md#engine_getpayloadv2"></a>
     */
    OpEthExecutionPayload getPayloadV2(BigInteger payloadId) throws IOException;
}
