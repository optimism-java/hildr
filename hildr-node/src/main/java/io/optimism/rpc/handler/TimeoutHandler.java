package io.optimism.rpc.handler;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Timeout handler.
 *
 * @author thinkAfCod
 * @since 2023.06
 */
public class TimeoutHandler {

    private static final int DEFAULT_ERROR_CODE = 504;

    private static final long DEFAULT_TIMEOUT_SECONDS = Duration.ofMinutes(5).toSeconds();

    private TimeoutHandler() {}

    /**
     * create TimeoutHandler.
     *
     * @param timeoutSeconds time duration.
     * @param errorCode timeout error code
     * @return vertx handler.
     */
    public static Handler<RoutingContext> handler(int timeoutSeconds, int errorCode) {
        final long timeoutDuration = timeoutSeconds <= 0
                ? DEFAULT_TIMEOUT_SECONDS
                : Duration.ofSeconds(timeoutSeconds).toSeconds();
        final var finalErrorCode = errorCode == 0 ? DEFAULT_ERROR_CODE : errorCode;
        return ctx -> processHandler(ctx, timeoutDuration, finalErrorCode);
    }

    /**
     * create TimeoutHandler use default timeout value and default error code.
     *
     * @return vertx handler.
     */
    public static Handler<RoutingContext> handler() {
        return TimeoutHandler.handler(0, 0);
    }

    private static void processHandler(final RoutingContext ctx, final long timeoutDuration, final int errorCode) {
        try {
            long tid = ctx.vertx().setTimer(TimeUnit.SECONDS.toMillis(timeoutDuration), t -> {
                ctx.fail(errorCode);
                ctx.response().close();
            });
            ctx.addBodyEndHandler(v -> ctx.vertx().cancelTimer(tid));
        } finally {
            ctx.next();
        }
    }
}
