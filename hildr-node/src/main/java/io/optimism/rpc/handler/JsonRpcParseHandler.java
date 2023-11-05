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

package io.optimism.rpc.handler;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.optimism.rpc.internal.response.JsonRpcError;
import io.optimism.rpc.internal.response.JsonRpcErrorResponse;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

/**
 * copied from besu.
 *
 * @author thinkAfCod
 * @since 2023.06
 */
public class JsonRpcParseHandler {

    private JsonRpcParseHandler() {}

    /**
     * Handler handler.
     *
     * @return the handler
     */
    public static Handler<RoutingContext> handler() {
        return ctx -> {
            final HttpServerResponse response = ctx.response();
            var body = ctx.body();
            if (body == null) {
                errorResponse(response, JsonRpcError.PARSE_ERROR);
            } else {
                try {
                    ctx.put("REQUEST_BODY_AS_JSON_OBJECT", body.asJsonObject());
                } catch (DecodeException | ClassCastException jsonObjectDecodeException) {
                    errorResponse(response, JsonRpcError.PARSE_ERROR);
                }
                ctx.next();
            }
        };
    }

    private static void errorResponse(final HttpServerResponse response, final JsonRpcError rpcError) {
        if (!response.closed()) {
            response.setStatusCode(HttpResponseStatus.BAD_REQUEST.code())
                    .end(Json.encode(new JsonRpcErrorResponse(null, rpcError)));
        }
    }
}
