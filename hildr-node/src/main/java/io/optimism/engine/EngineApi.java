package io.optimism.engine;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
import io.jsonwebtoken.security.Keys;
import java.math.BigInteger;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import okhttp3.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.encoders.Hex;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.http.HttpService;

/**
 * The type EngineApi
 *
 * @author zhouxing
 * @since 0.1.0
 */
public class EngineApi implements Engine {
  public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
  public static final String ENGINE_FORKCHOICE_UPDATED_V1 = "engine_forkchoiceUpdatedV1";
  public static final String ENGINE_NEW_PAYLOAD_V1 = "engine_newPayloadV1";
  public static final String ENGINE_GET_PAYLOAD_V1 = "engine_getPayloadV1";
  public static final Integer DEFAULT_AUTH_PORT = 8851;
  private final String baseUrl;
  private final Integer port;
  private final String jwtSecret;
  private final HttpService web3jService;

  public EngineApi fromEnv() {
    String baseUrlParm = System.getProperty("ENGINE_API_URL");
    if (StringUtils.isBlank(baseUrlParm)) {
      throw new RuntimeException("""
          ENGINE_API_URL environment variable not set.
          Please set this to the base url of the engine api
          """);
    }
    String secretKey = System.getProperty("JWT_SECRET");
    if (StringUtils.isBlank(secretKey)) {
      throw new RuntimeException("""
          JWT_SECRET environment variable not set.
          Please set this to the 256 bit hex-encoded secret key used to authenticate with the engine api.
          This should be the same as set in the `--auth.secret` flag when executing go-ethereum.
          """);
    }
    String baseUrlFormat = authUrlFromAddr(baseUrlParm, null);
    return new EngineApi(baseUrlFormat, secretKey);
  }

  public EngineApi(final String baseUrl, final String secretStr) {
    this.jwtSecret = fromHex(secretStr);
    String[] parts = baseUrl.split(":");
    port = Integer.valueOf(parts[parts.length - 1]);
    if (parts.length <= 2) {
      this.baseUrl = parts[0];
    } else {
      this.baseUrl = String.join(":", parts);
    }
    Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    String jws = Jwts.builder()
        .setClaims(generateClaims())
        .signWith(key).compact();
    HttpService httpService = new HttpService(baseUrl);
    httpService.addHeader("authorization", String.format("Bearer %1$s", jws));
    this.web3jService = httpService;
  }

  private String fromHex(String secret) {
    String hex = secret.trim();
    if (hex.length() != 64) {
      throw new RuntimeException("Invalid JWT secret key length");
    }
    return new String(Hex.decode(hex));
  }

  public static String authUrlFromAddr(String addr, Integer portParm) {
    String stripped = addr.replace("http://", "").replace("https://", "");
    Integer port = portParm == null ? DEFAULT_AUTH_PORT : portParm;
    return String.format("http://%1$s%2$s", stripped, port);
  }

  public static Claims generateClaims() {
    long nowSecs = LocalDateTime.now().toEpochSecond(ZoneOffset.of("0"));
    Map<String, Long> map = new HashMap<>();
    map.put("iat", nowSecs);
    map.put("exp", nowSecs + 60);
    return new DefaultClaims(map);
  }

  @Override
  public Request<?, ForkChoiceUpdate> forkChoiceUpdate(ForkchoiceState forkchoiceState,
                                                       PayloadAttributes payloadAttributes) {
    return new Request<>(ENGINE_FORKCHOICE_UPDATED_V1,
        Arrays.asList(forkchoiceState, payloadAttributes), web3jService, ForkChoiceUpdate.class);
  }

  @Override
  public Request<?, PayloadStatus> newPayload(ExecutionPayload executionPayload) {
    return new Request<>(ENGINE_NEW_PAYLOAD_V1,
        Arrays.asList(executionPayload), web3jService, PayloadStatus.class);
  }

  @Override
  public Request<?, ExecutionPayload> getPayload(BigInteger payloadId) {
    return new Request<>(ENGINE_GET_PAYLOAD_V1,
        Arrays.asList(payloadId), web3jService, ExecutionPayload.class);
  }
}
