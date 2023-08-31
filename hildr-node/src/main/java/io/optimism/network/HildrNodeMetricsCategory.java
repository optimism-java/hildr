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

package io.optimism.network;

import java.util.Optional;
import org.hyperledger.besu.plugin.services.metrics.MetricCategory;

/**
 * The enum HildrNodeMetricsCategory.
 *
 * @author grapebaba
 * @since 0.1.1
 */
public enum HildrNodeMetricsCategory implements MetricCategory {

  /** p2p network hildr node metrics category. */
  P2P_NETWORK("p2p_network");

  private final String name;

  HildrNodeMetricsCategory(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Optional<String> getApplicationPrefix() {
    return Optional.of("hildr_node");
  }
}
