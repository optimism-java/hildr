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
import org.web3j.protocol.core.Response;

/**
 * The type ForkChoiceUpdate.
 *
 * payloadStatus Payload status. Note: values of the status field in the context of this
 *     method are restricted to the following subset: VALID, INVALID, SYNCING.
 * payloadId 8 byte identifier of the payload build process or null
 *
 * @author zhouop0
 * @since 0.1.0
 */
public class ForkChoiceUpdate extends Response {
  private PayloadStatus payloadStatus;
  private BigInteger payloadId;

  public PayloadStatus getPayloadStatus() {
    return payloadStatus;
  }

  public void setPayloadStatus(PayloadStatus payloadStatus) {
    this.payloadStatus = payloadStatus;
  }

  public BigInteger getPayloadId() {
    return payloadId;
  }

  public void setPayloadId(BigInteger payloadId) {
    this.payloadId = payloadId;
  }
}

