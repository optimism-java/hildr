package io.optimism.batcher.loader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import io.optimism.batcher.TestConstants;
import io.optimism.batcher.telemetry.BatcherMetrics;
import io.optimism.type.BlockId;
import io.optimism.type.L1BlockRef;
import io.optimism.type.L2BlockRef;
import io.optimism.type.OpEthSyncStatusRes;
import io.optimism.type.RollupConfigResult;
import io.optimism.utilities.rpc.Web3jProvider;
import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.tuples.generated.Tuple2;

/**
 * test case of InnerWatcher.
 *
 * @author thinkAfCod
 * @since 2023.05
 */
class BlockLoaderTest {

    static LoaderConfig config;
    static BigInteger blockNumber;

    @BeforeAll
    static void setUp() throws IOException {
        BlockLoaderTest.config =
                new LoaderConfig(TestConstants.l2RpcUrl, TestConstants.rollupRpcUrl, mock(BatcherMetrics.class));
    }

    @AfterAll
    static void tearDown() throws IOException {}

    @Test
    void calculateL2BlockRangeToStore() {
        var config = new LoaderConfig("http://fakeurl", "http://fakeurl", mock(BatcherMetrics.class));
        BlockLoader loader = spy(new BlockLoader(config, (unused) -> {}));
        doReturn(new OpEthSyncStatusRes.OpEthSyncStatus(
                        null,
                        null,
                        new L1BlockRef("0xhead", BigInteger.valueOf(20L), null, null), // headl1
                        null,
                        null,
                        new L2BlockRef("0xunsafel2", BigInteger.valueOf(21L), null, null, null, null), // unsafeL2
                        new L2BlockRef("0xsafel2", BigInteger.valueOf(20L), null, null, null, null), // safeL2
                        null,
                        null))
                .doReturn(new OpEthSyncStatusRes.OpEthSyncStatus(
                        null,
                        null,
                        new L1BlockRef("0xhead", BigInteger.valueOf(20L), null, null), // headl1
                        null,
                        null,
                        new L2BlockRef("0xunsafel2", BigInteger.valueOf(20L), null, null, null, null), // unsafeL2
                        new L2BlockRef("0xsafel2", BigInteger.valueOf(21L), null, null, null, null), // safeL2
                        null,
                        null))
                .when(loader)
                .requestSyncStatus();
        Tuple2<BlockId, BlockId> res = loader.calculateL2BlockRangeToStore();

        assertNotNull(res.component1(), "start block id should not be null");
        assertNotNull(res.component2(), "end block id should not be null");
        assertTrue(res.component1().number().compareTo(res.component2().number()) <= 0);

        assertThrows(SyncStatusException.class, loader::calculateL2BlockRangeToStore);
    }

    @Test
    public void testLoadBlock() throws IOException {
        if (StringUtils.isEmpty(TestConstants.l2RpcUrl) || StringUtils.isEmpty(TestConstants.rollupRpcUrl)) {
            return;
        }

        Web3j l2RpcClient = Web3jProvider.createClient(TestConstants.l2RpcUrl);
        BlockLoaderTest.blockNumber = l2RpcClient.ethBlockNumber().send().getBlockNumber();
        l2RpcClient.shutdown();

        var consumeCount = new AtomicInteger();
        var blockConsumer = (Consumer<EthBlock.Block>) block -> {
            consumeCount.addAndGet(1);
        };

        BlockLoader loader = spy(new BlockLoader(config, blockConsumer));
        var mockedBlock = new EthBlock.Block();
        mockedBlock.setHash("testHash");
        mockedBlock.setNumber("0x123");
        doReturn(mockedBlock).when(loader).getBlock(any());
        doReturn(new Tuple2<>(
                        new BlockId("", BlockLoaderTest.blockNumber.subtract(BigInteger.valueOf(100L))),
                        new BlockId("", BlockLoaderTest.blockNumber.subtract(BigInteger.valueOf(80L)))))
                .when(loader)
                .calculateL2BlockRangeToStore();
        doReturn(null).when(loader).l2BlockToBlockRef(any(), any());
        doReturn(new RollupConfigResult()).when(loader).getRollConfig();
        loader.loadBlocksIntoState();
        assertEquals(20, consumeCount.get());
        assertEquals(loader.latestLoadedBlock.number(), mockedBlock.getNumber());
    }
}
