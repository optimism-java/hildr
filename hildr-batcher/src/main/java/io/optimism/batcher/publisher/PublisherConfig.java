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

package io.optimism.batcher.publisher;

import io.optimism.batcher.config.Config;
import io.optimism.batcher.telemetry.BatcherMetrics;
import io.optimism.type.RollupConfigResutl;
import java.math.BigInteger;

/**
 * Publisher Config class.
 *
 * @param l1RpcUrl L1 rpc url
 * @param l1Signer L1 signer private key
 * @param l1chainId L1 chain id
 * @param batchInboxAddress Address of BatchInboxContract on L1
 * @param metrics Batcher metrics
 * @author thinkAfCod
 * @since 0.1.1
 */
public record PublisherConfig(
    String l1RpcUrl,
    String l1Signer,
    BigInteger l1chainId,
    String batchInboxAddress,
    BatcherMetrics metrics) {

  /**
   * Create a PublisherConfig instance from Config instance.
   *
   * @param config Config instance
   * @param rollupConfig Rollup config, get from rollup node api
   * @return PublisherConfig instance
   */
  public static PublisherConfig from(Config config, RollupConfigResutl rollupConfig) {
    return new PublisherConfig(
        config.l1RpcUrl(),
        config.l1Signer(),
        rollupConfig.getL1ChainId(),
        rollupConfig.getBatchInboxAddress(),
        config.metrics());
  }
}
