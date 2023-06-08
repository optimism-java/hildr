/*
 * Copyright ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package io.optimism.rpc.internal.response;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.google.common.base.MoreObjects;
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
    return MoreObjects.toStringHelper(this).add("id", id).add("error", error).toString();
  }
}
