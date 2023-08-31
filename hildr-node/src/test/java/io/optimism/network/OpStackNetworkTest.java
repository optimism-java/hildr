package io.optimism.network;

import static java.lang.Thread.sleep;

import io.optimism.config.Config;
import io.optimism.engine.ExecutionPayload;
import org.jctools.queues.MpscUnboundedXaddArrayQueue;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Created by IntelliJ IDEA. Author: kaichen Date: 2023/8/30 Time: 16:38
 */
class OpStackNetworkTest {

    @Test
    @Disabled
    void start() throws InterruptedException {
        MpscUnboundedXaddArrayQueue<ExecutionPayload> unsafeBlockQueue =
                new MpscUnboundedXaddArrayQueue<>(1024 * 64);
        OpStackNetwork opStackNetwork = new OpStackNetwork(Config.ChainConfig.optimismGoerli(), unsafeBlockQueue);
        opStackNetwork.start();

        sleep(120000);
        opStackNetwork.stop();
    }
}
