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

package io.optimism.rpc.internal;

import io.vertx.ext.web.RoutingContext;
import java.util.Objects;

/**
 * json rpc request context.
 *
 * @author thinkAfCod
 * @since 2023.06
 */
@SuppressWarnings("UnusedVariable")
public class JsonRpcRequestContext {

  private final RoutingContext context;

  private final JsonRpcRequest jsonRpcRequest;

  /**
   * Instantiates a new Json rpc request context.
   *
   * @param context the context
   * @param jsonRpcRequest the json rpc request
   */
  public JsonRpcRequestContext(final RoutingContext context, final JsonRpcRequest jsonRpcRequest) {
    this.context = context;
    this.jsonRpcRequest = jsonRpcRequest;
  }

  /**
   * Gets request.
   *
   * @return the request
   */
  public JsonRpcRequest getRequest() {
    return jsonRpcRequest;
  }

  /**
   * Gets parameter.
   *
   * @param <T> the type parameter
   * @param index the index
   * @param paramClass the param class
   * @return the parameter
   */
  public <T> T getParameter(final int index, final Class<T> paramClass) {
    return jsonRpcRequest.getParameter(index, paramClass);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || (o instanceof JsonRpcRequestContext)) {
      return false;
    }
    final JsonRpcRequestContext that = (JsonRpcRequestContext) o;
    return Objects.equals(jsonRpcRequest, that.jsonRpcRequest);
  }

  @Override
  public int hashCode() {
    return Objects.hash(jsonRpcRequest);
  }
}
