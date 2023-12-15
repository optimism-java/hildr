package io.optimism.network;

import static java.lang.Thread.sleep;

import io.optimism.config.Config;
import io.optimism.engine.ExecutionPayload;
import org.jctools.queues.MpscUnboundedXaddArrayQueue;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * The type OpStackNetworkTest.
 *
 * @author grapebaba
 * @since 0.2.0
 */
class OpStackNetworkTest {

    @Test
    @Disabled
    void start() throws InterruptedException {
        MpscUnboundedXaddArrayQueue<ExecutionPayload> unsafeBlockQueue = new MpscUnboundedXaddArrayQueue<>(1024 * 64);
        OpStackNetwork opStackNetwork = new OpStackNetwork(Config.ChainConfig.optimismGoerli(), unsafeBlockQueue);
        opStackNetwork.start();

        sleep(120000);
        opStackNetwork.stop();
    }
}
