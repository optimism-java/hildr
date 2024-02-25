package io.optimism.utilities.rpc;

import okhttp3.OkHttpClient;

/**
 * Http client provider.
 *
 * @author thinkAfCod
 * @since 0.2.6
 */
public class HttpClientProvider {

    private HttpClientProvider() {}

    /**
     * Create a http client.
     *
     * @return a HttpClient
     */
    public static OkHttpClient create() {
        return new OkHttpClient.Builder()
                .addInterceptor(new RetryRateLimitInterceptor())
                .build();
    }
}
