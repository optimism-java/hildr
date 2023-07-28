/*
 * Copyright ConsenSys Software Inc., 2022
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

package io.optimism.network;

import static com.google.common.base.Preconditions.checkNotNull;

import io.optimism.config.Config;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt64;
import org.hyperledger.besu.metrics.noop.NoOpMetricsSystem;
import org.hyperledger.besu.plugin.services.MetricsSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.pegasys.teku.infrastructure.async.AsyncRunner;
import tech.pegasys.teku.infrastructure.async.AsyncRunnerFactory;
import tech.pegasys.teku.infrastructure.async.MetricTrackingExecutorFactory;
import tech.pegasys.teku.infrastructure.time.SystemTimeProvider;
import tech.pegasys.teku.networking.p2p.connection.PeerPools;
import tech.pegasys.teku.networking.p2p.discovery.DiscoveryConfig;
import tech.pegasys.teku.networking.p2p.discovery.DiscoveryPeer;
import tech.pegasys.teku.networking.p2p.discovery.DiscoveryService;
import tech.pegasys.teku.networking.p2p.discovery.noop.NoOpDiscoveryService;
import tech.pegasys.teku.networking.p2p.libp2p.LibP2PNetworkBuilder;
import tech.pegasys.teku.networking.p2p.network.P2PNetwork;
import tech.pegasys.teku.networking.p2p.network.config.NetworkConfig;
import tech.pegasys.teku.networking.p2p.reputation.DefaultReputationManager;
import tech.pegasys.teku.networking.p2p.reputation.ReputationManager;
import tech.pegasys.teku.storage.store.KeyValueStore;
import tech.pegasys.teku.storage.store.MemKeyValueStore;

/**
 * The type OpStackP2PNetwork.
 *
 * @author grapebaba
 * @since 0.1.1
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class OpStackP2PNetwork {

  private static final Logger LOGGER = LoggerFactory.getLogger(OpStackP2PNetwork.class);

  private final DiscoveryService discoveryService;

  private final UInt64 chainId;

  /**
   * Instantiates a new Op stack p 2 p network.
   *
   * @param config the config
   */
  public OpStackP2PNetwork(Config.ChainConfig config) {
    this.chainId = UInt64.valueOf(config.l2ChainId());
    MetricsSystem metricsSystem = new NoOpMetricsSystem();
    final PeerPools peerPools = new PeerPools();
    final ReputationManager reputationManager =
        new DefaultReputationManager(metricsSystem, new SystemTimeProvider(), 1024, peerPools);
    final DiscoveryConfig discoveryConfig =
        DiscoveryConfig.builder()
            .listenUdpPort(7777)
            .bootnodes(
                List.of(
                    "enr:-J64QBbwPjPLZ6IOOToOLsSjtFUjjzN66qmBZdUexpO32Klrc"
                        + "458Q24kbty2PdRaLacHM5z-cZQr8mjeQu3pik6jPS"
                        + "OGAYYFIqBfgmlkgnY0gmlwhDaRWFWHb3BzdGFja4SzlAUAi"
                        + "XNlY3AyNTZrMaECmeSnJh7zjKrDSPoNMGXoopeDF4"
                        + "hhpj5I0OsQUUt4u8uDdGNwgiQGg3VkcIIkBg",
                    "enr:-J64QAlTCDa188Hl1OGv5_2Kj2nWCsvxMVc_rEnLtw7RPFbOf"
                        + "qUOV6khXT_PH6cC603I2ynY31rSQ8sI9gLeJbfFGa"
                        + "WGAYYFIrpdgmlkgnY0gmlwhANWgzCHb3BzdGFja4SzlAUAi"
                        + "XNlY3AyNTZrMaECkySjcg-2v0uWAsFsZZu43qNHpp"
                        + "Gr2D5F913Qqs5jDCGDdGNwgiQGg3VkcIIkBg",
                    "enr:-J24QGEzN4mJgLWNTUNwj7riVJ2ZjRLenOFccl2dbRFxHHOC"
                        + "CZx8SXWzgf-sLzrGs6QgqSFCvGXVgGPBkRkfOWlT1-"
                        + "iGAYe6Cu93gmlkgnY0gmlwhCJBEUSHb3BzdGFja4OkAwC"
                        + "Jc2VjcDI1NmsxoQLuYIwaYOHg3CUQhCkS-RsSHmUd1b"
                        + "_x93-9yQ5ItS6udIN0Y3CCIyuDdWRwgiMr"))
            .build();
    final NetworkConfig p2pConfig = NetworkConfig.builder().build();
    final AsyncRunner asyncRunner =
        AsyncRunnerFactory.createDefault(new MetricTrackingExecutorFactory(metricsSystem))
            .create("op_stack", 20);
    final KeyValueStore<String, Bytes> kvStore = new MemKeyValueStore<>();
    final P2PNetwork<?> p2pNetwork =
        LibP2PNetworkBuilder.create()
            .metricsSystem(metricsSystem)
            .asyncRunner(
                AsyncRunnerFactory.createDefault(new MetricTrackingExecutorFactory(metricsSystem))
                    .create("op_stack_p2p", 20))
            .config(p2pConfig)
            //                                .privateKeyProvider(new LibP2PPrivateKeyLoader(new
            // FileKeyValueStore(Path.of("/.hildr-node").resolve("kvstore")), Optional.empty()))
            .privateKeyProvider(PrivateKeyGenerator::generate)
            .reputationManager(reputationManager)
            .rpcMethods(Collections.emptyList())
            .peerHandlers(Collections.emptyList())
            .preparedGossipMessageFactory(
                (topic, payload, networkingSpecConfig) -> {
                  throw new UnsupportedOperationException();
                })
            .gossipTopicFilter(topic -> true)
            .build();
    if (discoveryConfig.isDiscoveryEnabled()) {
      checkNotNull(metricsSystem);
      checkNotNull(asyncRunner);
      checkNotNull(p2pConfig);
      checkNotNull(kvStore);
      checkNotNull(p2pNetwork);

      discoveryService =
          new DiscV5Service(
              metricsSystem,
              asyncRunner,
              discoveryConfig,
              p2pConfig,
              kvStore,
              p2pNetwork.getPrivateKey(),
              DiscV5Service.createDefaultDiscoverySystemBuilder(),
              chainId,
              new NodeRecordConverter());

    } else {
      discoveryService = new NoOpDiscoveryService();
    }

    //        network = DiscoveryNetworkBuilder.create()
    //                .metricsSystem(metricsSystem)
    //                .asyncRunner(AsyncRunnerFactory.createDefault(new
    // MetricTrackingExecutorFactory(metricsSystem)).create("op_stack_disc", 20))
    //                .kvStore(new MemKeyValueStore<>())
    //                .p2pNetwork(
    //                        LibP2PNetworkBuilder.create()
    //                                .metricsSystem(metricsSystem)
    //                                .asyncRunner(AsyncRunnerFactory.createDefault(new
    // MetricTrackingExecutorFactory(metricsSystem)).create("op_stack_p2p", 20))
    //                                .config(p2pConfig)
    ////                                .privateKeyProvider(new LibP2PPrivateKeyLoader(new
    // FileKeyValueStore(Path.of("/.hildr-node").resolve("kvstore")), Optional.empty()))
    //                                .privateKeyProvider(PrivateKeyGenerator::generate)
    //                                .reputationManager(reputationManager)
    //                                .rpcMethods(Collections.emptyList())
    //                                .peerHandlers(Collections.emptyList())
    //                                .preparedGossipMessageFactory(
    //                                        (topic, payload, networkingSpecConfig) -> {
    //                                            throw new UnsupportedOperationException();
    //                                        })
    //                                .gossipTopicFilter(topic -> true)
    //                                .build())
    //                .peerPools(peerPools)
    //                .peerSelectionStrategy(new SimplePeerSelectionStrategy(new
    // TargetPeerRange(DiscoveryConfig.DEFAULT_P2P_PEERS_LOWER_BOUND, DEFAULT_P2P_PEERS_UPPER_BOUND,
    // Math.max(1, DEFAULT_P2P_PEERS_LOWER_BOUND * 2 / 10))))
    //                .discoveryConfig(discoveryConfig)
    //                .p2pConfig(p2pConfig)
    //                .spec(spec)
    //                .currentSchemaDefinitionsSupplier(spec::getGenesisSchemaDefinitions)
    //                .build();

  }

  /** Start. */
  public void start() {
    try {
      discoveryService.start().get(30, TimeUnit.SECONDS);
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Search peers list.
   *
   * @return the list
   * @throws ExecutionException the execution exception
   * @throws InterruptedException the interrupted exception
   */
  public List<DiscoveryPeer> searchPeers() throws ExecutionException, InterruptedException {
    return discoveryService.searchForPeers().get().stream().toList();
  }

  /** Stop. */
  public void stop() {
    try {
      discoveryService.stop().get(30, TimeUnit.SECONDS);
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      throw new RuntimeException(e);
    }
  }
}
