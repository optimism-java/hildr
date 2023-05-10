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

package io.optimism.l1;

import java.math.BigInteger;

/**
 * the BlockUpdate class.
 *
 * @param updateType the enum of BlockUpdateType
 * @param value the block update value
 * @author thinkAfCod
 * @since 0.1.0
 */
public record BlockUpdate(BlockUpdateType updateType, Object value) {

  /**
   * create a BlockUpdate.
   *
   * @param updateType the block update type
   * @param value the block update value
   * @return a BlockUpdate instance
   */
  public static BlockUpdate create(BlockUpdateType updateType, Object value) {
    if (BlockUpdateType.NewBlock.equals(updateType)) {
      if (!(value instanceof L1Info)) {
        throw new IllegalArgumentException("value not instanceof L1Info");
      }
      return new BlockUpdate(updateType, value);
    } else if (BlockUpdateType.FinalityUpdate.equals(updateType)) {
      if (!(value instanceof BigInteger)) {
        throw new IllegalArgumentException("value not instanceof BigInteger");
      }
      return new BlockUpdate(updateType, value);
    } else if (BlockUpdateType.Reorg.equals(updateType)) {
      return new BlockUpdate(updateType, null);
    } else {
      throw new IllegalArgumentException("wrong block update type");
    }
  }
}
