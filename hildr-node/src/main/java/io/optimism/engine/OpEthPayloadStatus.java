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

import io.optimism.engine.ExecutionPayload.PayloadStatus;
import org.web3j.protocol.core.Response;

/** The type OpEthPayloadStatus. */
public class OpEthPayloadStatus extends Response<PayloadStatus> {

  /** Instantiates a new Op eth payload status. */
  public OpEthPayloadStatus() {}

  /**
   * Gets payload status.
   *
   * @return the payload status
   */
  public PayloadStatus getPayloadStatus() {
    return getResult();
  }

  @Override
  public void setResult(PayloadStatus result) {
    super.setResult(result);
  }
}
