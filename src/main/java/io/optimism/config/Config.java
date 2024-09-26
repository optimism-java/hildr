package io.optimism.config;

import static java.util.Map.entry;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.optimism.exceptions.ConfigLoadException;
import io.optimism.types.BlockInfo;
import io.optimism.types.Epoch;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.github.gestalt.config.Gestalt;
import org.github.gestalt.config.builder.GestaltBuilder;
import org.github.gestalt.config.exceptions.GestaltException;
import org.github.gestalt.config.loader.EnvironmentVarsLoader;
import org.github.gestalt.config.loader.MapConfigLoader;
import org.github.gestalt.config.loader.PropertyLoader;
import org.github.gestalt.config.source.FileConfigSourceBuilder;
import org.github.gestalt.config.source.MapConfigSourceBuilder;
import org.github.gestalt.config.toml.TomlLoader;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.utils.Numeric;

/**
 * The type Config.
 *
 * @param l1RpcUrl            L1 chain rpc url.
 * @param l1WsRpcUrl          L1 chain websocket rpc url.
 * @param l1BeaconUrl         L1 beacon chain rpc url.
 * @param l1BeaconArchiverUrl L1 beacon chain archiver rpc url.
 * @param l2RpcUrl            L2 chain rpc url.
 * @param l2EngineUrl         L2 engine API url.
 * @param jwtSecret           L2 engine API jwt secret.
 * @param checkpointSyncUrl   The checkpoint sync url.
 * @param rpcAddr             The rpc address.
 * @param rpcPort             The rpc port.
 * @param bootNodes           The custom boot nodes.
 * @param discPort            The discovery port.
 * @param devnet              The flag of devnet.
 * @param sequencerEnable     The flag of sequencerEnable.
 * @param syncMode            The sync mode
 * @param chainConfig         The chain config.
 * @author grapebaba
 * @since 0.1.0
 */
