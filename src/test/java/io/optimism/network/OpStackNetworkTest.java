package io.optimism.network;

import static java.lang.Thread.sleep;

import io.optimism.config.Config;
import io.optimism.types.ExecutionPayload;
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
        // String l1RpcUrl,
        //        String l1WsRpcUrl,
        //        String l1BeaconUrl,
        //        String l1BeaconArchiverUrl,
        //        String l2RpcUrl,
        //        String l2EngineUrl,
        //        String jwtSecret,
        //        String checkpointSyncUrl,
        //        Integer rpcPort,
        //        List<String> bootNodes,
        //        Integer discPort,
        //        Boolean devnet,
        //        Boolean sequencerEnable,
        //        SyncMode syncMode,
        //        ChainConfig chainConfig
        Config config = new Config(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                9876,
                null,
                null,
                null,
                Config.ChainConfig.optimismSepolia());
        OpStackNetwork opStackNetwork = new OpStackNetwork(config, unsafeBlockQueue);
        opStackNetwork.start();

        sleep(120000);
        opStackNetwork.stop();
    }
}
