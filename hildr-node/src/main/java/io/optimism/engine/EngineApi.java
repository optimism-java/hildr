/*
 * Copyright 2023 281165273grape@gmail.com
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

package io.optimism.engine;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.optimism.engine.ExecutionPayload.PayloadAttributes;
import io.optimism.engine.ForkChoiceUpdate.ForkchoiceState;
import java.io.IOException;
import java.math.BigInteger;
import java.security.Key;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

/**
 * The type EngineApi.
 *
 * @author zhouxing
 * @since 0.1.0
 */
public class EngineApi implements Engine {

  /** The forkchoice updated method string. */
  public static final String ENGINE_FORKCHOICE_UPDATED_V1 = "engine_forkchoiceUpdatedV1";

  /** The new payload method string. */
  public static final String ENGINE_NEW_PAYLOAD_V1 = "engine_newPayloadV1";

  /** The get payload method string. */
  public static final String ENGINE_GET_PAYLOAD_V1 = "engine_getPayloadV1";

  /** The default engine api authentication port. */
  public static final Integer DEFAULT_AUTH_PORT = 8851;

  /** HttpService web3jService. */
  private final HttpService web3jService;

  private final Key key;

  /**
   * Creates an engine api from environment variables.
   *
   * @return EngineApi. engine api
   */
  public EngineApi fromEnv() {
    String baseUrlParm = System.getenv("ENGINE_API_URL");
    if (StringUtils.isBlank(baseUrlParm)) {
      throw new RuntimeException(
          """
              ENGINE_API_URL environment variable not set.
              Please set this to the base url of the engine api
              """);
    }
    String secretKey = System.getenv("JWT_SECRET");
    if (StringUtils.isBlank(secretKey)) {
      throw new RuntimeException(
          """
              JWT_SECRET environment variable not set.
              Please set this to the 256 bit hex-encoded secret key
               used to authenticate with the engine api.
              This should be the same as set in the `--auth.secret`
               flag when executing go-ethereum.
              """);
    }
    String baseUrlFormat = authUrlFromAddr(baseUrlParm, null);
    return new EngineApi(baseUrlFormat, secretKey);
  }

  /**
   * Creates a new [`EngineApi`] with a base url and secret.
   *
   * @param baseUrl baseUrl
   * @param secretStr secret
   */
  public EngineApi(final String baseUrl, final String secretStr) {
    this.key = Keys.hmacShaKeyFor(Numeric.hexStringToByteArray(secretStr));
    this.web3jService = new HttpService(baseUrl);
  }

  /**
   * Constructs the base engine api url for the given address.
   *
   * @param addr addr
   * @param portParm port
   * @return url string
   */
  public static String authUrlFromAddr(String addr, Integer portParm) {
    String stripped = addr.replace("http://", "").replace("https://", "");
    Integer port = portParm == null ? DEFAULT_AUTH_PORT : portParm;
    return String.format("http://%1$s:%2$s", stripped, port);
  }

  /**
   * Generate jws string.
   *
   * @param key the key
   * @return the string
   */
  protected static String generateJws(Key key) {
    Instant now = Instant.now();
    Date nowDate = Date.from(now);
    Date expirationDate = Date.from(now.plusSeconds(60));
    return Jwts.builder()
        .setIssuedAt(nowDate)
        .setExpiration(expirationDate)
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  @Override
  public OpEthForkChoiceUpdate forkChoiceUpdate(
      ForkchoiceState forkchoiceState, PayloadAttributes payloadAttributes) throws IOException {
    web3jService.addHeader("authorization", String.format("Bearer %1$s", generateJws(key)));
    Request<?, OpEthForkChoiceUpdate> r =
        new Request<>(
            ENGINE_FORKCHOICE_UPDATED_V1,
            Arrays.asList(forkchoiceState, payloadAttributes),
            web3jService,
            OpEthForkChoiceUpdate.class);
    return r.send();
  }

  @Override
  public OpEthPayloadStatus newPayload(ExecutionPayload executionPayload) throws IOException {
    web3jService.addHeader("authorization", String.format("Bearer %1$s", generateJws(key)));
    Request<?, OpEthPayloadStatus> r =
        new Request<>(
            ENGINE_NEW_PAYLOAD_V1,
            Collections.singletonList(executionPayload),
            web3jService,
            OpEthPayloadStatus.class);
    return r.send();
  }

  @Override
  public OpEthExecutionPayload getPayload(BigInteger payloadId) throws IOException {
    web3jService.addHeader("authorization", String.format("Bearer %1$s", generateJws(key)));
    Request<?, OpEthExecutionPayload> r =
        new Request<>(
            ENGINE_GET_PAYLOAD_V1,
            Collections.singletonList(payloadId),
            web3jService,
            OpEthExecutionPayload.class);
    return r.send();
  }
}
