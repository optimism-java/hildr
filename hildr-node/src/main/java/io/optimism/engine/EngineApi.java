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

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;
import io.jsonwebtoken.security.Keys;
import io.optimism.common.RequestWrapper;
import java.math.BigInteger;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.encoders.Hex;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.http.HttpService;

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

  /**
   * Creates an engine api from environment variables.
   *
   * @return EngineApi.
   */
  public EngineApi fromEnv() {
    String baseUrlParm = System.getProperty("ENGINE_API_URL");
    if (StringUtils.isBlank(baseUrlParm)) {
      throw new RuntimeException(
          """
              ENGINE_API_URL environment variable not set.
              Please set this to the base url of the engine api
              """);
    }
    String secretKey = System.getProperty("JWT_SECRET");
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
    Key key = Keys.hmacShaKeyFor(fromHex(secretStr));
    String jws =
        Jwts.builder()
            .setClaims(generateClaims())
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    HttpService httpService = new HttpService(baseUrl);
    httpService.addHeader("authorization", String.format("Bearer %1$s", jws));
    this.web3jService = httpService;
  }

  /**
   * The provided `secret` must be a valid hexadecimal string of length 64.
   *
   * @param secret secret
   * @return byte[]
   */
  public static byte[] fromHex(String secret) {
    String hex = secret.trim();
    if (hex.length() != 64) {
      throw new RuntimeException("Invalid JWT secret key length");
    }
    return Hex.decode(secret);
  }

  /**
   * Constructs the base engine api url for the given address.
   *
   * @param addr addr
   * @param portParm port
   * @return url
   */
  public static String authUrlFromAddr(String addr, Integer portParm) {
    String stripped = addr.replace("http://", "").replace("https://", "");
    Integer port = portParm == null ? DEFAULT_AUTH_PORT : portParm;
    return String.format("http://%1$s:%2$s", stripped, port);
  }

  /**
   * Generate claims.
   *
   * @return Claims
   */
  public static Claims generateClaims() {
    long nowSecs = LocalDateTime.now(ZoneId.systemDefault()).toEpochSecond(ZoneOffset.of("+8"));
    Map<String, Long> map = new HashMap<>();
    map.put("iat", nowSecs);
    map.put("exp", nowSecs + 60);
    return new DefaultClaims(map);
  }

  @Override
  public CompletableFuture<ForkChoiceUpdate> forkChoiceUpdate(
      ForkchoiceState forkchoiceState, PayloadAttributes payloadAttributes) {
    Request<?, ForkChoiceUpdate> r =
        new Request<>(
            ENGINE_FORKCHOICE_UPDATED_V1,
            Arrays.asList(forkchoiceState, payloadAttributes),
            web3jService,
            ForkChoiceUpdate.class);
    RequestWrapper<?, ForkChoiceUpdate> requestWrapper = new RequestWrapper<>(r);
    return requestWrapper.sendVtAsync();
  }

  @Override
  public CompletableFuture<PayloadStatus> newPayload(ExecutionPayload executionPayload) {
    Request<?, PayloadStatus> r =
        new Request<>(
            ENGINE_NEW_PAYLOAD_V1,
            Collections.singletonList(executionPayload),
            web3jService,
            PayloadStatus.class);
    RequestWrapper<?, PayloadStatus> requestWrapper = new RequestWrapper<>(r);
    return requestWrapper.sendVtAsync();
  }

  @Override
  public CompletableFuture<ExecutionPayload> getPayload(BigInteger payloadId) {
    Request<?, ExecutionPayload> r =
        new Request<>(
            ENGINE_GET_PAYLOAD_V1,
            Collections.singletonList(payloadId),
            web3jService,
            ExecutionPayload.class);
    RequestWrapper<?, ExecutionPayload> requestWrapper = new RequestWrapper<>(r);
    return requestWrapper.sendVtAsync();
  }
}
