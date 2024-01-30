package io.optimism.network;

import java.util.Optional;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.networking.p2p.connection.ConnectionManager;
import tech.pegasys.teku.networking.p2p.discovery.DiscoveryService;
import tech.pegasys.teku.networking.p2p.network.DelegatingP2PNetwork;
import tech.pegasys.teku.networking.p2p.network.P2PNetwork;
import tech.pegasys.teku.networking.p2p.peer.NodeId;
import tech.pegasys.teku.networking.p2p.peer.Peer;
import tech.pegasys.teku.networking.p2p.peer.PeerConnectedSubscriber;

/**
 * The type OpStackP2PNetwork.
 *
 * @param <P> the Peer type parameter
 * @author grapebaba
 * @since 0.1.1
 */
@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
public class OpStackP2PNetwork<P extends Peer> extends DelegatingP2PNetwork<P> {
    private static final Logger LOGGER = LoggerFactory.getLogger(OpStackP2PNetwork.class);

    private final P2PNetwork<P> p2pNetwork;
    private final DiscoveryService discoveryService;
    private final ConnectionManager connectionManager;

    /**
     * Instantiates a new OpStackP2PNetwork.
     *
     * @param p2pNetwork the p2pNetwork
     * @param discoveryService the discovery service
     * @param connectionManager the connection manager
     */
    protected OpStackP2PNetwork(
            final P2PNetwork<P> p2pNetwork,
            final DiscoveryService discoveryService,
            final ConnectionManager connectionManager) {
        super(p2pNetwork);
        this.p2pNetwork = p2pNetwork;
        this.discoveryService = discoveryService;
        this.connectionManager = connectionManager;
    }

    @Override
    public SafeFuture<?> start() {
        return SafeFuture.allOfFailFast(p2pNetwork.start(), discoveryService.start())
                .thenCompose(unused -> connectionManager.start())
                .thenRun(() -> getEnr().ifPresent(enr -> LOGGER.info("Local ENR: {}", enr)));
    }

    @Override
    public SafeFuture<?> stop() {
        return connectionManager.stop().handleComposed((unused, err) -> {
            if (err != null) {
                LOGGER.warn("Error shutting down connection manager", err);
            }
            return SafeFuture.allOf(p2pNetwork.stop(), discoveryService.stop());
        });
    }

    /**
     * Add static peer.
     *
     * @param peerAddress the peer address
     */
    public void addStaticPeer(final String peerAddress) {
        connectionManager.addStaticPeer(p2pNetwork.createPeerAddress(peerAddress));
    }

    @Override
    public Optional<String> getEnr() {
        return discoveryService.getEnr();
    }

    @Override
    public Optional<String> getDiscoveryAddress() {
        return discoveryService.getDiscoveryAddress();
    }

    @Override
    public long subscribeConnect(final PeerConnectedSubscriber<P> subscriber) {
        return p2pNetwork.subscribeConnect(subscriber);
    }

    @Override
    public void unsubscribeConnect(final long subscriptionId) {
        p2pNetwork.unsubscribeConnect(subscriptionId);
    }

    @Override
    public Optional<P> getPeer(final NodeId id) {
        return p2pNetwork.getPeer(id);
    }

    @Override
    public Stream<P> streamPeers() {
        return p2pNetwork.streamPeers();
    }

    /**
     * Gets discovery service.
     *
     * @return the discovery service
     */
    public DiscoveryService getDiscoveryService() {
        return discoveryService;
    }
}
