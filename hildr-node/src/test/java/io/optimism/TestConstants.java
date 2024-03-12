package io.optimism;

import io.optimism.config.Config;
import java.util.Map;

/**
 * The type Test constants.
 *
 * @author thinkAfCod
 * @since 2023.06
 */
public class TestConstants {

    private TestConstants() {}

    /** The constant isConfiguredApiKeyEnv. */
    public static boolean isConfiguredApiKeyEnv = false;

    private static final String ETH_API_ENV = "ETH_API_KEY";
    private static final String OPT_API_ENV = "OPT_API_KEY";

    /** The L 1 rpc url format. */
    static String l1RpcUrlFormat = "https://eth-sepolia.g.alchemy.com/v2/%s";

    static String l1RpcWsUrlFormat = "wss://eth-sepolia.g.alchemy.com/v2/%s";

    static String l1RpcBeaconUrlFormat = "%s";

    /** The L 2 rpc url format. */
    static String l2RpcUrlFormat = "https://opt-sepolia.g.alchemy.com/v2/%s";

    /**
     * Create config config.
     *
     * @return the config
     */
    public static Config createConfig() {
        Map<String, String> envs = System.getenv();
        isConfiguredApiKeyEnv = envs.containsKey(ETH_API_ENV) && envs.containsKey(OPT_API_ENV);
        if (!isConfiguredApiKeyEnv) {
            return null;
        }
        var l1RpcUrl = l1RpcUrlFormat.formatted(envs.get(ETH_API_ENV));
        var l1WsRpcUrl = l1RpcWsUrlFormat.formatted(envs.get(ETH_API_ENV));
        var l1BeaconRpcUrl = l1RpcBeaconUrlFormat.formatted(envs.get(ETH_API_ENV));
        var l2RpcUrl = l2RpcUrlFormat.formatted(envs.get(OPT_API_ENV));
        Config.CliConfig cliConfig = new Config.CliConfig(
                l1RpcUrl, l1WsRpcUrl, l1BeaconRpcUrl, l1BeaconRpcUrl, l2RpcUrl, null, "testjwt", null, null, false);
        return Config.create(null, cliConfig, Config.ChainConfig.optimismGoerli());
    }
}
