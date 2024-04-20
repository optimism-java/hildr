package io.optimism.network;

import static java.util.stream.Collectors.toList;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.crypto.SECP256K1.SecretKey;
import org.apache.tuweni.units.bigints.UInt64;
import org.ethereum.beacon.discovery.AddressAccessPolicy;
import org.ethereum.beacon.discovery.DiscoverySystem;
import org.ethereum.beacon.discovery.DiscoverySystemBuilder;
import org.ethereum.beacon.discovery.schema.EnrField;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import org.ethereum.beacon.discovery.schema.NodeRecordBuilder;
import org.ethereum.beacon.discovery.schema.NodeRecordFactory;
import org.ethereum.beacon.discovery.storage.NewAddressHandler;
import org.hyperledger.besu.plugin.services.MetricsSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.pegasys.teku.infrastructure.async.AsyncRunner;
import tech.pegasys.teku.infrastructure.async.Cancellable;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.infrastructure.metrics.TekuMetricCategory;
import tech.pegasys.teku.networking.p2p.discovery.DiscoveryConfig;
import tech.pegasys.teku.networking.p2p.discovery.DiscoveryPeer;
import tech.pegasys.teku.networking.p2p.discovery.DiscoveryService;
import tech.pegasys.teku.networking.p2p.discovery.discv5.SecretKeyParser;
import tech.pegasys.teku.networking.p2p.libp2p.MultiaddrUtil;
import tech.pegasys.teku.networking.p2p.network.config.NetworkConfig;
import tech.pegasys.teku.service.serviceutils.Service;
import tech.pegasys.teku.storage.store.KeyValueStore;

/**
 * DiscV5Service is a discovery service that uses the DiscV5 protocol to discover and connect to
 * peers.
 *
 * @author grapebaba
 * @since 0.1.1
 */
