package io.optimism.types.enums;

/** Various types of responses that the JSON-RPC component may produce. */
public enum JsonRpcResponseType {
    /** None json rpc response type. */
    NONE,
    /** Success json rpc response type. */
    SUCCESS,
    /** Error json rpc response type. */
    ERROR,
    /** Unauthorized json rpc response type. */
    UNAUTHORIZED
}
