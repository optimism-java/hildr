package io.optimism.driver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.web3j.protocol.core.DefaultBlockParameterName.FINALIZED;

import io.optimism.config.Config;
import io.optimism.config.Config.ChainConfig;
import io.optimism.config.Config.CliConfig;
import io.optimism.engine.EngineApi;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.http.HttpService;

class DriverTest {

    @Test
    void testNewDriverFromFinalizedHead() throws IOException, ExecutionException, InterruptedException {
        if (System.getenv("L2_TEST_RPC_URL") == null || System.getenv("L1_TEST_RPC_URL") == null) {
            return;
        }
        String l1rpc = System.getenv("L1_TEST_RPC_URL");
        String l1WsRpc = System.getenv("L1_TEST_WS_RPC_URL");
        String l1BeaconRpc = System.getenv("L1_TEST_BEACON_RPC_URL");
        String l2rpc = System.getenv("L2_TEST_RPC_URL");
        CliConfig cliConfig = new CliConfig(
                l1rpc,
                l1WsRpc,
                l1BeaconRpc,
                l1BeaconRpc,
                l2rpc,
                null,
                "d195a64e08587a3f1560686448867220c2727550ce3e0c95c7200d0ade0f9167",
                l2rpc,
                null,
                null,
                null,
                null,
                Config.SyncMode.Full,
                false,
                false);

        Config config = Config.create(null, cliConfig, ChainConfig.optimismSepolia());
        Web3j provider = Web3j.build(new HttpService(config.l2RpcUrl()));
        EthBlock finalizedBlock = provider.ethGetBlockByNumber(FINALIZED, true).send();
        Driver<EngineApi> driver = Driver.from(config, new CountDownLatch(1));

        assertEquals(
                driver.getEngineDriver().getFinalizedHead().number(),
                finalizedBlock.getBlock().getNumber());
    }
}
