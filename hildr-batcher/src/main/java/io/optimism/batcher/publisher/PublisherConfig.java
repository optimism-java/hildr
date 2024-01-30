package io.optimism.batcher.publisher;

import io.optimism.batcher.config.Config;
import io.optimism.batcher.telemetry.BatcherMetrics;
import io.optimism.type.RollupConfigResult;
import java.math.BigInteger;

/**
 * Publisher Config class.
 *
 * @param l1RpcUrl L1 rpc url
 * @param l1Signer L1 signer private key
 * @param l1chainId L1 chain id
 * @param batchInboxAddress Address of BatchInboxContract on L1
 * @param metrics Batcher metrics
 * @author thinkAfCod
 * @since 0.1.1
 */
public record PublisherConfig(
        String l1RpcUrl, String l1Signer, BigInteger l1chainId, String batchInboxAddress, BatcherMetrics metrics) {

    /**
     * Create a PublisherConfig instance from Config instance.
     *
     * @param config Config instance
     * @param rollupConfig Rollup config, get from rollup node api
     * @return PublisherConfig instance
     */
    public static PublisherConfig from(Config config, RollupConfigResult rollupConfig) {
        return new PublisherConfig(
                config.l1RpcUrl(),
                config.l1Signer(),
                rollupConfig.getL1ChainId(),
                rollupConfig.getBatchInboxAddress(),
                config.metrics());
    }
}
