package io.optimism.l1;

/**
 * the enum of BlockUpdate.
 *
 * @author thinkAfCod
 * @since 2023.05
 */
public enum BlockUpdateType {
  /// A new block extending the current chain
  NewBlock,
  /// Updates the most recent finalized block
  FinalityUpdate,
  /// Reorg detected
  Reorg;

}
