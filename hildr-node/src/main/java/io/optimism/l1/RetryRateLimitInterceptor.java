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

package io.optimism.l1;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.google.common.util.concurrent.RateLimiter;
import java.io.IOException;
import java.time.Duration;
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

  private final RateLimiter rateLimiter;

  private final Retryer<Response> retryer;

  /** the RetryRateLimitInterceptor constructor. */
  public RetryRateLimitInterceptor() {
    this.rateLimiter = RateLimiter.create(2000, Duration.ofMillis(50L));
    this.retryer =
        RetryerBuilder.<Response>newBuilder()
            .withWaitStrategy(WaitStrategies.randomWait(3, TimeUnit.SECONDS))
            .withStopStrategy(StopStrategies.stopAfterAttempt(10))
            .retryIfResult(
                res ->
                    res != null
                        && (res.code() == 429
                            || res.message().contains(HEADER_NOT_FOUND)
                            || res.message().contains(RATE_LIMIT_MSG)))
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
}
