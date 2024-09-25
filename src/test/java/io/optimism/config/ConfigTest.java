package io.optimism.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.optimism.config.Config.ChainConfig;
import io.optimism.config.Config.CliConfig;
import io.optimism.config.Config.SystemConfig;
import java.math.BigInteger;
import java.nio.file.Paths;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.web3j.utils.Numeric;

/**
 * The type ConfigTest.
 *
 * @author grapebaba
 * @since 0.1.0
 */
class ConfigTest {

    /**
     * Create.
     */
    @Test
    void create() {
        CliConfig cliConfig = new CliConfig(
                null,
                null,
                null,
                null,
                null,
                null,
                "testjwt",
                null,
                null,
                null,
                null,
                null,
                Config.SyncMode.Full,
                false,
                false);
        Config config = Config.create(
                Paths.get("src", "test", "resources", "test.toml"), cliConfig, ChainConfig.optimismSepolia());
        assertEquals("https://example2.com", config.l2RpcUrl());
        assertEquals("http://127.0.0.1:8551", config.l2EngineUrl());
        assertEquals("", config.l1RpcUrl());
        assertEquals("testjwt", config.jwtSecret());
        assertEquals("0.0.0.0", config.rpcAddr());
        assertEquals(9545, config.rpcPort());
        assertEquals(9876, config.discPort());
        assertNull(config.bootNodes());

        CliConfig bootCliConfig = new CliConfig(
                null,
                null,
                null,
                null,
                null,
                null,
                "testjwt",
                null,
                null,
                null,
                List.of("encode://123", "encode://321"),
                92,
                Config.SyncMode.Full,
                false,
                false);
        Config configBootNodes = Config.create(
                Paths.get("src", "test", "resources", "test.toml"), bootCliConfig, ChainConfig.optimismSepolia());
        assertEquals(92, configBootNodes.discPort());
        assertEquals(List.of("encode://123", "encode://321"), configBootNodes.bootNodes());

        assertEquals(
                "0x48f520cf4ddaf34c8336e6e490632ea3cf1e5e93b0b2bc6e917557e31845371b",
                config.chainConfig().l1StartEpoch().hash());
        assertEquals(
                BigInteger.valueOf(4071408L),
                config.chainConfig().l1StartEpoch().number());
        assertEquals(
                BigInteger.valueOf(1691802540L),
                config.chainConfig().l1StartEpoch().timestamp());
        assertEquals("optimism-sepolia", config.chainConfig().network());
        assertEquals(
                "0x102de6ffb001480cc9b8b548fd05c34cd4f46ae4aa91759393db90ea0409887d",
                config.chainConfig().l2Genesis().hash());
        assertEquals(BigInteger.valueOf(0L), config.chainConfig().l2Genesis().number());
        assertEquals(
                "0x0000000000000000000000000000000000000000000000000000000000000000",
                config.chainConfig().l2Genesis().parentHash());
        assertEquals(
                BigInteger.valueOf(1691802540L),
                config.chainConfig().l2Genesis().timestamp());
        assertEquals(
                "0x8F23BB38F531600e5d8FDDaAEC41F13FaB46E98c",
                config.chainConfig().systemConfig().batchSender());
        assertEquals(
                BigInteger.valueOf(30_000_000L),
                config.chainConfig().systemConfig().gasLimit());
        assertEquals(
                BigInteger.valueOf(188), config.chainConfig().systemConfig().l1FeeOverhead());
        assertEquals(
                new BigInteger("452312848583266388373324160190187140051835877600158453279134670530344387928"),
                config.chainConfig().systemConfig().l1FeeScalar());
        assertEquals(
                "0xff00000000000000000000000000000011155420",
                config.chainConfig().batchInbox());
        assertEquals(
                "0x16fc5058f25648194471939df75cf27a2fdc48bc",
                config.chainConfig().depositContract());
        assertEquals(
                "0x034edd2a225f7f429a63e0f1d2084b9e0a93b538",
                config.chainConfig().systemConfigContract());
        assertEquals(
                "0x4200000000000000000000000000000000000016",
                config.chainConfig().l2Tol1MessagePasser());
        assertEquals(BigInteger.valueOf(300L), config.chainConfig().channelTimeout());
        assertEquals(BigInteger.valueOf(111111111L), config.chainConfig().seqWindowSize());
        assertEquals(BigInteger.valueOf(600L), config.chainConfig().maxSeqDrift());
        assertEquals(BigInteger.valueOf(0L), config.chainConfig().regolithTime());
        assertEquals(BigInteger.valueOf(1699981200L), config.chainConfig().canyonTime());
        assertEquals(BigInteger.valueOf(2L), config.chainConfig().blockTime());
    }

    /**
     * Base sepolia.
     */
    @Test
    void baseSepolia() {
        ChainConfig chainConfig = ChainConfig.baseSepolia();
        assertEquals(chainConfig.canyonTime(), BigInteger.valueOf(1699981200L));
    }

    @Test
    void optimismSepolia() {
        ChainConfig chainConfig = ChainConfig.optimismSepolia();
        assertEquals(chainConfig.canyonTime(), BigInteger.valueOf(1699981200L));
    }

