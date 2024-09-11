package io.optimism.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.optimism.TestConstants;
import io.optimism.types.Epoch;
import io.optimism.types.ExecutionPayload.ExecutionPayloadRes;
import io.optimism.types.ExecutionPayload.PayloadAttributes;
import io.optimism.types.ExecutionPayload.PayloadStatus;
import io.optimism.types.ExecutionPayload.Status;
import io.optimism.types.ForkChoiceUpdate.ForkChoiceUpdateRes;
import io.optimism.types.ForkChoiceUpdate.ForkchoiceState;
import java.io.IOException;
import java.math.BigInteger;
import java.security.Key;
import java.util.List;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.web3j.utils.Numeric;

/**
 * The type EngineApiTest.
 *
 * @author zhouxing
 * @since 0.1.0
 */
public class EngineApiTest {

    public static final String AUTH_ADDR = "127.0.0.1";
    public static final String SECRET = "f79ae8046bc11c9927afe911db7143c51a806c4a537cc08e0d37140b0192f430";

    public static MockWebServer server;

    @BeforeAll
    static void setUp() throws IOException {
        TestConstants.createConfig();
        server = new MockWebServer();
        server.start(8851);
    }

    @AfterAll
    static void tearDown() throws IOException {
        server.shutdown();
    }

    String initForkChoiceUpdateResp() throws JsonProcessingException {
        PayloadStatus payloadStatus = new PayloadStatus();
        payloadStatus.setStatus(Status.ACCEPTED);
        payloadStatus.setLatestValidHash("asdfadfsdfadsfasdf");
        payloadStatus.setValidationError("");
        ForkChoiceUpdateRes forkChoiceUpdateRes = new ForkChoiceUpdateRes(payloadStatus, "1");
        OpEthForkChoiceUpdate opEthForkChoiceUpdate = new OpEthForkChoiceUpdate();
        opEthForkChoiceUpdate.setResult(forkChoiceUpdateRes);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        return ow.writeValueAsString(opEthForkChoiceUpdate);
    }

    String initPayloadStatusResp() throws JsonProcessingException {
        PayloadStatus payloadStatus = new PayloadStatus();
        payloadStatus.setStatus(Status.ACCEPTED);
        payloadStatus.setLatestValidHash("12312321");
        OpEthPayloadStatus opEthPayloadStatus = new OpEthPayloadStatus();
        opEthPayloadStatus.setResult(payloadStatus);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        return ow.writeValueAsString(opEthPayloadStatus);
    }

    ExecutionPayloadRes initExecutionPayload() {
        return new ExecutionPayloadRes(
                "sdvkem39441fd132131",
                "123123",
                "123123",
                "123123",
                "123123",
                "123123",
                "1234",
                "123123",
                "123123",
                "123123",
                "123123",
                "123123",
                "sdfasdf12312312",
                null,
                List.of(),
                "123321",
                "321123");
    }

    String initExecutionPayloadJson() throws JsonProcessingException {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        OpEthExecutionPayload opEthExecutionPayload = new OpEthExecutionPayload();
        opEthExecutionPayload.setResult(new OpEthExecutionPayload.ExecutionPayloadObj(initExecutionPayload()));
        return ow.writeValueAsString(opEthExecutionPayload);
    }

    @Test
    void testForkChoiceUpdate() throws IOException {
        if (!TestConstants.isConfiguredApiKeyEnv) {
            return;
        }
        String baseUrl = EngineApi.authUrlFromAddr(AUTH_ADDR, null);
        assertEquals("http://127.0.0.1:8851", baseUrl);
        server.enqueue(new MockResponse().setBody(initForkChoiceUpdateResp()));
        EngineApi engineApi = new EngineApi(TestConstants.createConfig(), baseUrl, SECRET);
        ForkchoiceState forkchoiceState = new ForkchoiceState("123", "123", "!@3");
        PayloadAttributes payloadAttributes = new PayloadAttributes(
                new BigInteger("123123"),
                "123123",
                "123",
                List.of(""),
                null,
                true,
                new BigInteger("1"),
                new Epoch(new BigInteger("12"), "123", new BigInteger("1233145"), BigInteger.ZERO),
                new BigInteger("1334"),
                new BigInteger("321"),
                null);

        OpEthForkChoiceUpdate forkChoiceUpdate = engineApi.forkchoiceUpdated(forkchoiceState, payloadAttributes);
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        assertEquals(initForkChoiceUpdateResp(), ow.writeValueAsString(forkChoiceUpdate));
    }

    @Test
    void testNewPayload() throws IOException {
        if (!TestConstants.isConfiguredApiKeyEnv) {
            return;
        }
        String baseUrl = EngineApi.authUrlFromAddr(AUTH_ADDR, null);
        assertEquals("http://127.0.0.1:8851", baseUrl);
        server.enqueue(new MockResponse().setBody(initPayloadStatusResp()));
        EngineApi engineApi = new EngineApi(TestConstants.createConfig(), baseUrl, SECRET);
        OpEthPayloadStatus payloadStatus =
                engineApi.newPayload(initExecutionPayload().toExecutionPayload(null));
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        assertEquals(initPayloadStatusResp(), ow.writeValueAsString(payloadStatus));
    }

    @Test
    void testGetPayload() throws IOException {
        if (!TestConstants.isConfiguredApiKeyEnv) {
            return;
        }
        String baseUrl = EngineApi.authUrlFromAddr(AUTH_ADDR, null);
        assertEquals("http://127.0.0.1:8851", baseUrl);
        server.enqueue(new MockResponse().setBody(initExecutionPayloadJson()));
        EngineApi engineApi = new EngineApi(TestConstants.createConfig(), baseUrl, SECRET);
        OpEthExecutionPayload executionPayload = engineApi.getPayload(null, new BigInteger("123"));
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        assertEquals(initExecutionPayloadJson(), ow.writeValueAsString(executionPayload));
    }

    @Test
    @DisplayName("test jwt token")
    void testJwts() {
        Key key = Keys.hmacShaKeyFor(
                Numeric.hexStringToByteArray("f79ae5046bc11c9927afe911db7143c51a806c4a537cc08e0d37140b0192f430"));
        String jws = EngineApi.generateJws(key);

        Jws<Claims> jwt = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(jws);
        System.out.println(jwt);
        assertEquals(jwt.getHeader().getAlgorithm(), "HS256");

        assertEquals(
                jwt.getBody().getExpiration().toInstant().getEpochSecond()
                        - jwt.getBody().getIssuedAt().toInstant().getEpochSecond(),
                60L);
    }
}
