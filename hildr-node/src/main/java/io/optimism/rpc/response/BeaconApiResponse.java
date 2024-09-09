/*
 * Copyright 2023 q315xia@163.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

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
