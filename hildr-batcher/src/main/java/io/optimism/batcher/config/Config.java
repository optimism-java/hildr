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
    Long maxL1TxSize) {}
