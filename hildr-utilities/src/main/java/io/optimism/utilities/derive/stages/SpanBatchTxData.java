package io.optimism.utilities.derive.stages;

import org.hyperledger.besu.datatypes.TransactionType;

public interface SpanBatchTxData {

    TransactionType txType();
}
