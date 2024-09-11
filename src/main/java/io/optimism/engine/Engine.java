package io.optimism.engine;

import io.optimism.types.ExecutionPayload;
import io.optimism.types.ExecutionPayload.PayloadAttributes;
import io.optimism.types.ForkChoiceUpdate.ForkchoiceState;
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
     * @param timestamp the payload timestamp
     * @param payloadId the payload id
     * @return the payload v2
     * @throws IOException the io exception
     * @see <a
     *     href="https://github.com/ethereum/execution-apis/blob/main/src/engine/shanghai.md#engine_getpayloadv2"></a>
     */
    OpEthExecutionPayload getPayload(BigInteger timestamp, BigInteger payloadId) throws IOException;
}
