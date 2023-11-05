/*
 * Copyright 2023 q315xia@163.com
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

package io.optimism.batcher.config;

import io.optimism.batcher.telemetry.BatcherMetrics;
import java.util.Objects;

/**
 * Batcher config.
 *
 * @param l1RpcUrl L1 rpc url
 * @param l2RpcUrl L2 rpc url
 * @param rollupRpcUrl Op-node rpc url
 * @param l1Signer L1 signer private key
 * @param batchInboxAddress address of BatchInboxContract on l1
 * @param subSafetyMargin Sub-safety margin
 * @param pollInterval Milliseconds of poll interval
 * @param maxL1TxSize Max L1 Tx Size
 * @param targetFrameSize The target of frame size
 * @param targetNumFrames The target of frame number
 * @param approxComprRatio Compress ratio
 * @param metrics Batcher metrics
 * @author thinkAfCod
 * @since 0.1.1
 */
public record Config(
    String l1RpcUrl,
    String l2RpcUrl,
    String rollupRpcUrl,
    String l1Signer,
    String batchInboxAddress,
    Long subSafetyMargin,
    Long pollInterval,
    Long maxL1TxSize,
    Integer targetFrameSize,
    Integer targetNumFrames,
    String approxComprRatio,
    BatcherMetrics metrics) {

  @Override
  public boolean equals(Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof Config that)) {
      return false;
    }
    return Objects.equals(this.l1RpcUrl, that.l1RpcUrl)
        && Objects.equals(this.l2RpcUrl, that.l2RpcUrl)
        && Objects.equals(this.rollupRpcUrl, that.rollupRpcUrl)
        && Objects.equals(this.l1Signer, that.l1Signer)
        && Objects.equals(this.batchInboxAddress, that.batchInboxAddress)
        && Objects.equals(this.subSafetyMargin, that.subSafetyMargin)
        && Objects.equals(this.pollInterval, that.pollInterval)
        && Objects.equals(this.maxL1TxSize, that.maxL1TxSize)
        && Objects.equals(this.targetFrameSize, that.targetFrameSize)
        && Objects.equals(this.targetNumFrames, that.targetNumFrames)
        && Objects.equals(this.approxComprRatio, that.approxComprRatio);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        l1RpcUrl,
        l2RpcUrl,
        rollupRpcUrl,
        l1Signer,
        batchInboxAddress,
        subSafetyMargin,
        pollInterval,
        maxL1TxSize,
        targetFrameSize,
        targetNumFrames,
        approxComprRatio);
  }

  @Override
  public String toString() {
    return "Config["
        + "l1RpcUrl="
        + l1RpcUrl
        + ", "
        + "l2RpcUrl="
        + l2RpcUrl
        + ", "
        + "rollupRpcUrl="
        + rollupRpcUrl
        + ", "
        + "l1Signer="
        + l1Signer
        + ", "
        + "batchInboxAddress="
        + batchInboxAddress
        + ", "
        + "subSafetyMargin="
        + subSafetyMargin
        + ", "
        + "pollInterval="
        + pollInterval
        + ", "
        + "maxL1TxSize="
        + maxL1TxSize
        + ", "
        + "targetFrameSize="
        + targetFrameSize
        + ", "
        + "targetNumFrames="
        + targetNumFrames
        + ", "
        + "approxComprRatio="
        + approxComprRatio
        + ']';
  }
}
