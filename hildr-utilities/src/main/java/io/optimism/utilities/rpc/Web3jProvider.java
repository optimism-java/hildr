package io.optimism.utilities.rpc;

import com.google.common.util.concurrent.AbstractExecutionThreadService;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.lang3.StringUtils;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.websocket.WebSocketService;
import org.web3j.tuples.generated.Tuple2;

/**
 * Web3j client provider.
 *
 * @author thinkAfCod
 * @since 2023.06
 */
public class Web3jProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientProvider.class);

    private Web3jProvider() {}

    private static final List<AbstractExecutionThreadService> services = new ArrayList<>();

    /**
     * create web3j client.
     *
     * @param url ethereum/optimism client node url
     * @return web3j client
     */
    public static Web3j createClient(String url) {
        return create(url).component1();
    }

    /**
     * Create web3j client, and return Web3jService.
     * There are more custom operations that can be performed using a Web3jService instance.
     *
     * @param url ethereum/optimism client node url
     * @return web3j client and web3j service
     */
    public static Tuple2<Web3j, Web3jService> create(String url) {
        return create(url, null);
    }

    public static Tuple2<Web3j, Web3jService> create(String url, Function<String, Boolean> logFilter) {
        Web3jService web3Srv;
        if (Web3jProvider.isHttp(url)) {
            var okHttpClientBuilder = new OkHttpClient.Builder();
            if (LOGGER.isDebugEnabled()) {
                okHttpClientBuilder.addInterceptor(
                        new HttpLoggingInterceptor(LOGGER::debug).setLevel(HttpLoggingInterceptor.Level.BODY));
            }
            if (logFilter != null) {
                var interceptor = new JsonRpcRequestBodyLoggingInterceptor(logFilter);
                services.add(interceptor);
                okHttpClientBuilder.addInterceptor(interceptor);
            }
            var okHttpClient = okHttpClientBuilder
                    .addInterceptor(new RetryRateLimitInterceptor())
                    .build();
            web3Srv = new HttpService(url, okHttpClient);

        } else if (Web3jProvider.isWs(url)) {
            final var web3finalSrv = new WebSocketService(url, true);
            wsConnect(web3finalSrv);
            web3Srv = web3finalSrv;
        } else {
            throw new IllegalArgumentException("not supported scheme:%s".formatted(url));
        }
        return new Tuple2<>(Web3j.build(web3Srv), web3Srv);
    }

    public static void stop() {
        services.forEach(AbstractExecutionThreadService::stopAsync);
    }

    private static void wsConnect(final WebSocketService wss) {
        final Consumer<Throwable> onError = t -> {
            if (t instanceof WebsocketNotConnectedException) {
                wsConnect(wss);
            }
        };
        try {
            wss.connect(s -> {}, onError, () -> {});
        } catch (ConnectException e) {
            throw new IllegalStateException(e);
        }
    }

    private static boolean isHttp(final String url) {
        return !StringUtils.isEmpty(url) && url.startsWith("http");
    }

    private static boolean isWs(final String url) {
        return !StringUtils.isEmpty(url) && url.startsWith("ws");
    }
}
