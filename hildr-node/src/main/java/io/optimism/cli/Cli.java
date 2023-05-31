/*
 * Copyright 2023 281165273grape@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package io.optimism.cli;

import io.micrometer.tracing.Tracer;
import io.optimism.cli.typeconverter.SyncModeConverter;
import io.optimism.config.Config;
import io.optimism.runner.Runner;
import io.optimism.telemetry.InnerMetrics;
import io.optimism.telemetry.Logging;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * CLI handler.
 *
 * @author thinkAfCod
 * @since 2023.05
 */
@Command(name = "hildr", mixinStandardHelpOptions = true, description = "")
public class Cli implements Runnable {

  private static final Logger logger = LoggerFactory.getLogger(Cli.class);

  @Option(names = "--network", description = "network type, support: optimism-goerli, base-goerli")
  String network;

  @Option(names = "--l1-rpc-url", required = true, description = "The base chain RPC URL")
  String l1RpcUrl;

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

  @Option(
      names = {"--verbose", "-v"},
      description = "")
  Boolean verbose;

  @Option(
      names = {"--rpc-port", "-p"},
      required = true,
      description = "The port of RPC server")
  Integer rpcPort;

  @Option(
      names = {"--checkpoint-hash"},
      description = "L2 checkpoint hash")
  String checkpointHash;

  @Option(
      names = {"--checkpoint-sync-url"},
      description = "A trusted L2 RPC URL to use for fast/checkpoint syncing")
  String checkpointSyncUrl;

  /** the Cli constructor. */
  public Cli() {}

  @Override
  public void run() {
    var syncMode = this.syncMode;
    var unusedVerbose = this.verbose;
    var checkpointHash = this.checkpointHash;
    var config = this.toConfig();

    Tracer tracer = Logging.INSTANCE.getTracer();
    InnerMetrics.start(9200);

    Runner runner = Runner.create(config).setSyncMode(syncMode).setCheckpointHash(checkpointHash);
    var span = tracer.nextSpan().name("start-runner").start();
    try (var unused = tracer.withSpan(span)) {
      runner.startAsync().awaitTerminated();
    } catch (Exception e) {
      logger.error("hildr: ", e);
      throw new RuntimeException(e);
    }
  }

  private Config toConfig() {
    Config.ChainConfig chain;
    if ("optimism-goerli".equals(network)) {
      chain = Config.ChainConfig.optimismGoerli();
    } else if ("base-goerli".equals(network)) {
      chain = Config.ChainConfig.baseGoerli();
    } else {
      throw new RuntimeException("network not recognized");
    }

    var configPath = Paths.get(System.getProperty("user.home"), ".magi/magi.toml");
    var cliConfig = from(Cli.this);
    return Config.create(configPath, cliConfig, chain);
  }

  private Config.CliConfig from(Cli cli) {
    return new Config.CliConfig(
        cli.l1RpcUrl,
        cli.l2RpcUrl,
        cli.l2EngineUrl,
        cli.jwtSecret,
        cli.checkpointSyncUrl,
        cli.rpcPort);
  }
}