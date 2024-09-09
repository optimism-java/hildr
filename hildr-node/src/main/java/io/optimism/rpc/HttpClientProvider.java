/*
 * Copyright 2023 q315xia@163.com
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

package io.optimism.rpc;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Http client provider.
 *
 * @author thinkAfCod
 * @since 0.2.6
 */
public class HttpClientProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientProvider.class);

    private HttpClientProvider() {}

    /**
     * Create a http client.
     *
     * @return a HttpClient
     */
    public static OkHttpClient create() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (LOGGER.isTraceEnabled()) {
            builder.addInterceptor(
                    new HttpLoggingInterceptor(LOGGER::debug).setLevel(HttpLoggingInterceptor.Level.BODY));
        }
        return builder.addInterceptor(new RetryRateLimitInterceptor()).build();
    }
}
