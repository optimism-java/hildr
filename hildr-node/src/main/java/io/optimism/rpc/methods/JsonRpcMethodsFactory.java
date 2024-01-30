package io.optimism.rpc.methods;

import io.optimism.config.Config;
import java.util.HashMap;
import java.util.Map;

/**
 * JsonRpc method factory. copied from besu.
 *
 * @author thinkAfCod
 * @since 2023.06
 */
public class JsonRpcMethodsFactory {

    /** JsonRpcMethodsFactory constructor. */
    public JsonRpcMethodsFactory() {}

    /**
     * Methods map.
     *
     * @param config the config
     * @return the map
     */
    public Map<String, JsonRpcMethod> methods(Config config) {
        final Map<String, JsonRpcMethod> methods = new HashMap<>();
        JsonRpcMethod outputAtBlock =
                new OutputAtBlock(config.l2RpcUrl(), config.chainConfig().l2Tol1MessagePasser());

        methods.put(outputAtBlock.getName(), outputAtBlock);

        return methods;
    }
}
