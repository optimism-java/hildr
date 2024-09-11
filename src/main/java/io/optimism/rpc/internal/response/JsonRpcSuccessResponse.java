package io.optimism.rpc.internal.response;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.optimism.types.enums.JsonRpcResponseType;
import java.util.Objects;

/**
 * json rpc success response. Copied from besu.
 *
 * @author thinkAfCod
 * @since 2023.06
 */
@JsonPropertyOrder({"jsonrpc", "id", "result"})
public class JsonRpcSuccessResponse implements JsonRpcResponse {

    /** The constant SUCCESS_RESULT. */
    public static final String SUCCESS_RESULT = "Success";

    private final Object id;
    private final Object result;

    /**
     * Instantiates a new Json rpc success response.
     *
     * @param id the id
     * @param result the result
     */
    public JsonRpcSuccessResponse(final Object id, final Object result) {
        this.id = id;
        this.result = result;
    }

    /**
     * Instantiates a new Json rpc success response.
     *
     * @param id the id
     */
    public JsonRpcSuccessResponse(final Object id) {
        this.id = id;
        this.result = SUCCESS_RESULT;
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
     * Gets result.
     *
     * @return the result
     */
    @JsonGetter("result")
    public Object getResult() {
        return result;
    }

    @Override
    @JsonIgnore
    public JsonRpcResponseType getType() {
        return JsonRpcResponseType.SUCCESS;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof JsonRpcSuccessResponse)) {
            return false;
        }
        final JsonRpcSuccessResponse that = (JsonRpcSuccessResponse) o;
        return Objects.equals(id, that.id) && Objects.equals(result, that.result);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, result);
    }
}
