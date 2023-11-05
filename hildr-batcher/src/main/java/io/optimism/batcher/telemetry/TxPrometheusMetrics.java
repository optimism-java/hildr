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

package io.optimism.batcher.telemetry;

import io.micrometer.core.instrument.MeterRegistry;
import io.optimism.utilities.telemetry.MetricsSupplier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

/**
 * The type of TxPrometheusMetrics. It would record metrics to prometheus metrics.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public class TxPrometheusMetrics implements TxMetrics {

    private static final Map<String, String> EMPTY_TAGS = new HashMap<>();

    private static final Map<String, String> DESC_MAP = new HashMap<>();

    private static final double[] FEE_GWEI_BUCKETS =
            new double[] {0.5, 1, 2, 5, 10, 20, 40, 60, 80, 100, 200, 400, 800, 1600};

    static {
        DESC_MAP.put("tx_fee_gwei", "L1 gas fee for transactions in GWEI.");
        DESC_MAP.put("tx_fee_gwei_total", "Sum of fees spent for all transactions in GWEI.");
        DESC_MAP.put("tx_fee_histogram_gwei", "Tx Fee in GWEI.");
        DESC_MAP.put("tx_gas_bump", "Number of times a transaction gas needed to be bumped before it got included.");
        DESC_MAP.put("tx_confirmed_latency_ms", "Latency of a confirmed transaction in milliseconds.");
        DESC_MAP.put("current_nonce", "Current nonce of the from address.");
        DESC_MAP.put("pending_txs", "Number of transactions pending receipts.");
        DESC_MAP.put("tx_publish_error_count", "Count of publish errors. Labels are sanitized error strings.");
        DESC_MAP.put("rpc_error_count", "Temporary: Count of RPC errors (like timeouts) that have occurred.");
    }

    private final MetricsSupplier metricsSupplier;

    /**
     * The TxPrometheusMetrics constructor.
     *
     * @param registry The meter registry instance.
     * @param namespace The prefix of metrics name.
     */
    public TxPrometheusMetrics(MeterRegistry registry, String namespace) {
        this.metricsSupplier = new MetricsSupplier(registry, namespace + "_tx", DESC_MAP);
    }

    @Override
    public void recordNonce(BigInteger nonce) {
        this.metricsSupplier.getOrCreateGauge("current_nonce", EMPTY_TAGS).getAndSet(nonce.longValue());
    }

    @Override
    public void recordPendingTx(long pending) {
        this.metricsSupplier.getOrCreateGauge("pending_txs", EMPTY_TAGS).getAndSet(pending);
    }

    @Override
    public void txConfirmed(TransactionReceipt receipt) {
        var effectiveGasPrice = new BigDecimal(Numeric.toBigInt(receipt.getEffectiveGasPrice()));
        var gasUsed = new BigDecimal(receipt.getGasUsed());
        BigDecimal fee = Convert.fromWei(effectiveGasPrice.divide(gasUsed, 2, RoundingMode.UP), Convert.Unit.GWEI);
        this.metricsSupplier.getOrCreateGaugeDecimal("tx_fee_gwei", EMPTY_TAGS).getAndSet(fee);
        this.metricsSupplier.getOrCreateCounter("tx_fee_gwei_total", EMPTY_TAGS).increment(fee.doubleValue());
        this.metricsSupplier
                .getOrCreateHistogram("tx_fee_histogram_gwei", "GWEI", FEE_GWEI_BUCKETS, EMPTY_TAGS)
                .record(fee.doubleValue());
    }

    @Override
    public void recordGasBumpCount(int times) {
        this.metricsSupplier.getOrCreateGauge("tx_gas_bump", EMPTY_TAGS).getAndSet(times);
    }

    @Override
    public void recordTxConfirmationLatency(long latency) {
        this.metricsSupplier
                .getOrCreateGauge("tx_confirmed_latency_ms", EMPTY_TAGS)
                .getAndSet(latency);
    }

    @Override
    public void txPublished(String reason) {
        if (StringUtils.isEmpty(reason)) {
            HashMap<String, String> tags = new HashMap<>();
            tags.put("error", reason);
            this.metricsSupplier
                    .getOrCreateCounter("tx_publish_error_count", tags)
                    .increment(1);
        } else {
            this.metricsSupplier.getOrCreateEventMeter("publish", EMPTY_TAGS).record();
        }
    }

    @Override
    public void rpcError() {
        this.metricsSupplier.getOrCreateCounter("rpc_error_count", EMPTY_TAGS).increment();
    }
}
