package io.optimism.rpc.internal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Objects;

/**
 * copied from project besu(<a href="https://github.com/hyperledger/besu">...</a>).
 *
 * @author thinkAfCod
 * @since 2023.06
 */
public class JsonRpcRequest {

    private static final ObjectMapper mapper = new ObjectMapper();

    private JsonRpcRequestId id;
    private final String method;
    private final Object[] params;
    private final String version;
    private boolean isNotification = true;

    /**
     * Instantiates a new Json rpc request.
     *
     * @param version the version
     * @param method the method
     * @param params the params
     */
    @JsonCreator
    public JsonRpcRequest(
            @JsonProperty("jsonrpc") final String version,
            @JsonProperty("method") final String method,
            @JsonProperty("params") final Object[] params) {
        this.version = version;
        this.method = method;
        this.params = params;
        if (method == null) {
            throw new RuntimeException("Field 'method' is required");
        }
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    @JsonGetter("id")
    public Object getId() {
        return id == null ? null : id.getValue();
    }

    /**
     * Gets method.
     *
     * @return the method
     */
    @JsonGetter("method")
    public String getMethod() {
        return method;
    }

    /**
     * Gets version.
     *
     * @return the version
     */
    @JsonGetter("jsonrpc")
    public String getVersion() {
        return version;
    }

    /**
     * Get params object [ ].
     *
     * @return the object [ ]
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonGetter("params")
    public Object[] getParams() {
        return params;
    }

    /**
     * Is notification boolean.
     *
     * @return the boolean
     */
    @JsonIgnore
    public boolean isNotification() {
        return isNotification;
    }

    /**
     * Gets param length.
     *
     * @return the param length
     */
    @JsonIgnore
    public int getParamLength() {
        return hasParams() ? params.length : 0;
    }

    /**
     * Has params boolean.
     *
     * @return the boolean
     */
    @JsonIgnore
    public boolean hasParams() {

        // Null Object: "params":null
        if (params == null) {
            return false;
        }

        // Null Array: "params":[null]
        return params.length != 0 && params[0] != null;
    }

    /**
     * Sets id.
     *
     * @param id the id
     */
    @JsonSetter("id")
    public void setId(final JsonRpcRequestId id) {
        // If an id is explicitly set, it is not a notification
        isNotification = false;
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JsonRpcRequest that)) return false;
        return isNotification == that.isNotification
                && Objects.equals(id, that.id)
                && Objects.equals(method, that.method)
                && Arrays.equals(params, that.params)
                && Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(id, method, version, isNotification);
        result = 31 * result + Arrays.hashCode(params);
        return result;
    }

    /**
     * Gets parameter.
     *
     * @param <T> the type parameter
     * @param index the index
     * @param paramClass the param class
     * @return the parameter
     */
    @SuppressWarnings("unchecked")
    public <T> T getParameter(final int index, final Class<T> paramClass) {
        if (params == null || params.length <= index || params[index] == null) {
            return null;
        }

        final T param;
        final Object rawParam = params[index];
        if (paramClass.isAssignableFrom(rawParam.getClass())) {
            param = (T) rawParam;
        } else {
            try {
                final String json = mapper.writeValueAsString(rawParam);
                param = mapper.readValue(json, paramClass);
            } catch (final JsonProcessingException e) {
                throw new IllegalArgumentException(
                        String.format(
                                "Invalid json rpc parameter at index %d. Supplied value was: '%s' of type: '%s' - expected type: '%s'",
                                index, rawParam, rawParam.getClass().getName(), paramClass.getName()),
                        e);
            }
        }

        return param;
    }

    @Override
    public String toString() {
        return "JsonRpcRequest{id=%s, method='%s', params=%s, version='%s', isNotification=%s}"
                .formatted(id, method, Arrays.toString(params), version, isNotification);
    }
}