public class DiscV5Service extends Service implements DiscoveryService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DiscV5Service.class);
    private static final String SEQ_NO_STORE_KEY = "local-enr-seqno";

    private static final String OP_STACK = "opstack";

    private static final Duration BOOTNODE_REFRESH_DELAY = Duration.ofMinutes(2);

    /**
     * Create default discovery system builder discovery system builder.
     *
     * @return the discovery system builder
     */
    public static DiscoverySystemBuilder createDefaultDiscoverySystemBuilder() {
        return new DiscoverySystemBuilder();
    }

    private final AsyncRunner asyncRunner;
    private final SecretKey localNodePrivateKey;

    private final NodeRecordConverter nodeRecordConverter;

    private final DiscoverySystem discoverySystem;
    private final KeyValueStore<String, Bytes> kvStore;
    private final List<NodeRecord> bootnodes;
    private volatile Cancellable bootnodeRefreshTask;

    private final UInt64 chainId;

    /**
     * Instantiates a new Disc v 5 service.
     *
     * @param metricsSystem          the metrics system
     * @param asyncRunner            the async runner
     * @param discoConfig            the disco config
     * @param p2pConfig              the p 2 p config
     * @param kvStore                the kv store
     * @param privateKey             the private key
     * @param discoverySystemBuilder the discovery system builder
     * @param chainId                the chain id
     * @param nodeRecordConverter    the node record converter
     */
    public DiscV5Service(
            final MetricsSystem metricsSystem,
            final AsyncRunner asyncRunner,
            final DiscoveryConfig discoConfig,
            final NetworkConfig p2pConfig,
            final KeyValueStore<String, Bytes> kvStore,
            final Bytes privateKey,
            final DiscoverySystemBuilder discoverySystemBuilder,
            final UInt64 chainId,
            final NodeRecordConverter nodeRecordConverter) {
        this.chainId = chainId;
        this.asyncRunner = asyncRunner;
        this.localNodePrivateKey = SecretKeyParser.fromLibP2pPrivKey(privateKey);
        this.nodeRecordConverter = nodeRecordConverter;
        final String listenAddress = p2pConfig.getNetworkInterface();
        final int listenUdpPort = discoConfig.getListenUdpPort();
        final String advertisedAddress = p2pConfig.getAdvertisedIp();
        final int advertisedTcpPort = p2pConfig.getAdvertisedPort();
        final int advertisedUdpPort = discoConfig.getAdvertisedUdpPort();
        final UInt64 seqNo = kvStore.get(SEQ_NO_STORE_KEY)
                .map(UInt64::fromBytes)
                .orElse(UInt64.ZERO)
                .add(1);
        final NewAddressHandler maybeUpdateNodeRecordHandler =
                maybeUpdateNodeRecord(p2pConfig.hasUserExplicitlySetAdvertisedIp(), advertisedTcpPort);
        this.bootnodes = discoConfig.getBootnodes().stream()
                .map(NodeRecordFactory.DEFAULT::fromEnr)
                .collect(toList());
        final OpStackEnrData opStackEnrData = new OpStackEnrData(chainId, UInt64.ZERO);
        final NodeRecordBuilder nodeRecordBuilder = new NodeRecordBuilder()
                .secretKey(localNodePrivateKey)
                .seq(seqNo)
                .customField(OP_STACK, opStackEnrData.encode());
        if (p2pConfig.hasUserExplicitlySetAdvertisedIp()) {
            nodeRecordBuilder.address(advertisedAddress, advertisedUdpPort, advertisedTcpPort);
        }
        final NodeRecord localNodeRecord = nodeRecordBuilder.build();
        this.discoverySystem = discoverySystemBuilder
                .listen(listenAddress, listenUdpPort)
                .secretKey(localNodePrivateKey)
                .bootnodes(bootnodes)
                .localNodeRecord(localNodeRecord)
                .newAddressHandler(maybeUpdateNodeRecordHandler)
                .localNodeRecordListener(this::localNodeRecordUpdated)
                .addressAccessPolicy(
                        discoConfig.areSiteLocalAddressesEnabled()
                                ? AddressAccessPolicy.ALLOW_ALL
                                : address -> !address.getAddress().isSiteLocalAddress())
                .build();
        this.kvStore = kvStore;
        metricsSystem.createIntegerGauge(
                TekuMetricCategory.DISCOVERY,
                "live_nodes_current",
                "Current number of live nodes tracked by the discovery system",
                () -> discoverySystem.getBucketStats().getTotalLiveNodeCount());
    }

    private NewAddressHandler maybeUpdateNodeRecord(
            boolean userExplicitlySetAdvertisedIpOrPort, final int advertisedTcpPort) {
        if (userExplicitlySetAdvertisedIpOrPort) {
            return (oldRecord, newAddress) -> Optional.of(oldRecord);
        } else {
            return (oldRecord, newAddress) -> Optional.of(
                    oldRecord.withNewAddress(newAddress, Optional.of(advertisedTcpPort), localNodePrivateKey));
        }
    }

    private void localNodeRecordUpdated(NodeRecord oldRecord, NodeRecord newRecord) {
        kvStore.put(SEQ_NO_STORE_KEY, newRecord.getSeq().toBytes());
    }

    @Override
    protected SafeFuture<?> doStart() {
        return SafeFuture.of(discoverySystem.start())
                .thenRun(() -> this.bootnodeRefreshTask = asyncRunner.runWithFixedDelay(
                        this::pingBootnodes,
                        BOOTNODE_REFRESH_DELAY,
                        error -> LOGGER.error("Failed to contact discovery bootnodes", error)));
    }

    private void pingBootnodes() {
        bootnodes.forEach(bootnode -> SafeFuture.of(discoverySystem.ping(bootnode))
                .finish(error -> LOGGER.debug("Bootnode {} is unresponsive", bootnode)));
    }

    @Override
    protected SafeFuture<?> doStop() {
        final Cancellable refreshTask = this.bootnodeRefreshTask;
        this.bootnodeRefreshTask = null;
        if (refreshTask != null) {
            refreshTask.cancel();
        }
        discoverySystem.stop();
        return SafeFuture.completedFuture(null);
    }

    @Override
    public Stream<DiscoveryPeer> streamKnownPeers() {
        return activeNodes().flatMap(node -> nodeRecordConverter.convertToDiscoveryPeer(node).stream());
    }

    @Override
    public SafeFuture<Collection<DiscoveryPeer>> searchForPeers() {
        return SafeFuture.of(discoverySystem.searchForNewPeers())
                .thenApply(this::filterByOpStackDataEnr)
                .thenApply(this::convertToDiscoveryPeers);
    }

    private List<NodeRecord> filterByOpStackDataEnr(final Collection<NodeRecord> nodeRecords) {
        return nodeRecords.stream()
                .filter(nodeRecord -> {
                    if (!nodeRecord.containsKey(OP_STACK)) {
                        return false;
                    }
                    OpStackEnrData enrData = OpStackEnrData.decode((Bytes) nodeRecord.get(OP_STACK));
                    return enrData.getChainId().equals(chainId)
                            && enrData.getVersion().isZero();
                })
                .collect(toList());
    }

    private List<DiscoveryPeer> convertToDiscoveryPeers(final Collection<NodeRecord> foundNodes) {
        LOGGER.debug("Found {} nodes prior to filtering", foundNodes.size());
        return foundNodes.stream()
                .flatMap(nodeRecord -> nodeRecordConverter.convertToDiscoveryPeer(nodeRecord).stream())
                .collect(toList());
    }

    @Override
    public Optional<String> getEnr() {
        return Optional.of(discoverySystem.getLocalNodeRecord().asEnr());
    }

    @Override
    public Optional<Bytes> getNodeId() {
        return Optional.of(discoverySystem.getLocalNodeRecord().getNodeId());
    }

    @Override
    public Optional<String> getDiscoveryAddress() {
        final NodeRecord nodeRecord = discoverySystem.getLocalNodeRecord();
        if (nodeRecord.getUdpAddress().isEmpty()) {
            return Optional.empty();
        }
        final DiscoveryPeer discoveryPeer = new DiscoveryPeer(
                (Bytes) nodeRecord.get(EnrField.PKEY_SECP256K1),
                nodeRecord.getUdpAddress().get(),
                Optional.empty(),
                null,
                null);

        return Optional.of(MultiaddrUtil.fromDiscoveryPeerAsUdp(discoveryPeer).toString());
    }

    @Override
    public void updateCustomENRField(String fieldName, Bytes value) {
        discoverySystem.updateCustomFieldValue(fieldName, value);
    }

    private Stream<NodeRecord> activeNodes() {
        return discoverySystem.streamLiveNodes();
    }
}
