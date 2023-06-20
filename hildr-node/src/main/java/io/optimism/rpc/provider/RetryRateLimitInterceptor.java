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

package io.optimism.rpc.provider;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.util.concurrent.RateLimiter;
import io.optimism.l1.InnerWatcher;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import okhttp3.Interceptor;
import okhttp3.Protocol;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * custom retry and rate limit interceptor of OkHttp.
 *
 * @author thinkAfCod
 * @since 0.1.0
 */
@SuppressWarnings("checkstyle:AnnotationLocationMostCases")
public class RetryRateLimitInterceptor implements Interceptor {

  private static final Logger LOGGER = LoggerFactory.getLogger(InnerWatcher.class);

  private static final String HEADER_NOT_FOUND = "header not found";

  private static final String RATE_LIMIT_MSG = "rate limit";

  private static final String DAILY_REQUEST_COUNT_EXCEEDED_REQUEST_RATE_LIMITED =
      "daily request count exceeded, request rate limited";

  private static final String RPC_CODE = "code";
  private static final String RPC_MESSAGE = "message";

  private static final String RPC_ERROR_CODE = "429";
  private static final String EXCEEDS_BLOCK_GAS_LIMIT = "-32005";
  private static final String RATE_LIMIT_ERROR_CODE = "-32016";

  private final RateLimiter rateLimiter;

  private final Retryer<Response> retryer;

  private final ObjectMapper mapper = new ObjectMapper();

  /** the RetryRateLimitInterceptor constructor. */
  public RetryRateLimitInterceptor() {
    this.rateLimiter = RateLimiter.create(200, Duration.ofMillis(50L));
    this.retryer =
        RetryerBuilder.<Response>newBuilder()
            .withWaitStrategy(WaitStrategies.randomWait(100, TimeUnit.MILLISECONDS))
            .withStopStrategy(StopStrategies.stopAfterAttempt(100))
            .retryIfResult(this::shouldRetry)
            .retryIfException(e -> e instanceof IOException)
            .build();
  }

  @NotNull @Override
  public Response intercept(@NotNull final Chain chain) throws IOException {
    try {
      return this.retryer.call(
          () -> {
            if (!this.rateLimiter.tryAcquire()) {
              return new Response.Builder()
                  .request(chain.request())
                  .protocol(Protocol.HTTP_1_1)
                  .code(429)
                  .build();
            }
            return chain.proceed(chain.request());
          });
    } catch (ExecutionException | RetryException e) {
      LOGGER.error("request failed", e);
      return new Response.Builder().request(chain.request()).code(-1).build();
    }
  }

  private boolean shouldRetry(Response res) {
    var httpCode = res.code();
    if (httpCode == 429) {
      return true;
    }

    try {
      if (res.body() == null) {
        return false;
      }
      String jsonRpcRes = res.body().string();
      Map<String, String> rpcRes = mapper.readValue(jsonRpcRes, new TypeReference<>() {});
      String rpcCode = rpcRes.get(RPC_CODE);
      String rpcMsg = rpcRes.getOrDefault(RPC_MESSAGE, "");
      if (RPC_ERROR_CODE.equals(rpcCode)
          || EXCEEDS_BLOCK_GAS_LIMIT.equals(rpcCode)
          || (RATE_LIMIT_ERROR_CODE.equals(rpcCode) && RATE_LIMIT_MSG.contains(rpcMsg))) {
        return true;
      }
      if (HEADER_NOT_FOUND.equals(rpcMsg)
          || DAILY_REQUEST_COUNT_EXCEEDED_REQUEST_RATE_LIMITED.equals(rpcMsg)) {
        return true;
      }

    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return false;
  }
}
