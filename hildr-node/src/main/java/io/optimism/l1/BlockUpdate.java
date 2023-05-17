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
 * @author thinkAfCod
 * @since 0.1.0
 */
public abstract class BlockUpdate {

  /** not public constructor. */
  private BlockUpdate() {}

  /** update new Block. */
  public static class NewBlock extends BlockUpdate {

    private L1Info l1Info;

    /**
     * NewBlock constructor.
     *
     * @param l1Info update l1Info
     */
    public NewBlock(L1Info l1Info) {
      this.l1Info = l1Info;
    }

    /**
     * get update L1Info.
     *
     * @return l1Info
     */
    public L1Info get() {
      return l1Info;
    }
  }

  /** update finalized Block. */
  public static class FinalityUpdate extends BlockUpdate {

    private BigInteger finalizedBlock;

    /**
     * FinalityUpdate constructor.
     *
     * @param finalizedBlock finalized block
     */
    public FinalityUpdate(BigInteger finalizedBlock) {
      this.finalizedBlock = finalizedBlock;
    }

    /**
     * get finalized block.
     *
     * @return finalized block
     */
    public BigInteger get() {
      return finalizedBlock;
    }
  }

  /** update Reorg. */
  public static class Reorg extends BlockUpdate {

    /** Reorg constructor. */
    public Reorg() {}
  }
}
