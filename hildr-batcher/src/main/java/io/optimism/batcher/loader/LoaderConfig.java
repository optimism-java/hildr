package io.optimism.batcher.loader;

import io.optimism.batcher.config.Config;
import io.optimism.batcher.telemetry.BatcherMetrics;

/**
 * L2 loader config.
 *
 * @param l2RpcUrl L2 rpc url
 * @param rollupUrl op-rollup node url
 * @param metrics Batcher metrics
 * @author thinkAfCod
 * @since 0.1.1
 */
public record LoaderConfig(String l2RpcUrl, String rollupUrl, BatcherMetrics metrics) {

    /**
     * Create a LoaderConfig instance from Config instance.
     *
     * @param config Config instance
     * @return LoaderConfig instance
     */
    public static LoaderConfig from(Config config) {
        return new LoaderConfig(config.l2RpcUrl(), config.rollupRpcUrl(), config.metrics());
    }
}
