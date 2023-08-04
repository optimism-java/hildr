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

import org.apache.commons.lang3.ArrayUtils;

/**
 * Channel interface. cache batch submit data.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public interface Channel {

  /** Derivation version. */
  byte DERIVATION_VERSION_0 = 0;

  /**
   * Channel Tx Data class.
   *
   * @param data L2 block data that will send to L1
   * @param channelId channelId
   * @param frameNumber channel frame number
   */
  record TxData(byte[] data, byte[] channelId, int frameNumber) {
    /**
     * Get tx bytes.
     *
     * @return tx bytes
     */
    public byte[] txBytes() {
      return ArrayUtils.addAll(new byte[] {DERIVATION_VERSION_0}, data());
    }
  }
}
