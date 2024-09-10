package io.optimism.types;

import org.hyperledger.besu.datatypes.TransactionType;

/**
 * The interface SpanBatchTxData.
 *
 * @author grapebaba
 * @since 0.2.4
 */
public interface SpanBatchTxData {

    /**
     * Tx type transaction type.
     *
     * @return the transaction type
     */
    TransactionType txType();
}
