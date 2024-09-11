package io.optimism.cli;

import ch.qos.logback.classic.Level;
import io.micrometer.tracing.Tracer;
import io.optimism.cli.typeconverter.SyncModeConverter;
import io.optimism.config.Config;
import io.optimism.exceptions.HildrServiceExecutionException;
import io.optimism.runner.Runner;
import io.optimism.telemetry.InnerMetrics;
import io.optimism.telemetry.TracerTaskWrapper;
import io.optimism.types.enums.Logging;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import sun.misc.Signal;

/**
 * CLI handler.
 *
 * @author thinkAfCod
 * @since 2023.05
 */
@Command(name = "hildr", mixinStandardHelpOptions = true, description = "")
public class Cli implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(Cli.class);

    private static final int DEFAULT_METRICS_PORT = 9200;

    private static final int MAX_PORT_NUMBER = 65535;

    @Option(
            names = "--network",
            defaultValue = "optimism",
            description = "network type, or rollup.json file path, support: optimism-goerli, base-goerli")
    String network;

    @Option(names = "--l1-rpc-url", required = true, description = "The l1 chain RPC URL")
    String l1RpcUrl;

    @Option(names = "--l1-ws-rpc-url", required = true, description = "The l1 chain WS RPC URL")
    String l1WsRpcUrl;

    @Option(names = "--l1-beacon-url", required = true, description = "The l1 chain beacon client RPC URL")
    String l1BeaconUrl;

    @Option(names = "--l1-beacon-archiver-url", description = "The l1 beacon chain archiver RPC URL")
    String l1BeaconArchiverUrl;

    @Option(names = "--l2-rpc-url", required = true, description = "The L2 engine RPC URL")
    String l2RpcUrl;

    @Option(
            names = {"--sync-mode", "-m"},
            defaultValue = "full",
            converter = SyncModeConverter.class,
            description = "Sync Mode Specifies how `hildr` should sync the L2 chain")
    Config.SyncMode syncMode;

    @Option(names = "--l2-engine-url", required = true, description = "The L2 engine API URL")
    String l2EngineUrl;

    @Option(
            names = "--jwt-secret",
            description = "Engine API JWT Secret. This is used to authenticate with the engine API")
    String jwtSecret;

    @Option(names = "--jwt-file", description = "Path to a JWT secret to use for authenticated RPC endpoints")
    String jwtFile;

    @Option(
            names = {"--rpc-addr"},
            description = "The address of RPC server",
            defaultValue = "0.0.0.0")
    String rpcAddr;

    @Option(
            names = {"--rpc-port", "-p"},
            required = true,
            description = "The port of RPC server",
            defaultValue = "9545")
    Integer rpcPort;

    @Option(
            names = {"--checkpoint-hash"},
            description = "L2 checkpoint hash")
    String checkpointHash;

    @Option(
            names = {"--checkpoint-sync-url"},
            description = "A trusted L2 RPC URL to use for fast/checkpoint syncing")
    String checkpointSyncUrl;

    @Option(
            names = {"--metrics-enable"},
            description = "The flag of enabled metrics")
    Boolean metricsEnable;

    @Option(
            names = {"--metrics-port"},
            description = "The port of metrics server")
    Integer metricsPort;

    @Option(
            names = {"--disc-boot-nodes"},
            description = "The custom bootNodes")
    List<String> bootNodes;

    @Option(
            names = {"--disc-port"},
            description = "The port of discovery",
            defaultValue = "9876")
    Integer discPort;

    @Option(names = "--devnet", description = "Dev net flag")
    Boolean devnet;

    @Option(
            names = "--sequencer-enable",
            defaultValue = "false",
            description =
                    "Enable sequencing of new L2 blocks. A separate batch submitter has to be deployed to publish the data for verifiers.")
    Boolean sequencerEnable;

    @Option(
            names = "--log-level",
            defaultValue = "INFO",
            converter = LogLevelConverter.class,
            description = "Log level")
    Level logLevel;

    /** the Cli constructor. */
    public Cli() {}

    @Override
    public void run() {
        var logger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        if (logger instanceof ch.qos.logback.classic.Logger logbackLogger) {
            logbackLogger.setLevel(logLevel);
        }
        TracerTaskWrapper.setTracerSupplier(Logging.INSTANCE::getTracer);
        if (Boolean.TRUE.equals(metricsEnable)) {
            var metricsPort = this.metricsPort;
            if (metricsPort == null || metricsPort > MAX_PORT_NUMBER) {
                metricsPort = DEFAULT_METRICS_PORT;
            }
            InnerMetrics.start(metricsPort);
        }

        Signal.handle(new Signal("INT"), sig -> System.exit(0));
        Signal.handle(new Signal("TERM"), sig -> System.exit(0));

        var syncMode = this.syncMode;
        var checkpointHash = this.checkpointHash;
        var config = this.toConfig();
        Runner runner = Runner.create(config).setSyncMode(syncMode).setCheckpointHash(checkpointHash);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOGGER.info("hildr: shutdown");
            runner.stopAsync().awaitTerminated();
        }));
        Tracer tracer = Logging.INSTANCE.getTracer("hildr-cli");
        var span = tracer.nextSpan().name("start-runner").start();
        try (var unused = tracer.withSpan(span)) {
            runner.startAsync().awaitTerminated();
        } catch (Exception e) {
            LOGGER.error("hildr: ", e);
            throw new HildrServiceExecutionException(e);
        } finally {
            LOGGER.info("stop inner metrics");
            InnerMetrics.stop();
            span.end();
        }
    }

    @SuppressWarnings("checkstyle:Indentation")
    private Config toConfig() {
        Config.ChainConfig chain =
                switch (network) {
                    case "optimism" -> Config.ChainConfig.optimism();
                    case "optimism-sepolia" -> Config.ChainConfig.optimismSepolia();
                    case "base" -> Config.ChainConfig.base();
                    case "base-sepolia" -> Config.ChainConfig.baseSepolia();
                    default -> {
                        if (network.endsWith(".json")) {
                            yield Config.ChainConfig.fromJson(network);
                        }
                        throw new RuntimeException("network not recognized");
                    }
                };

        var configPath = Paths.get(System.getProperty("user.home"), ".hildr/hildr.toml");
        var cliConfig = from(Cli.this);
        return Config.create(Files.exists(configPath) ? configPath : null, cliConfig, chain);
    }

    private String getJwtSecret() {
        if (StringUtils.isNotEmpty(Cli.this.jwtSecret)) {
            return Cli.this.jwtSecret;
        }
        return Cli.this.getJwtFromFile();
    }

    private String getJwtFromFile() {
        final Path jwtFilePath = StringUtils.isNotEmpty(Cli.this.jwtFile)
                ? Paths.get(Cli.this.jwtFile)
                : Paths.get(System.getProperty("user.dir"), "jwt.hex");
        if (!Files.exists(jwtFilePath)) {
            throw new RuntimeException("Failed to read JWT secret from file: %s".formatted(jwtFilePath));
        }
        try {
            return Files.readString(jwtFilePath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JWT secret from file: %s".formatted(jwtFilePath), e);
        }
    }

    private Config.CliConfig from(Cli cli) {
        return new Config.CliConfig(
                cli.l1RpcUrl,
                cli.l1WsRpcUrl,
                cli.l1BeaconUrl,
                cli.l1BeaconArchiverUrl,
                cli.l2RpcUrl,
                cli.l2EngineUrl,
                StringUtils.trim(Cli.this.getJwtSecret()),
                cli.checkpointSyncUrl,
                cli.rpcAddr,
                cli.rpcPort,
                cli.bootNodes,
                cli.discPort,
                cli.syncMode,
                cli.sequencerEnable,
                cli.devnet);
    }
}
