package io.optimism.proposer.config;

/**
 * The proposer config.
 *
 * @param l2ChainId The chain ID for L2.
 * @param l1RpcUrl The HTTP URL for L1.
 * @param l2RpcUrl The HTTP URL for L2.
 * @param rollupRpc The HTTP URL for the rollup node.
 * @param l2Signer The signer for L2.
 * @param l2OutputOracleAddr The L2OutputOracle contract address.
 * @param dgfContractAddr The DisputeGameFactory contract address.
 * @param pollInterval The delay between querying L2 for more transaction and creating a new batch.
 * @param networkTimeout network timeout.
 * @param allowNonFinalized set to true to propose outputs for L2 blocks derived from non-finalized
 *     L1 data
 * @author thinkAfCod
 * @since 0.1.1
 */
public record Config(
        Long l2ChainId,
        String l1RpcUrl,
        String l2RpcUrl,
        String rollupRpc,
        String l2Signer,
        String l2OutputOracleAddr,
        String dgfContractAddr,
        Long pollInterval,
        Long networkTimeout,
        Boolean allowNonFinalized) {}
