package io.optimism.type;

/**
 * TxCandidate is a transaction candidate that can be submitted to ask to construct a transaction
 * with gas price bounds.
 *
 * @param txData the transaction data to be used in the constructed tx.
 * @param address To is the recipient of the constructed tx. Nil means contract creation.
 * @param gasLimit the gas limit to be used in the constructed tx.
 * @author thinkAfCod
 * @since 0.1.1
 */
public record TxCandidate(byte[] txData, String address, long gasLimit) {}
