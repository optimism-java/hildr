package io.optimism.rpc.response;

import java.util.Objects;

/**
 * The BeaconApiResponse class.
 *
 * @author thinkAfCod
 * @since 0.3.0
 * @param <T> the beacon api response data type.
 */
public class BeaconApiResponse<T> {

    /**
     * The response inner data.
     */
    public T data;

    /**
     * The BeaconApiResponse constructor.
     */
    public BeaconApiResponse() {}

    /**
     * The BeaconApiResponse constructor.
     *
     * @param data the data.
     */
    public BeaconApiResponse(T data) {
        this.data = data;
    }

    /**
     * Get data.
     *
     * @return the data
     */
    public T getData() {
        return data;
    }

    /**
     * Set data value.
     *
     * @param data the data
     */
    public void setData(T data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BeaconApiResponse that)) {
            return false;
        }
        return Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }
}