public record Config(
        String l1RpcUrl,
        String l1WsRpcUrl,
        String l1BeaconUrl,
        String l1BeaconArchiverUrl,
        String l2RpcUrl,
        String l2EngineUrl,
        String jwtSecret,
        String checkpointSyncUrl,
        String rpcAddr,
        Integer rpcPort,
        List<String> bootNodes,
        Integer discPort,
        Boolean devnet,
        Boolean sequencerEnable,
        SyncMode syncMode,
        ChainConfig chainConfig) {

    private static final int MAX_CHANNEL_SIZE_BEDROCK = 100_000_000;

    private static final int MAX_CHANNEL_SIZE_FJORD = 1_000_000_000;

    private static final int MAX_SEQUENCER_DRIFT_FJORD = 1800;

    private static final int CHANNEL_TIMEOUT_GRANITE = 50;

    /**
     * Create Config.
     *
     * @param configPath  the config path
     * @param cliConfig   the cli config
     * @param chainConfig the chain config
     * @return the config
     */
    public static Config create(Path configPath, CliConfig cliConfig, ChainConfig chainConfig) {

        try {
            EnvironmentVarsLoader environmentVarsLoader = new EnvironmentVarsLoader();
            MapConfigLoader mapConfigLoader = new MapConfigLoader();
            TomlLoader tomlLoader = new TomlLoader();
            PropertyLoader propertyLoader = new PropertyLoader();

            Map<String, String> defaultProvider = getDefaultConfigMap();

            Map<String, String> chainProvider = chainConfig.toConfigMap();

            Map<String, String> cliProvider = cliConfig.toConfigMap();

            Gestalt gestalt;
            final GestaltBuilder gestaltBuilder = new GestaltBuilder()
                    .addDefaultDecoders()
                    .addDecoder(new ChainBigIntegerDecoder())
                    .addConfigLoader(environmentVarsLoader)
                    .addConfigLoader(mapConfigLoader)
                    .addConfigLoader(tomlLoader)
                    .addConfigLoader(propertyLoader)
                    .setTreatMissingValuesAsErrors(false);
            if (configPath != null) {
                gestalt = gestaltBuilder
                        .addSource(MapConfigSourceBuilder.builder()
                                .setCustomConfig(defaultProvider)
                                .build())
                        .addSource(MapConfigSourceBuilder.builder()
                                .setCustomConfig(chainProvider)
                                .build())
                        .addSource(FileConfigSourceBuilder.builder()
                                .setPath(configPath)
                                .build())
                        .addSource(MapConfigSourceBuilder.builder()
                                .setCustomConfig(cliProvider)
                                .build())
                        .build();
            } else {
                gestalt = gestaltBuilder
                        .addSource(MapConfigSourceBuilder.builder()
                                .setCustomConfig(defaultProvider)
                                .build())
                        .addSource(MapConfigSourceBuilder.builder()
                                .setCustomConfig(chainProvider)
                                .build())
                        .addSource(MapConfigSourceBuilder.builder()
                                .setCustomConfig(cliProvider)
                                .build())
                        .build();
            }
            gestalt.loadConfigs();
            return gestalt.getConfig("config", Config.class);

        } catch (GestaltException e) {
            throw new ConfigLoadException(e);
        }
    }

    private static Map<String, String> getDefaultConfigMap() {
        Map<String, String> defaultProvider = new HashMap<>();
        defaultProvider.put("config.l2RpcUrl", "http://127.0.0.1:8545");
        defaultProvider.put("config.l2EngineUrl", "http://127.0.0.1:8551");
        defaultProvider.put("config.l1RpcUrl", "");
        defaultProvider.put("config.l1WsRpcUrl", "");
        defaultProvider.put("config.l1BeaconUrl", "");
        defaultProvider.put("config.l1BeaconArchiverUrl", "");
        defaultProvider.put("config.jwtSecret", "");
        defaultProvider.put("config.checkpointSyncUrl", "");
        defaultProvider.put("config.rpcPort", "9545");
        defaultProvider.put("config.rpcAddr", "0.0.0.0");
        defaultProvider.put("config.discPort", "9876");
        return defaultProvider;
    }

    /**
     * The type Cli config.
     *
     * @param l1RpcUrl            L1 chain rpc url.
     * @param l1WsRpcUrl          L1 chain websocket rpc url.
     * @param l1BeaconUrl         L1 chain beacon client API rpc url.
     * @param l1BeaconArchiverUrl L1 chain beacon archiver API rpc url.
     * @param l2RpcUrl            L2 chain rpc url.
     * @param l2EngineUrl         L2 engine API url.
     * @param jwtSecret           L2 engine API jwt secret.
     * @param checkpointSyncUrl   The checkpoint sync url.
     * @param rpcAddr             The rpc address.
     * @param rpcPort             The rpc port.
     * @param bootNodes           The custom bootNodes.
     * @param discPort            The custom discovery port.
     * @param syncMode            The sync mode.
     * @param sequencerEnable     The sequencer enable flag.
     * @param devnet              The devnet flag.
     */
    public record CliConfig(
            String l1RpcUrl,
            String l1WsRpcUrl,
            String l1BeaconUrl,
            String l1BeaconArchiverUrl,
            String l2RpcUrl,
            String l2EngineUrl,
            String jwtSecret,
            String checkpointSyncUrl,
            String rpcAddr,
            Integer rpcPort,
            List<String> bootNodes,
            Integer discPort,
            SyncMode syncMode,
            Boolean sequencerEnable,
            Boolean devnet) {

        /**
         * To configMap.
         *
         * @return the map
         */
        public Map<String, String> toConfigMap() {
            Map<String, String> map = new HashMap<>();
            if (StringUtils.isNotEmpty(l1RpcUrl)) {
                map.put("config.l1RpcUrl", l1RpcUrl);
            }
            if (StringUtils.isNotEmpty(l1WsRpcUrl)) {
                map.put("config.l1WsRpcUrl", l1WsRpcUrl);
            }
            if (StringUtils.isNotEmpty(l1BeaconUrl)) {
                map.put("config.l1BeaconUrl", l1BeaconUrl);
            }
            if (StringUtils.isNotEmpty(l1BeaconArchiverUrl)) {
                map.put("config.l1BeaconArchiverUrl", l1BeaconArchiverUrl);
            }
            if (StringUtils.isNotEmpty(l2RpcUrl)) {
                map.put("config.l2RpcUrl", l2RpcUrl);
            }
            if (StringUtils.isNotEmpty(l2EngineUrl)) {
                map.put("config.l2EngineUrl", l2EngineUrl);
            }
            if (StringUtils.isNotEmpty(jwtSecret)) {
                map.put("config.jwtSecret", jwtSecret);
            }
            if (StringUtils.isNotEmpty(checkpointSyncUrl)) {
                map.put("config.checkpointSyncUrl", checkpointSyncUrl);
            }
            if (bootNodes != null && !bootNodes.isEmpty()) {
                map.put("config.bootNodes", String.join(",", bootNodes));
            }
            if (discPort != null) {
                map.put("config.discPort", discPort.toString());
            }
            if (StringUtils.isNotEmpty(rpcAddr)) {
                map.put("config.rpcAddr", rpcAddr);
            }
            if (rpcPort != null) {
                map.put("config.rpcPort", rpcPort.toString());
            }
            if (syncMode != null) {
                map.put("config.syncMode", syncMode.toString());
            }
            map.put("config.sequencerEnable", String.valueOf(sequencerEnable != null && sequencerEnable));
            map.put("config.devnet", String.valueOf(devnet != null && devnet));
            return map;
        }
    }

    /**
     * The type ChainConfig.
     *
     * @param network              The network name.
     * @param l1ChainId            The L1 chain id.
     * @param l2ChainId            The L2 chain id.
     * @param l1StartEpoch         The L1 block referenced by the L2 chainConfig.
     * @param l2Genesis            The L2 genesis block info.
     * @param systemConfig         The initial system config value.
     * @param batchInbox           The batch inbox address.
     * @param depositContract      The deposit contract address.
     * @param systemConfigContract The L1 system config contract.
     * @param channelTimeout       The max timeout for a channel (as measured by the frame L1 block number).
     * @param seqWindowSize        Number of L1 blocks in a sequence window.
     * @param maxSeqDrift          Maximum timestamp drift.
     * @param regolithTime         Timestamp of the regolith hardfork.
     * @param canyonTime           Timestamp of the canyon hardfork.
     * @param deltaTime            Timestamp of the deltaTime hardfork.
     * @param ecotoneTime          Timestamp of the ecotone hardfork.
     * @param fjordTime            Timestamp of the fjord hardfork.
     * @param graniteTime          Timestamp of the granite hardfork.
     * @param blockTime            Network blocktime.
     * @param l2Tol1MessagePasser  L2 To L1 Message passer address.
     * @author grapebaba
     * @since 0.1.0
     */
    public record ChainConfig(
            String network,
            BigInteger l1ChainId,
            BigInteger l2ChainId,
            Epoch l1StartEpoch,
            BlockInfo l2Genesis,
            SystemConfig systemConfig,
            String batchInbox,
            String depositContract,
            String systemConfigContract,
            BigInteger channelTimeout,
            BigInteger seqWindowSize,
            BigInteger maxSeqDrift,
            BigInteger regolithTime,
            BigInteger canyonTime,
            BigInteger deltaTime,
            BigInteger ecotoneTime,
            BigInteger fjordTime,
            BigInteger graniteTime,
            BigInteger blockTime,
            String l2Tol1MessagePasser) {

        /**
         * Checking if the time is the ecotone activation block.
         *
         * @param time block time
         * @return true if the time is the ecotone activation block, otherwise false.
         */
        public boolean isEcotoneActivationBlock(BigInteger time) {
            return isEcotone(time)
                    && time.compareTo(this.blockTime) >= 0
                    && time.subtract(this.blockTime).compareTo(ecotoneTime) < 0;
        }

        /**
         * Check if the time is the ecotone activation block.
         *
         * @param time the block timestamp
         * @return true if the time is the ecotone activation block, otherwise false.
         */
        public boolean isEcotone(BigInteger time) {
            return ecotoneTime.compareTo(BigInteger.ZERO) >= 0 && time.compareTo(ecotoneTime) >= 0;
        }

        /**
         * Check if the time is the ecotone activation block and not the first ecotone block.
         *
         * @param time the block timestamp
         * @return true if the time is the ecotone activation block and not the first ecotone block, otherwise false.
         */
        public boolean isEcotoneAndNotFirst(BigInteger time) {
            return isEcotone(time) && time.compareTo(blockTime) >= 0 && isEcotone(time.subtract(blockTime));
        }

        /**
         * Check if the time is the fjord activation block.
         *
         * @param time the block timestamp
         * @return true if the time is the fjord activation block, otherwise false.
         */
        public boolean isFjord(BigInteger time) {
            return fjordTime.compareTo(BigInteger.ZERO) >= 0 && time.compareTo(fjordTime) >= 0;
        }

        /**
         * Check if the time is the fjord activation block.
         *
         * @param time the block timestamp
         * @return true if the time is the fjord activation block, otherwise false.
         */
        public boolean isFjordActivationBlock(BigInteger time) {
            return isFjord(time)
                    && time.compareTo(blockTime) >= 0
                    && time.subtract(blockTime).compareTo(fjordTime) < 0;
        }

        /**
         * Check if the time is the granite activation block.
         *
         * @param time the block timestamp
         * @return true if the time is the granite activation block, otherwise false.
         */
        public boolean isGranite(BigInteger time) {
            return graniteTime.compareTo(BigInteger.ZERO) >= 0 && time.compareTo(graniteTime) >= 0;
        }

        /**
         * Check if the time is the granite activation block.
         *
         * @param time the block timestamp
         * @return true if the time is the granite activation block, otherwise false.
         */
        public boolean isGraniteActivationBlock(BigInteger time) {
            return isFjord(time)
                    && time.compareTo(blockTime) >= 0
                    && time.subtract(blockTime).compareTo(fjordTime) < 0;
        }

        /**
         * Check if the time is the canyon activation block.
         *
         * @param time the block timestamp
         * @return true if the time is the canyon activation block, otherwise false.
         */
        public boolean isCanyon(BigInteger time) {
            return canyonTime.compareTo(BigInteger.ZERO) >= 0 && time.compareTo(canyonTime) >= 0;
        }

        /**
         * Max channel size int.
         *
         * @param time the time
         * @return the int
         */
        public int maxChannelSize(BigInteger time) {
            return isFjord(time) ? MAX_CHANNEL_SIZE_FJORD : MAX_CHANNEL_SIZE_BEDROCK;
        }

        /**
         * Max sequencer drift big integer.
         *
         * @param time the time
         * @return the big integer
         */
        public BigInteger maxSequencerDrift(BigInteger time) {
            return isFjord(time) ? BigInteger.valueOf(MAX_SEQUENCER_DRIFT_FJORD) : this.maxSeqDrift();
        }

        /**
         * Gets Channel timeout.
         *
         * @param time the current block timestamp
         * @return the channel timeout
         */
        public BigInteger channelTimeout(BigInteger time) {
            return isGranite(time) ? BigInteger.valueOf(CHANNEL_TIMEOUT_GRANITE) : this.channelTimeout();
        }

        /**
         * Optimism chain config.
         *
         * @return the chain config
         */
        public static ChainConfig optimism() {
            return new ChainConfig(
                    "optimism",
                    BigInteger.valueOf(1L),
                    BigInteger.valueOf(10L),
                    new Epoch(
                            BigInteger.valueOf(17422590L),
                            "0x438335a20d98863a4c0c97999eb2481921ccd28553eac6f913af7c12aec04108",
                            BigInteger.valueOf(1686068903L),
                            BigInteger.ZERO),
                    new BlockInfo(
                            "0xdbf6a80fef073de06add9b0d14026d6e5a86c85f6d102c36d3d8e9cf89c2afd3",
                            BigInteger.valueOf(105235063L),
                            "0x21a168dfa5e727926063a28ba16fd5ee84c814e847c81a699c7a0ea551e4ca50",
                            BigInteger.valueOf(1686068903L)),
                    new SystemConfig(
                            "0x6887246668a3b87f54deb3b94ba47a6f63f32985",
                            BigInteger.valueOf(30_000_000L),
                            BigInteger.valueOf(188L),
                            BigInteger.valueOf(684000L),
                            "0xAAAA45d9549EDA09E70937013520214382Ffc4A2"),
                    "0xff00000000000000000000000000000000000010",
                    "0xbEb5Fc579115071764c7423A4f12eDde41f106Ed",
                    "0x229047fed2591dbec1eF1118d64F7aF3dB9EB290",
                    BigInteger.valueOf(300L),
                    BigInteger.valueOf(3600L),
                    BigInteger.valueOf(600L),
                    BigInteger.ZERO,
                    BigInteger.valueOf(1704992401L),
                    BigInteger.valueOf(1708560000L),
                    BigInteger.valueOf(1710374401L),
                    BigInteger.valueOf(1720627201L),
                    BigInteger.valueOf(1726070401L),
                    BigInteger.valueOf(2L),
                    "0x4200000000000000000000000000000000000016");
        }

        /**
         * Base mainnet chain config.
         *
         * @return the chain config
         */
        public static ChainConfig base() {
            return new ChainConfig(
                    "base",
                    BigInteger.valueOf(1L),
                    BigInteger.valueOf(8453L),
                    new Epoch(
                            BigInteger.valueOf(17481768L),
                            "0x5c13d307623a926cd31415036c8b7fa14572f9dac64528e857a470511fc30771",
                            BigInteger.valueOf(1686789347L),
                            BigInteger.ZERO),
                    new BlockInfo(
                            "0xf712aa9241cc24369b143cf6dce85f0902a9731e70d66818a3a5845b296c73dd",
                            BigInteger.valueOf(0L),
                            Numeric.toHexString(new byte[32]),
                            BigInteger.valueOf(1686789347L)),
                    new SystemConfig(
                            "0x5050f69a9786f081509234f1a7f4684b5e5b76c9",
                            BigInteger.valueOf(30_000_000L),
                            BigInteger.valueOf(188),
                            BigInteger.valueOf(684000),
                            "0xAf6E19BE0F9cE7f8afd49a1824851023A8249e8a"),
                    "0xff00000000000000000000000000000000008453",
                    "0x49048044d57e1c92a77f79988d21fa8faf74e97e",
                    "0x73a79fab69143498ed3712e519a88a918e1f4072",
                    BigInteger.valueOf(300L),
                    BigInteger.valueOf(3600L),
                    BigInteger.valueOf(600L),
                    BigInteger.ZERO,
                    BigInteger.valueOf(1704992401L),
                    BigInteger.valueOf(1708560000L),
                    BigInteger.valueOf(1710374401L),
                    BigInteger.valueOf(1720627201L),
                    BigInteger.valueOf(1726070401L),
                    BigInteger.valueOf(2L),
                    "0x4200000000000000000000000000000000000016");
        }

        /**
         * Optimism sepolia ChainConfig.
         *
         * @return the chain config
         */
        public static ChainConfig optimismSepolia() {
            return new ChainConfig(
                    "optimism-sepolia",
                    BigInteger.valueOf(11155111L),
                    BigInteger.valueOf(11155420L),
                    new Epoch(
                            BigInteger.valueOf(4071408L),
                            "0x48f520cf4ddaf34c8336e6e490632ea3cf1e5e93b0b2bc6e917557e31845371b",
                            BigInteger.valueOf(1691802540L),
                            BigInteger.ZERO),
                    new BlockInfo(
                            "0x102de6ffb001480cc9b8b548fd05c34cd4f46ae4aa91759393db90ea0409887d",
                            BigInteger.valueOf(0L),
                            Numeric.toHexString(new byte[32]),
                            BigInteger.valueOf(1691802540L)),
                    new SystemConfig(
                            "0x8F23BB38F531600e5d8FDDaAEC41F13FaB46E98c",
                            BigInteger.valueOf(30_000_000L),
                            BigInteger.valueOf(188),
                            BigInteger.valueOf(684000),
                            "0x57CACBB0d30b01eb2462e5dC940c161aff3230D3"),
                    "0xff00000000000000000000000000000011155420",
                    "0x16fc5058f25648194471939df75cf27a2fdc48bc",
                    "0x034edd2a225f7f429a63e0f1d2084b9e0a93b538",
                    BigInteger.valueOf(300L),
                    BigInteger.valueOf(3600L),
                    BigInteger.valueOf(600L),
                    BigInteger.ZERO,
                    BigInteger.valueOf(1699981200L),
                    BigInteger.valueOf(1703203200L),
                    BigInteger.valueOf(1708534800L),
                    BigInteger.valueOf(1716998400L),
                    BigInteger.valueOf(1723478400L),
                    BigInteger.valueOf(2L),
                    "0x4200000000000000000000000000000000000016");
        }

        /**
         * Base sepolia chain config.
         *
         * @return the chain config
         */
        public static ChainConfig baseSepolia() {
            return new ChainConfig(
                    "base-sepolia",
                    BigInteger.valueOf(11155111L),
                    BigInteger.valueOf(84532L),
                    new Epoch(
                            BigInteger.valueOf(4370868L),
                            "0xcac9a83291d4dec146d6f7f69ab2304f23f5be87b1789119a0c5b1e4482444ed",
                            BigInteger.valueOf(1695768288L),
                            BigInteger.ZERO),
                    new BlockInfo(
                            "0x0dcc9e089e30b90ddfc55be9a37dd15bc551aeee999d2e2b51414c54eaf934e4",
                            BigInteger.valueOf(0L),
                            Numeric.toHexString(new byte[32]),
                            BigInteger.valueOf(1695768288L)),
                    new SystemConfig(
                            "0x6cdebe940bc0f26850285caca097c11c33103e47",
                            BigInteger.valueOf(25_000_000L),
                            BigInteger.valueOf(2100L),
                            BigInteger.valueOf(1000000L),
                            "0xb830b99c95Ea32300039624Cb567d324D4b1D83C"),
                    "0xff00000000000000000000000000000000084532",
                    "0x49f53e41452C74589E85cA1677426Ba426459e85",
                    "0xf272670eb55e895584501d564AfEB048bEd26194",
                    BigInteger.valueOf(300L),
                    BigInteger.valueOf(3600L),
                    BigInteger.valueOf(600L),
                    BigInteger.ZERO,
                    BigInteger.valueOf(1699981200L),
                    BigInteger.valueOf(1703203200L),
                    BigInteger.valueOf(1708534800L),
                    BigInteger.valueOf(1716998400L),
                    BigInteger.valueOf(1723478400L),
                    BigInteger.valueOf(2L),
                    "0x4200000000000000000000000000000000000016");
        }

        /**
         * External ChainConfig from json.
         *
         * @param filePath json file path
         * @return the chain config
         */
        public static ChainConfig fromJson(String filePath) {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            try {
                ExternalChainConfig externalChainConfig = mapper.readValue(
                        Files.readString(Path.of(filePath), StandardCharsets.UTF_8), ExternalChainConfig.class);
                return ChainConfig.fromExternal(externalChainConfig);
            } catch (IOException e) {
                throw new ConfigLoadException(e);
            }
        }

        /**
         * ChainConfig from External ChainConfig.
         *
         * @param external External ChainConfig
         * @return the chain config
         */
        public static ChainConfig fromExternal(ExternalChainConfig external) {
            return new ChainConfig(
                    "external",
                    external.l1ChainId,
                    external.l2ChainId,
                    new Epoch(external.genesis.l1.number, external.genesis.l1.hash, BigInteger.ZERO, BigInteger.ZERO),
                    new BlockInfo(
                            external.genesis.l2.hash,
                            external.genesis.l2.number,
                            Numeric.toHexString(new byte[32]),
                            external.genesis.l2Time),
                    new SystemConfig(
                            external.genesis.systemConfig.batcherAddr,
                            external.genesis.systemConfig.gasLimit,
                            Numeric.toBigInt(external.genesis.systemConfig.overhead),
                            Numeric.toBigInt(external.genesis.systemConfig.scalar),
                            Numeric.toHexString(new byte[32])),
                    external.batchInboxAddress,
                    external.depositContractAddress,
                    external.l1SystemConfigAddress,
                    external.channelTimeout,
                    external.seqWindowSize,
                    external.maxSequencerDrift,
                    external.regolithTime,
                    external.canyonTime == null ? BigInteger.valueOf(-1L) : external.canyonTime,
                    external.deltaTime == null ? BigInteger.valueOf(-1L) : external.deltaTime,
                    external.ecotoneTime == null ? BigInteger.valueOf(-1L) : external.ecotoneTime,
                    external.fjordTime == null ? BigInteger.valueOf(-1L) : external.fjordTime,
                    external.graniteTime == null ? BigInteger.valueOf(-1L) : external.graniteTime,
                    external.blockTime,
                    "0x4200000000000000000000000000000000000016");
        }

        /**
         * To ConfigMap.
         *
         * @return the map
         */
        public Map<String, String> toConfigMap() {
            return Map.ofEntries(
                    entry("config.chainConfig.network", this.network),
                    entry("config.chainConfig.l1ChainId", this.l1ChainId.toString()),
                    entry("config.chainConfig.l2ChainId", this.l2ChainId.toString()),
                    entry(
                            "config.chainConfig.l1StartEpoch.number",
                            this.l1StartEpoch.number().toString()),
                    entry(
                            "config.chainConfig.l1StartEpoch.timestamp",
                            this.l1StartEpoch.timestamp().toString()),
                    entry("config.chainConfig.l1StartEpoch.hash", this.l1StartEpoch.hash()),
                    entry(
                            "config.chainConfig.l1StartEpoch.sequenceNumber",
                            this.l1StartEpoch.sequenceNumber().toString()),
                    entry("config.chainConfig.l2Genesis.hash", this.l2Genesis.hash()),
                    entry("config.chainConfig.l2Genesis.parentHash", this.l2Genesis.parentHash()),
                    entry(
                            "config.chainConfig.l2Genesis.number",
                            this.l2Genesis.number().toString()),
                    entry(
                            "config.chainConfig.l2Genesis.timestamp",
                            this.l2Genesis.timestamp().toString()),
                    entry("config.chainConfig.systemConfig.batchSender", this.systemConfig.batchSender()),
                    entry(
                            "config.chainConfig.systemConfig.gasLimit",
                            this.systemConfig.gasLimit().toString()),
                    entry(
                            "config.chainConfig.systemConfig.l1FeeOverhead",
                            this.systemConfig.l1FeeOverhead().toString()),
                    entry(
                            "config.chainConfig.systemConfig.l1FeeScalar",
                            this.systemConfig.l1FeeScalar().toString()),
                    entry("config.chainConfig.systemConfig.unsafeBlockSigner", this.systemConfig.unsafeBlockSigner()),
                    entry("config.chainConfig.batchInbox", this.batchInbox),
                    entry("config.chainConfig.depositContract", this.depositContract),
                    entry("config.chainConfig.systemConfigContract", this.systemConfigContract),
                    entry("config.chainConfig.channelTimeout", this.channelTimeout.toString()),
                    entry("config.chainConfig.seqWindowSize", this.seqWindowSize.toString()),
                    entry("config.chainConfig.maxSeqDrift", this.maxSeqDrift.toString()),
                    entry("config.chainConfig.regolithTime", this.regolithTime.toString()),
                    entry("config.chainConfig.canyonTime", this.canyonTime.toString()),
                    entry("config.chainConfig.deltaTime", this.deltaTime.toString()),
                    entry("config.chainConfig.ecotoneTime", this.ecotoneTime.toString()),
                    entry("config.chainConfig.fjordTime", this.fjordTime.toString()),
                    entry("config.chainConfig.graniteTime", this.graniteTime.toString()),
                    entry("config.chainConfig.blockTime", this.blockTime.toString()),
                    entry("config.chainConfig.l2Tol1MessagePasser", this.l2Tol1MessagePasser));
        }
    }

    /**
     * The enum Sync mode.
     *
     * @author grapebaba
     * @since 0.1.0
     */
    public enum SyncMode {
        /**
         * Fast sync mode.
         */
        Fast,
        /**
         * Challenge sync mode.
         */
        Challenge,
        /**
         * Full sync mode.
         */
        Full,
        /**
         * Checkpoint sync mode.
         */
        Checkpoint,
        /**
         * Execution layer sync mode.
         */
        ExecutionLayer;

        /**
         * is execution layer sync mode
         *
         * @return true if execution layer sync mode, otherwise false.
         */
        public boolean isEl() {
            return this == ExecutionLayer;
        }

        /**
         * From sync mode.
         *
         * @param value the value
         * @return the sync mode
         */
        public static SyncMode from(String value) {
            return switch (value) {
                case "fast" -> Fast;
                case "challenge" -> Challenge;
                case "full" -> Full;
                case "checkpoint" -> Checkpoint;
                case "execution-layer" -> ExecutionLayer;
                default -> throw new RuntimeException("invalid sync mode");
            };
        }
    }

    /**
     * The type SystemAccounts.
     *
     * @param attributesDepositor attributes depositor.
     * @param attributesPreDeploy attributes preDeploy.
     * @param feeVault            fee vault.
     * @author grapebaba
     * @since 0.1.0
     */
    public record SystemAccounts(String attributesDepositor, String attributesPreDeploy, String feeVault) {

        /**
         * Create default SystemAccounts instance.
         *
         * @return the SystemAccounts
         */
        public static SystemAccounts defaultSystemAccounts() {
            return new SystemAccounts(
                    "0xdeaddeaddeaddeaddeaddeaddeaddeaddead0001",
                    "0x4200000000000000000000000000000000000015",
                    "0x4200000000000000000000000000000000000011");
        }
    }

    /**
     * The type SystemConfig.
     *
     * @param batchSender       batch sender address.
     * @param gasLimit          gas limit.
     * @param l1FeeOverhead     L1 fee overhead. Pre-Ecotone this is passed as-is to engine.                          Post-Ecotone this is always zero, and not passed into the engine.
     * @param l1FeeScalar       L1 fee scalar. Pre-Ecotone this is passed as-is to the engine.                          Post-Ecotone this encodes multiple pieces of scalar data.
     * @param unsafeBlockSigner unsafe block signer address.
     * @author grapebaba
     * @since 0.1.0
     */
    public record SystemConfig(
            String batchSender,
            BigInteger gasLimit,
            BigInteger l1FeeOverhead,
            BigInteger l1FeeScalar,
            String unsafeBlockSigner) {

        /**
         * Create SystemConfig from Bedrock tx input.
         *
         * @param unsafeBlockSigner the unsafe block signer
         * @param gasLimit          l2 gas limit
         * @param input             l2 block tx input
         * @return the system config
         */
        public static SystemConfig fromBedrockTxInput(String unsafeBlockSigner, BigInteger gasLimit, byte[] input) {
            final String batchSender = Numeric.toHexString(Arrays.copyOfRange(input, 176, 196));
            var l1FeeOverhead = Numeric.toBigInt(Arrays.copyOfRange(input, 196, 228));
            var l1FeeScalar = Numeric.toBigInt(Arrays.copyOfRange(input, 228, 260));
            return new Config.SystemConfig(batchSender, gasLimit, l1FeeOverhead, l1FeeScalar, unsafeBlockSigner);
        }

        /**
         * Create SystemConfig from Ecotone tx input.
         *
         * @param unsafeBlockSigner the unsafe block signer
         * @param gasLimit          l2 gas limit
         * @param input             l2 block tx input
         * @return the system config
         */
        public static SystemConfig fromEcotoneTxInput(String unsafeBlockSigner, BigInteger gasLimit, byte[] input) {
            final String batchSender = Numeric.toHexString(Arrays.copyOfRange(input, 144, 164));
            var originFeeScalar = Arrays.copyOfRange(input, 4, 12);
            var destFeeScalar = new byte[32];
            System.arraycopy(originFeeScalar, 0, destFeeScalar, 28, 4);
            System.arraycopy(originFeeScalar, 4, destFeeScalar, 24, 4);
            destFeeScalar[0] = 1;
            var l1FeeScalar = Numeric.toBigInt(destFeeScalar);
            return new Config.SystemConfig(batchSender, gasLimit, BigInteger.ZERO, l1FeeScalar, unsafeBlockSigner);
        }

        /**
         * Batch hash string.
         *
         * @return the string
         */
        public String batcherHash() {
            return Numeric.toHexStringWithPrefixZeroPadded(Numeric.toBigInt(batchSender), 64);
        }

        /**
         * Get base fee scalar.
         *
         * @return tuple contains blobBaseFeeScalar and baseFeeScalar
         */
        public Tuple2<BigInteger, BigInteger> ecotoneScalars() {
            var scalars = Numeric.toBytesPadded(l1FeeScalar, 32);
            var versionByte = scalars[0];
            if (versionByte == 0) {
                // Bedrock version L1 base fee scalar
                var blobBaseFeeScalar = BigInteger.ZERO;
                var baseFeeScalar = Numeric.toBigInt(Arrays.copyOfRange(scalars, 28, scalars.length));
                return new Tuple2<>(blobBaseFeeScalar, baseFeeScalar);
            } else if (versionByte == 1) {
                // Ecotone version L1 base fee scalar
                var blobBaseFeeScalar = Numeric.toBigInt(Arrays.copyOfRange(scalars, 24, 28));
                var baseFeeScalar = Numeric.toBigInt(Arrays.copyOfRange(scalars, 28, scalars.length));
                return new Tuple2<>(blobBaseFeeScalar, baseFeeScalar);
            }
            throw new IllegalStateException("invalid l1FeeScalar");
        }
    }

    /**
     * External chain config.
     *
     * <p>This is used to parse external chain configs from JSON. This interface corresponds to the
     * default output of the `op-node`
     *
     * @param genesis                external genesis info
     * @param blockTime              block time
     * @param maxSequencerDrift      max sequencer drift
     * @param seqWindowSize          seq window size
     * @param channelTimeout         channel timeout
     * @param l1ChainId              l1 chain id
     * @param l2ChainId              l2 chain id
     * @param regolithTime           regolith time
     * @param canyonTime             canyon time
     * @param deltaTime              delta time
     * @param ecotoneTime            ecotone time
     * @param fjordTime              fjord time
     * @param graniteTime            granite time
     * @param batchInboxAddress      batch inbox address
     * @param depositContractAddress deposit contract address
     * @param l1SystemConfigAddress  l1 system config address
     */
    @JsonSerialize
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record ExternalChainConfig(
            ExternalGenesisInfo genesis,
            BigInteger blockTime,
            BigInteger maxSequencerDrift,
            BigInteger seqWindowSize,
            BigInteger channelTimeout,
            BigInteger l1ChainId,
            BigInteger l2ChainId,
            BigInteger regolithTime,
            BigInteger canyonTime,
            BigInteger deltaTime,
            BigInteger ecotoneTime,
            BigInteger fjordTime,
            BigInteger graniteTime,
            String batchInboxAddress,
            String depositContractAddress,
            String l1SystemConfigAddress) {}

    /**
     * External Genesis Info.
     *
     * @param l1           L1 chain genesis info
     * @param l2           L2 chain genesis info
     * @param l2Time       L2 time
     * @param systemConfig system config
     */
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record ExternalGenesisInfo(
            ChainGenesisInfo l1, ChainGenesisInfo l2, BigInteger l2Time, SystemConfigInfo systemConfig) {}

    /**
     * system config info.
     *
     * @param batcherAddr batcher address
     * @param overhead    overhead
     * @param scalar      scalar
     * @param gasLimit    gas limit
     */
    @JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
    public record SystemConfigInfo(String batcherAddr, String overhead, String scalar, BigInteger gasLimit) {}

    /**
     * chain genesis info.
     *
     * @param hash   chain hash
     * @param number chain number
     */
    public record ChainGenesisInfo(String hash, BigInteger number) {}
}
