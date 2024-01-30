package io.optimism.rpc.internal;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.math.BigInteger;
import java.util.Objects;

/**
 * Json rpc request id.
 *
 * @author thinkAfCod
 * @since 2023.06
 */
public class JsonRpcRequestId {

    private static final Class<?>[] VALID_ID_TYPES =
            new Class<?>[] {String.class, Integer.class, Long.class, Float.class, Double.class, BigInteger.class};

    private final Object id;

    /**
     * Instantiates a new Json rpc request id.
     *
     * @param id the id
     */
    @JsonCreator
    public JsonRpcRequestId(final Object id) {
        if (isRequestTypeInvalid(id)) {
            throw new RuntimeException("Invalid id");
        }
        this.id = id;
    }

    /**
     * Gets value.
     *
     * @return the value
     */
    @JsonValue
    public Object getValue() {
        return id;
    }

    private boolean isRequestTypeInvalid(final Object id) {
        return isNotNull(id) && isTypeInvalid(id);
    }

    /**
     * The JSON spec says "The use of Null as a value for the id member in a Request object is
     * discouraged" Both geth and parity accept null values, so we decided to support them as well.
     */
    private boolean isNotNull(final Object id) {
        return id != null;
    }

    private boolean isTypeInvalid(final Object id) {
        for (final Class<?> validType : VALID_ID_TYPES) {
            if (validType.isInstance(id)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof JsonRpcRequestId that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
