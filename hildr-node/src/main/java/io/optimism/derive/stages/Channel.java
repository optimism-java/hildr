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

package io.optimism.derive.stages;

import java.math.BigInteger;

/**
 * The type Channel.
 *
 * @param id the id
 * @param data the data
 * @param l1InclusionBlock L1 inclusion block
 * @author grapebaba
 * @since 0.1.0
 */
public record Channel(BigInteger id, byte[] data, BigInteger l1InclusionBlock) {

  /**
   * From channel.
   *
   * @param pendingChannel the pending channel
   * @return the channel
   */
  public static Channel from(PendingChannel pendingChannel) {
    return new Channel(
        pendingChannel.getChannelId(),
        pendingChannel.assemble(),
        pendingChannel.l1InclusionBlock());
  }
}
