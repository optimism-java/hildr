package io.optimism.rpc.internal.response;

import com.fasterxml.jackson.annotation.JsonGetter;
import io.optimism.types.enums.JsonRpcResponseType;

/** The interface Json rpc response. */
public interface JsonRpcResponse {

    /**
     * Gets version.
     *
     * @return the version
     */
    @JsonGetter("jsonrpc")
    default String getVersion() {
        return "2.0";
    }

    /**
     * Gets type.
     *
     * @return the type
     */
    JsonRpcResponseType getType();
}
