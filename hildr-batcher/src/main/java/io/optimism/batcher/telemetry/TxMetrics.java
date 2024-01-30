package io.optimism.batcher.telemetry;

import java.math.BigInteger;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

/**
 * Metrics of transaction.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public interface TxMetrics {
    /**
     * Record current nonce.
     *
     * @param nonce The current nonce
     */
    void recordNonce(BigInteger nonce);

    /**
     * Record pending tx count.
     *
     * @param pending The pending tx count
     */
    void recordPendingTx(long pending);

    /**
     * Record tx confirmed fee.
     *
     * @param receipt The tx receipt
     */
    void txConfirmed(TransactionReceipt receipt);

    /**
     * Record gas bump count.
     *
     * @param times The times of gas bump count
     */
    void recordGasBumpCount(int times);

    /**
     * Record tx confirmation latency.
     *
     * @param latency The tx confirmation latency
     */
    void recordTxConfirmationLatency(long latency);

    /**
     * Record tx published event. If reason not empty, will increase error count
     *
     * @param reason Error reason
     */
    void txPublished(String reason);

    /** Record rpc error count. */
    void rpcError();
}
