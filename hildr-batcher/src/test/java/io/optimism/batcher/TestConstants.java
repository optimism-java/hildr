package io.optimism.batcher;

import java.util.Map;
import org.apache.commons.lang3.StringUtils;

/**
 * The type Test constants.
 *
 * @author thinkAfCod
 * @since 2023.06
 */
public class TestConstants {

    /** The constant isConfiguredApiKeyEnv. */
    public static boolean isConfiguredApiKeyEnv = false;

    private static final String ETH_API_ENV = "ETH_API_KEY";
    private static final String OPT_API_ENV = "OPT_API_KEY";
    private static final String ROLLUP_API_KEY = "ROLLUP_API_KEY";

    /** The L 1 rpc url format. */
    static String l1RpcUrlFormat = "https://eth-goerli.g.alchemy.com/v2/%s";

    /** The L 2 rpc url format. */
    static String l2RpcUrlFormat = "https://opt-goerli.g.alchemy.com/v2/%s";

    public static String l1RpcUrl;

    public static String l2RpcUrl;

    public static String rollupRpcUrl;

    static {
        Map<String, String> envs = System.getenv();
        String ethApiKey = envs.get(ETH_API_ENV);
        String optApiKey = envs.get(OPT_API_ENV);
        String rollUpApiUrl = envs.get(ROLLUP_API_KEY);

        if (!StringUtils.isEmpty(ethApiKey)) {
            l1RpcUrl = l1RpcUrlFormat.formatted(ethApiKey);
        }
        if (!StringUtils.isEmpty(optApiKey)) {
            l2RpcUrl = l2RpcUrlFormat.formatted(optApiKey);
        }
        if (!StringUtils.isEmpty(rollUpApiUrl)) {
            rollupRpcUrl = rollUpApiUrl;
        }
    }

    private TestConstants() {}
}
