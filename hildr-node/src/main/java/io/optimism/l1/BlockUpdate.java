package io.optimism.l1;

import java.math.BigInteger;

/**
 * the BlockUpdate class.
 *
 * @param updateType
 * @param
 * @author thinkAfCod
 * @since 2023.05
 */
public record BlockUpdate (BlockUpdateType updateType, Object value) {

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
