package io.optimism.rpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.optimism.TestConstants;
import io.optimism.config.Config;
import io.optimism.rpc.internal.JsonRpcRequest;
import io.optimism.rpc.internal.JsonRpcRequestId;
import io.optimism.rpc.internal.result.OutputRootResult;
import io.optimism.telemetry.TracerTaskWrapper;
import io.optimism.types.enums.Logging;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.StructuredTaskScope;
import java.util.function.Function;
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

    private static ObjectMapper mapper;

    @BeforeAll
    static void setUp() {
        TracerTaskWrapper.setTracerSupplier(Logging.INSTANCE::getTracer);
        config = TestConstants.createConfig();
        mapper = new ObjectMapper();
    }

    RpcServer createRpcServer(Config config) {
        return new RpcServer(config);
    }

    @Test
    @SuppressWarnings("rawtypes")
    void testRpcServerStart() throws Exception {
        if (!TestConstants.isConfiguredApiKeyEnv) {
            return;
        }

        RpcServer rpcServer = createRpcServer(config);
        try {
            rpcServer.start();

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .readTimeout(Duration.ofMinutes(5))
                    .callTimeout(Duration.ofMinutes(5))
                    .build();

            ObjectMapper mapper = new ObjectMapper();
            JsonRpcRequest jsonRpcRequest = new JsonRpcRequest(
                    "2.0", RpcMethod.OP_OUTPUT_AT_BLOCK.getRpcMethodName(), new Object[] {"0x788B60"});
            jsonRpcRequest.setId(new JsonRpcRequestId("1"));
            var postBody = mapper.writeValueAsBytes(jsonRpcRequest);
            RequestBody requestBody = RequestBody.create(postBody, MediaType.get("application/json"));

            final Request request = new Request.Builder()
                    .url("http://127.0.0.1:9545")
                    .post(requestBody)
                    .build();

            try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
                StructuredTaskScope.Subtask<Response> fork = scope.fork(TracerTaskWrapper.wrap(() -> {
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
                        mapper.readValue(mapper.writeValueAsString(jsonRpcResp.get("result")), OutputRootResult.class);
                assertNotNull(outputRootResult);
            }
        } finally {
            rpcServer.stop();
        }
    }

    @Test
    @SuppressWarnings("rawtypes")
    void testRpcServerRegister() throws IOException, InterruptedException {
        RpcServer rpcServer = createRpcServer(new Config(
                null,
                null,
                "http://fakeurl",
                "http://fakeurl",
                "http://fakeurl",
                null,
                null,
                null,
                "0.0.0.0",
                9545,
                null,
                null,
                false,
                false,
                Config.SyncMode.Full,
                Config.ChainConfig.optimism()));
        rpcServer.start();
        HashMap<String, Function> rpcHandler = HashMap.newHashMap(1);
        rpcHandler.put("test_url", unused -> "response data");
        rpcServer.register(rpcHandler);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .readTimeout(Duration.ofMinutes(5))
                .callTimeout(Duration.ofMinutes(5))
                .build();
        JsonRpcRequest jsonRpcRequest = new JsonRpcRequest("2.0", "test_url", new Object[] {"7900000"});
        jsonRpcRequest.setId(new JsonRpcRequestId("1"));
        try {
            Map jsonRpcResp;
            try (Response response = sendRequest(okHttpClient, jsonRpcRequest)) {
                assertEquals(200, response.code());
                assertNotNull(response.body());
                jsonRpcResp = mapper.readValue(response.body().string(), Map.class);
            }
            System.out.println(jsonRpcResp);
        } finally {
            rpcServer.stop();
        }
    }

    private Response sendRequest(OkHttpClient okHttpClient, JsonRpcRequest jsonRpcRequest)
            throws JsonProcessingException, InterruptedException {
        var postBody = mapper.writeValueAsBytes(jsonRpcRequest);
        RequestBody requestBody = RequestBody.create(postBody, MediaType.get("application/json"));

        final Request request = new Request.Builder()
                .url("http://127.0.0.1:9545")
                .post(requestBody)
                .build();
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            StructuredTaskScope.Subtask<Response> fork = scope.fork(TracerTaskWrapper.wrap(() -> {
                logger.info("test: {}", Thread.currentThread().getName());
                return okHttpClient.newCall(request).execute();
            }));
            scope.join();
            return fork.get();
        }
    }
}
