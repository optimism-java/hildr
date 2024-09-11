package io.optimism.rpc.internal.response;

import io.optimism.types.enums.JsonRpcResponseType;

/** The type Json rpc no response. */
public class JsonRpcNoResponse implements JsonRpcResponse {

    /** Instantiates a new Json rpc no response. */
    public JsonRpcNoResponse() {}

    @Override
    public JsonRpcResponseType getType() {
        return JsonRpcResponseType.NONE;
    }
}
