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

package io.optimism.batcher.channel;

import io.optimism.batcher.config.Config;

/**
 * ChannelConfig class.
 *
 * @param channelTimeout The maximum number of L1 blocks that the inclusion transactions of a
 *     channel's frames can span.
 * @param maxChannelDuration If 0, duration checks are disabled.
 * @param maxFrameSize The maximum byte-size a frame can have.
 * @author thinkAfCod
 * @since 0.1.1
 */
public record ChannelConfig(long channelTimeout, long maxChannelDuration, int maxFrameSize) {

  /**
   * Create a ChannelConfig instance from Config instance.
   *
   * @param config Config instance
   * @return ChannelConfig instance
   */
  public static ChannelConfig from(Config config) {
    return new ChannelConfig(30000, 0, 120_000);
  }
}