    /**
     * Batch hash.
     */
    @Test
    void batchHash() {
        SystemConfig systemConfig = new SystemConfig(
                "0x2d679b567db6187c0c8323fa982cfb88b74dbcc7",
                BigInteger.valueOf(25_000_000L),
                BigInteger.valueOf(2100),
                BigInteger.valueOf(1000000),
                "0x2d679b567db6187c0c8323fa982cfb88b74dbcc7");

        assertTrue(systemConfig.batcherHash().contains(Numeric.cleanHexPrefix(systemConfig.batchSender())));
    }

    @Test
    void testOptimismChannelTimeout() {
        ChainConfig chainConfig = ChainConfig.optimismSepolia();
        BigInteger bedrockChTimeout = chainConfig.channelTimeout();
        BigInteger fjordTimeChTimeout =
                chainConfig.channelTimeout(chainConfig.graniteTime().subtract(BigInteger.ONE));
        BigInteger graniteChTimeout =
                chainConfig.channelTimeout(chainConfig.graniteTime().add(BigInteger.ONE));
        assertEquals(bedrockChTimeout, BigInteger.valueOf(300L));
        assertEquals(fjordTimeChTimeout, BigInteger.valueOf(300L));
        assertEquals(graniteChTimeout, BigInteger.valueOf(50L));
    }

    /**
     * Read external chain from json.
     */
    @Test
    void readExternalChainFromJson() {
        var devnetJson = "{\n"
                + "\"genesis\": {\n"
                + "  \"l1\": {\n"
                + "    \"hash\": \"0xdb52a58e7341447d1a9525d248ea"
                + "07dbca7dfa0e105721dee1aa5a86163c088d\",\n"
                + "    \"number\": 0\n"
                + "  },\n"
                + "  \"l2\": {\n"
                + "    \"hash\": \"0xf85bca315a08237644b06a8350cda3"
                + "bc0de1593745a91be93daeadb28fb3a32e\",\n"
                + "    \"number\": 0\n"
                + "  },\n"
                + "  \"l2_time\": 1685710775,\n"
                + "  \"system_config\": {\n"
                + "    \"batcherAddr\": \"0x3c44cdddb6a900fa2b585dd299e03d12fa4293bc\",\n"
                + "    \"overhead\":\n"
                + "         \"0x0000000000000000000000000000000000000000000000000000000000000834\",\n"
                + "    \"scalar\": \"0x000000000000000000000000000000"
                + "00000000000000000000000000000f4240\",\n"
                + "    \"gasLimit\": 30000000\n"
                + "  }\n"
                + "},\n"
                + "\"block_time\": 2,\n"
                + "\"max_sequencer_drift\": 300,\n"
                + "\"seq_window_size\": 200,\n"
                + "\"channel_timeout\": 120,\n"
                + "\"l1_chain_id\": 900,\n"
                + "\"l2_chain_id\": 901,\n"
                + "\"regolith_time\": 0,\n"
                + "\"canyon_time\": -1,\n"
                + "\"batch_inbox_address\": \"0xff00000000000000000000000000000000000000\",\n"
                + "\"deposit_contract_address\": \"0x6900000000000000000000000000000000000001\",\n"
                + "\"l1_system_config_address\": \"0x6900000000000000000000000000000000000009\"\n"
                + "}";
        var external = assertDoesNotThrow(
                () -> {
                    var mapper = new ObjectMapper();
                    return mapper.readValue(devnetJson, Config.ExternalChainConfig.class);
                },
                "parse json content should not throws but it does");
        var chain = Config.ChainConfig.fromExternal(external);
        assertEquals(chain.network(), "external");
        assertEquals(chain.l1ChainId(), BigInteger.valueOf(900L));
        assertEquals(chain.l2ChainId(), BigInteger.valueOf(901L));
        assertEquals(chain.l1StartEpoch().number(), BigInteger.ZERO);
        assertEquals(chain.l1StartEpoch().hash(), "0xdb52a58e7341447d1a9525d248ea07dbca7dfa0e105721dee1aa5a86163c088d");
        assertEquals(chain.l2Genesis().hash(), "0xf85bca315a08237644b06a8350cda3bc0de1593745a91be93daeadb28fb3a32e");
        assertEquals(chain.systemConfig().gasLimit(), BigInteger.valueOf(30_000_000L));
        assertEquals(chain.systemConfig().l1FeeOverhead(), BigInteger.valueOf(2100L));
        assertEquals(chain.systemConfig().l1FeeScalar(), BigInteger.valueOf(1_000_000L));
        assertEquals(chain.systemConfig().batchSender(), "0x3c44cdddb6a900fa2b585dd299e03d12fa4293bc");
        assertEquals(chain.batchInbox(), "0xff00000000000000000000000000000000000000");
        assertEquals(chain.depositContract(), "0x6900000000000000000000000000000000000001");
        assertEquals(chain.systemConfigContract(), "0x6900000000000000000000000000000000000009");

        assertEquals(chain.channelTimeout(), BigInteger.valueOf(120L));
        assertEquals(chain.seqWindowSize(), BigInteger.valueOf(200L));
        assertEquals(chain.maxSeqDrift(), BigInteger.valueOf(300L));
        assertEquals(chain.regolithTime(), BigInteger.ZERO);
        assertEquals(chain.blockTime(), BigInteger.TWO);
        assertEquals(chain.canyonTime(), BigInteger.valueOf(-1L));
    }
}
