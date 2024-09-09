package io.optimism.network;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toList;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt64;
import org.hyperledger.besu.plugin.services.MetricsSystem;
import tech.pegasys.teku.infrastructure.async.AsyncRunner;
import tech.pegasys.teku.networking.p2p.connection.ConnectionManager;
import tech.pegasys.teku.networking.p2p.connection.PeerPools;
import tech.pegasys.teku.networking.p2p.connection.PeerSelectionStrategy;
import tech.pegasys.teku.networking.p2p.discovery.DiscoveryConfig;
import tech.pegasys.teku.networking.p2p.discovery.DiscoveryService;
import tech.pegasys.teku.networking.p2p.discovery.noop.NoOpDiscoveryService;
import tech.pegasys.teku.networking.p2p.network.P2PNetwork;
import tech.pegasys.teku.networking.p2p.network.config.NetworkConfig;
import tech.pegasys.teku.storage.store.KeyValueStore;

/**
 * The type OpStackP2PNetworkBuilder.
 *
 * @author grapebaba
 * @version 0.1.1
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class OpStackP2PNetworkBuilder {

    /**
     * Create OpStackP2PNetworkBuilder.
     *
     * @return the OpStackP2PNetworkBuilder
     */
    public static OpStackP2PNetworkBuilder create() {
        return new OpStackP2PNetworkBuilder();
    }

    /** The Metrics system. */
    protected MetricsSystem metricsSystem;

    /** The Async runner. */
    protected AsyncRunner asyncRunner;

    /** The Kv store. */
    protected KeyValueStore<String, Bytes> kvStore;

    /** The P 2 p network. */
    protected P2PNetwork<?> p2pNetwork;

    /** The Peer pools. */
    protected PeerPools peerPools;

    /** The Peer selection strategy. */
    protected PeerSelectionStrategy peerSelectionStrategy;

    /** The Discovery config. */
    protected DiscoveryConfig discoveryConfig;

    /** The P 2 p config. */
    protected NetworkConfig p2pConfig;

    /** The Discovery service. */
    protected DiscoveryService discoveryService;

    /** The Connection manager. */
    protected ConnectionManager connectionManager;

    /** The Chain id. */
    protected UInt64 chainId;

    /** Instantiates a new OpStackP2PNetworkBuilder. */
    protected OpStackP2PNetworkBuilder() {}

    /** Init missing defaults. */
    protected void initMissingDefaults() {
        if (discoveryService == null) {
            discoveryService = createDiscoveryService();
        }

        if (connectionManager == null) {
            connectionManager = createConnectionManager();
        }
    }

    /**
     * Build OpStackP2PNetwork.
     *
     * @return the OpStackP2PNetwork
     */
    public OpStackP2PNetwork<?> build() {
        initMissingDefaults();

        checkNotNull(p2pNetwork);
        checkNotNull(discoveryService);
        checkNotNull(connectionManager);

        return new OpStackP2PNetwork<>(p2pNetwork, discoveryService, connectionManager);
    }

    /**
     * Create connection manager.
     *
     * @return the ConnectionManager
     */
    protected ConnectionManager createConnectionManager() {
        checkNotNull(metricsSystem);
        checkNotNull(discoveryService);
        checkNotNull(asyncRunner);
        checkNotNull(p2pNetwork);
        checkNotNull(peerSelectionStrategy);
        checkNotNull(discoveryConfig);

        return new ConnectionManager(
                metricsSystem,
                discoveryService,
                asyncRunner,
                p2pNetwork,
                peerSelectionStrategy,
                discoveryConfig.getStaticPeers().stream()
                        .map(p2pNetwork::createPeerAddress)
                        .collect(toList()),
                peerPools);
    }

    /**
     * Create discovery service.
     *
     * @return the DiscoveryService
     */
    protected DiscoveryService createDiscoveryService() {
        final DiscoveryService discoveryService;

        checkNotNull(discoveryConfig);
        if (discoveryConfig.isDiscoveryEnabled()) {
            checkNotNull(metricsSystem);
            checkNotNull(asyncRunner);
            checkNotNull(p2pConfig);
            checkNotNull(kvStore);
            checkNotNull(p2pNetwork);

            discoveryService = new DiscV5Service(
                    metricsSystem,
                    asyncRunner,
                    discoveryConfig,
                    p2pConfig,
                    kvStore,
                    p2pNetwork.getPrivateKey(),
                    DiscV5Service.createDefaultDiscoverySystemBuilder(),
                    this.chainId,
                    new NodeRecordConverter());
        } else {
            discoveryService = new NoOpDiscoveryService();
        }
        return discoveryService;
    }

    /**
     * Metrics system OpStackP2PNetworkBuilder.
     *
     * @param metricsSystem the metrics system
     * @return the OpStackP2PNetworkBuilder
     */
    public OpStackP2PNetworkBuilder metricsSystem(MetricsSystem metricsSystem) {
        this.metricsSystem = metricsSystem;
        return this;
    }

    /**
     * Async runner OpStackP2PNetworkBuilder.
     *
     * @param asyncRunner the async runner
     * @return the OpStackP2PNetworkBuilder
     */
    public OpStackP2PNetworkBuilder asyncRunner(AsyncRunner asyncRunner) {
        this.asyncRunner = asyncRunner;
        return this;
    }

    /**
     * kvStore OpStackP2PNetworkBuilder.
     *
     * @param kvStore the kvStore
     * @return the OpStackP2PNetworkBuilder
     */
    public OpStackP2PNetworkBuilder kvStore(KeyValueStore<String, Bytes> kvStore) {
        this.kvStore = kvStore;
        return this;
    }

    /**
     * p2pNetwork OpStackP2PNetworkBuilder.
     *
     * @param p2pNetwork the p2pNetwork
     * @return the OpStackP2PNetworkBuilder
     */
    public OpStackP2PNetworkBuilder p2pNetwork(P2PNetwork<?> p2pNetwork) {
        this.p2pNetwork = p2pNetwork;
        return this;
    }

    /**
     * Peer pools OpStackP2PNetworkBuilder.
     *
     * @param peerPools the peer pools
     * @return the OpStackP2PNetworkBuilder
     */
    public OpStackP2PNetworkBuilder peerPools(PeerPools peerPools) {
        this.peerPools = peerPools;
        return this;
    }

    /**
     * Peer selection strategy OpStackP2PNetworkBuilder.
     *
     * @param peerSelectionStrategy the peer selection strategy
     * @return the OpStackP2PNetworkBuilder
     */
    public OpStackP2PNetworkBuilder peerSelectionStrategy(PeerSelectionStrategy peerSelectionStrategy) {
        this.peerSelectionStrategy = peerSelectionStrategy;
        return this;
    }

    /**
     * Discovery config OpStackP2PNetworkBuilder.
     *
     * @param discoveryConfig the discoveryConfig
     * @return the OpStackP2PNetworkBuilder
     */
    public OpStackP2PNetworkBuilder discoveryConfig(DiscoveryConfig discoveryConfig) {
        this.discoveryConfig = discoveryConfig;
        return this;
    }

    /**
     * p2pConfigOpStackP2PNetworkBuilder.
     *
     * @param p2pConfig the p2pConfig
     * @return the OpStackP2PNetworkBuilder
     */
    public OpStackP2PNetworkBuilder p2pConfig(NetworkConfig p2pConfig) {
        this.p2pConfig = p2pConfig;
        return this;
    }

    /**
     * Discovery service OpStackP2PNetworkBuilder.
     *
     * @param discoveryService the discovery service
     * @return the OpStackP2PNetworkBuilder
     */
    public OpStackP2PNetworkBuilder discoveryService(DiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
        return this;
    }

    /**
     * Connection manager OpStackP2PNetworkBuilder.
     *
     * @param connectionManager the connection manager
     * @return the OpStackP2PNetworkBuilder
     */
    public OpStackP2PNetworkBuilder connectionManager(ConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
        return this;
    }

    /**
     * Chain id OpStackP2PNetworkBuilder.
     *
     * @param chainId the chain id
     * @return the OpStackP2PNetworkBuilder
     */
    public OpStackP2PNetworkBuilder chainId(UInt64 chainId) {
        this.chainId = chainId;
        return this;
    }
}
