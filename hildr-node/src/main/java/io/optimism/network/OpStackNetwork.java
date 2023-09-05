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

package io.optimism.network;

import com.google.common.collect.ImmutableSet;
import io.optimism.config.Config;
import io.optimism.engine.ExecutionPayload;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt64;
import org.hyperledger.besu.metrics.StandardMetricCategory;
import org.hyperledger.besu.metrics.prometheus.PrometheusMetricsSystem;
import org.hyperledger.besu.plugin.services.MetricsSystem;
import org.hyperledger.besu.plugin.services.metrics.MetricCategory;
import org.jctools.queues.MessagePassingQueue;
import org.slf4j.Logger;
import tech.pegasys.teku.infrastructure.async.AsyncRunner;
import tech.pegasys.teku.infrastructure.async.AsyncRunnerFactory;
import tech.pegasys.teku.infrastructure.async.MetricTrackingExecutorFactory;
import tech.pegasys.teku.infrastructure.time.SystemTimeProvider;
import tech.pegasys.teku.networking.p2p.connection.PeerPools;
import tech.pegasys.teku.networking.p2p.connection.PeerSelectionStrategy;
import tech.pegasys.teku.networking.p2p.connection.TargetPeerRange;
import tech.pegasys.teku.networking.p2p.discovery.DiscoveryConfig;
import tech.pegasys.teku.networking.p2p.gossip.PreparedGossipMessageFactory;
import tech.pegasys.teku.networking.p2p.gossip.TopicHandler;
import tech.pegasys.teku.networking.p2p.libp2p.LibP2PNetworkBuilder;
import tech.pegasys.teku.networking.p2p.network.P2PNetwork;
import tech.pegasys.teku.networking.p2p.network.config.NetworkConfig;
import tech.pegasys.teku.networking.p2p.reputation.DefaultReputationManager;
import tech.pegasys.teku.networking.p2p.reputation.ReputationManager;
import tech.pegasys.teku.storage.store.KeyValueStore;
import tech.pegasys.teku.storage.store.MemKeyValueStore;

/**
 * The type OpStackNetwork.
 *
 * @author grapebaba
 * @since 0.1.1
 */
@SuppressWarnings({"checkstyle:AbbreviationAsWordInName", "checkstyle:AbbreviationAsWordInName"})
public class OpStackNetwork {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(OpStackNetwork.class);
    private static final List<String> BOOTNODES = List.of(
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
                    + "_x93-9yQ5ItS6udIN0Y3CCIyuDdWRwgiMr");

    private final P2PNetwork<?> p2pNetwork;

    private final TopicHandler topicHandler;

    /**
     * Instantiates a new OpStackNetwork.
     *
     * @param config the config
     * @param unsafeBlockQueue the unsafe block queue
     */
    public OpStackNetwork(Config.ChainConfig config, MessagePassingQueue<ExecutionPayload> unsafeBlockQueue) {
        UInt64 chainId = UInt64.valueOf(config.l2ChainId());
        final MetricsSystem metricsSystem = new PrometheusMetricsSystem(
                ImmutableSet.<MetricCategory>builder()
                        .addAll(EnumSet.allOf(StandardMetricCategory.class))
                        .addAll(EnumSet.allOf(HildrNodeMetricsCategory.class))
                        .build(),
                true);
        final PeerPools peerPools = new PeerPools();
        final ReputationManager reputationManager =
                new DefaultReputationManager(metricsSystem, new SystemTimeProvider(), 1024, peerPools);
        final DiscoveryConfig discoveryConfig = DiscoveryConfig.builder()
                .listenUdpPortDefault(9876)
                .bootnodes(BOOTNODES)
                .build();
        final NetworkConfig p2pConfig = NetworkConfig.builder().build();
        final AsyncRunner asyncRunner = AsyncRunnerFactory.createDefault(
                        new MetricTrackingExecutorFactory(metricsSystem))
                .create("hildr_node_p2p", 20);
        final KeyValueStore<String, Bytes> kvStore = new MemKeyValueStore<>();
        final PreparedGossipMessageFactory preparedGossipMessageFactory = new SnappyPreparedGossipMessageFactory();

        this.topicHandler = new BlockTopicHandler(
                new SnappyPreparedGossipMessageFactory(),
                asyncRunner,
                chainId,
                config.systemConfig().unsafeBlockSigner(),
                unsafeBlockQueue);
        final P2PNetwork<?> p2pNetwork = LibP2PNetworkBuilder.create()
                .metricsSystem(metricsSystem)
                .asyncRunner(asyncRunner)
                .config(p2pConfig)
                .privateKeyProvider(PrivateKeyGenerator::generate)
                .reputationManager(reputationManager)
                .rpcMethods(Collections.emptyList())
                .peerHandlers(Collections.emptyList())
                .preparedGossipMessageFactory(preparedGossipMessageFactory)
                .gossipTopicFilter(topic -> true)
                .build();
        final OpStackP2PNetworkBuilder builder = new OpStackP2PNetworkBuilder();
        final TargetPeerRange targetPeerRange = new TargetPeerRange(
                discoveryConfig.getMinPeers(),
                discoveryConfig.getMaxPeers(),
                discoveryConfig.getMinRandomlySelectedPeers());
        final PeerSelectionStrategy peerSelectionStrategy = new SimplePeerSelectionStrategy(targetPeerRange);
        this.p2pNetwork = builder.kvStore(kvStore)
                .asyncRunner(asyncRunner)
                .discoveryConfig(discoveryConfig)
                .metricsSystem(metricsSystem)
                .p2pConfig(p2pConfig)
                .p2pNetwork(p2pNetwork)
                .peerPools(peerPools)
                .peerSelectionStrategy(peerSelectionStrategy)
                .chainId(chainId)
                .build();
    }

    /** Start. */
    public void start() {
        this.p2pNetwork
                .start()
                .thenAccept((Consumer<Object>)
                        o -> p2pNetwork.subscribe(((BlockTopicHandler) topicHandler).getTopic(), topicHandler))
                .finish(error -> {
                    if (error != null) {
                        LOGGER.error("Error starting p2p network", error);
                        throw new RuntimeException(error);
                    }
                });
    }

    /** Stop. */
    public void stop() {
        this.p2pNetwork.stop().finish(error -> {
            if (error != null) {
                LOGGER.error("Error stopping p2p network", error);
            }
        });
    }
}
