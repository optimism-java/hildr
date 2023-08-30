/*
 * Copyright 2023-2811 281165273grape@gmail.com
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
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package io.optimism.rpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.optimism.TestConstants;
import io.optimism.config.Config;
import io.optimism.rpc.internal.JsonRpcRequest;
import io.optimism.rpc.internal.JsonRpcRequestId;
import io.optimism.rpc.internal.result.OutputRootResult;
import io.optimism.utilities.telemetry.Logging;
import io.optimism.utilities.telemetry.TracerTaskWrapper;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Future;
import jdk.incubator.concurrent.StructuredTaskScope;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * rpc server test.
 *
 * @author thinkAfCod
 * @since 2023.06
 */
public class RpcServerTest {

  private static final Logger logger = LoggerFactory.getLogger(RpcServerTest.class);

  private static Config config;

  @BeforeAll
  static void setUp() {
    TracerTaskWrapper.setTracerSupplier(Logging.INSTANCE::getTracer);
    config = TestConstants.createConfig();
  }

  RpcServer createRpcServer(Config config) {
    return new RpcServer(config);
  }

  @Test
  void testRpcServerStart() throws Exception {
    if (!TestConstants.isConfiguredApiKeyEnv) {
      return;
    }

    RpcServer rpcServer = createRpcServer(config);
    try {
      rpcServer.start();

      OkHttpClient okHttpClient =
          new OkHttpClient.Builder()
              .readTimeout(Duration.ofMinutes(5))
              .callTimeout(Duration.ofMinutes(5))
              .build();

      ObjectMapper mapper = new ObjectMapper();
      JsonRpcRequest jsonRpcRequest =
          new JsonRpcRequest(
              "2.0", RpcMethod.OP_OUTPUT_AT_BLOCK.getRpcMethodName(), new Object[] {"7900000"});
      jsonRpcRequest.setId(new JsonRpcRequestId("1"));
      var postBody = mapper.writeValueAsBytes(jsonRpcRequest);
      RequestBody requestBody = RequestBody.create(postBody, MediaType.get("application/json"));

      final Request request =
          new Request.Builder().url("http://127.0.0.1:9545").post(requestBody).build();

      try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
        Future<Response> fork =
            scope.fork(
                TracerTaskWrapper.wrap(
                    () -> {
                      logger.info("test: {}", Thread.currentThread().getName());
                      return okHttpClient.newCall(request).execute();
                    }));
        scope.join();
        Response response = fork.get();
        assertEquals(200, response.code());
        assertNotNull(response.body());
        Map jsonRpcResp = mapper.readValue(response.body().string(), Map.class);
        assertEquals(jsonRpcResp.get("id"), "1");
        OutputRootResult outputRootResult =
            mapper.readValue(
                mapper.writeValueAsString(jsonRpcResp.get("result")), OutputRootResult.class);
        assertNotNull(outputRootResult);
      }
    } finally {
      rpcServer.stop();
    }
  }
}
