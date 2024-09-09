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
