package io.optimism.rpc.internal.response;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.base.MoreObjects;
import io.optimism.types.enums.JsonRpcError;
import io.optimism.types.enums.JsonRpcResponseType;
import java.util.Objects;

/** The type Json rpc error response. */
@JsonPropertyOrder({"jsonrpc", "id", "error"})
public class JsonRpcErrorResponse implements JsonRpcResponse {

    private final Object id;
    private final JsonRpcError error;

    /**
     * Instantiates a new Json rpc error response.
     *
     * @param id the id
     * @param error the error
     */
    public JsonRpcErrorResponse(final Object id, final JsonRpcError error) {
        this.id = id;
        this.error = error;
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    @JsonGetter("id")
    public Object getId() {
        return id;
    }

    /**
     * Gets error.
     *
     * @return the error
     */
    @JsonGetter("error")
    public JsonRpcError getError() {
        return error;
    }

    @Override
    @JsonIgnore
    public JsonRpcResponseType getType() {
        return JsonRpcResponseType.ERROR;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof JsonRpcErrorResponse)) {
            return false;
        }
        final JsonRpcErrorResponse that = (JsonRpcErrorResponse) o;
        return Objects.equals(id, that.id) && error == that.error;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, error);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("error", error)
                .toString();
    }
}
