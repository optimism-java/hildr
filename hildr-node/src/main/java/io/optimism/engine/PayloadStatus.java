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

import org.web3j.protocol.core.Response;

/**
 * The type PayloadStatus.
 *
 * <p>status The status of the payload. latestValidHash 32 Bytes - the hash of the most recent valid
 * block in the branch defined by payload and its ancestors. validationError A message providing
 * additional details on the validation error if the payload is classified as INVALID or
 * INVALID_BLOCK_HASH.
 *
 * @author zhouop0
 * @since 0.1.0
 */
public class PayloadStatus extends Response {
  private Status status;
  private String latestValidHash;
  private String validationError;

  /** PayloadStatus constructor. */
  public PayloadStatus() {}

  /**
   * status get method.
   *
   * @return status.
   */
  public Status getStatus() {
    return status;
  }

  /**
   * status set method.
   *
   * @param status status.
   */
  public void setStatus(Status status) {
    this.status = status;
  }

  /**
   * latestValidHash get method.
   *
   * @return latestValidHash.
   */
  public String getLatestValidHash() {
    return latestValidHash;
  }

  /**
   * latestValidHash set method.
   *
   * @param latestValidHash latestValidHash.
   */
  public void setLatestValidHash(String latestValidHash) {
    this.latestValidHash = latestValidHash;
  }

  /**
   * validationError get method.
   *
   * @return validationError.
   */
  public String getValidationError() {
    return validationError;
  }

  /**
   * validationError set method.
   *
   * @param validationError validationError.
   */
  public void setValidationError(String validationError) {
    this.validationError = validationError;
  }
}
