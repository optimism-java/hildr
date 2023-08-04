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

package io.optimism.batcher.loader;

import io.optimism.batcher.config.Config;

/**
 * L2 loader config.
 *
 * @param l2RpcUrl L2 rpc url
 * @param rollupUrl op-rollup node url
 * @author thinkAfCod
 * @since 0.1.1
 */
public record LoaderConfig(String l2RpcUrl, String rollupUrl) {

  /**
   * Create a LoaderConfig instance from Config instance.
   *
   * @param config Config instance
   * @return LoaderConfig instance
   */
  public static LoaderConfig from(Config config) {
    return new LoaderConfig(config.l2RpcUrl(), config.rollupRpcUrl());
  }
}
