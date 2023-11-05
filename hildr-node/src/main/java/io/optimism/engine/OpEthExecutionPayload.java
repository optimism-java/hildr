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

import io.optimism.engine.ExecutionPayload.ExecutionPayloadRes;
import org.web3j.protocol.core.Response;

/**
 * The type OpEthExecutionPayload.
 *
 * @author grapebaba
 * @since 0.1.0
 */
public class OpEthExecutionPayload extends Response<ExecutionPayloadRes> {

    /** Instantiates a new Op eth execution payload. */
    public OpEthExecutionPayload() {}

    /**
     * Gets execution payload.
     *
     * @return the execution payload
     */
    public ExecutionPayload getExecutionPayload() {
        return getResult().toExecutionPayload();
    }

    @Override
    public void setResult(ExecutionPayloadRes result) {
        super.setResult(result);
    }
}
