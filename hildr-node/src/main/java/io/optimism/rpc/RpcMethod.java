package io.optimism.rpc;

import java.util.HashSet;

/**
 * method handler of rpc.
 *
 * @author thinkAfCod
 * @since 2023.06
 */
public enum RpcMethod {

    /** optimism_outputAtBlock api. */
    OP_OUTPUT_AT_BLOCK("optimism_outputAtBlock"),
    /** optimism_syncStatus api. */
    OP_SYNC_STATUS("optimism_syncStatus"),
    /** optimism_rollupConfig api. */
    OP_ROLLUP_CONFIG("optimism_rollupConfig");

    private final String rpcMethodName;

    private static final HashSet<String> allMethodNames;

    static {
        allMethodNames = new HashSet<>();
        for (RpcMethod m : RpcMethod.values()) {
            allMethodNames.add(m.getRpcMethodName());
        }
    }

    RpcMethod(String rpcMethodName) {
        this.rpcMethodName = rpcMethodName;
    }

    /**
     * Gets rpc method name.
     *
     * @return the rpc method name
     */
    public String getRpcMethodName() {
        return rpcMethodName;
    }

    /**
     * Rpc method exists boolean.
     *
     * @param rpcMethodName the rpc method name
     * @return the boolean
     */
    public static boolean rpcMethodExists(final String rpcMethodName) {
        return allMethodNames.contains(rpcMethodName);
    }
}
