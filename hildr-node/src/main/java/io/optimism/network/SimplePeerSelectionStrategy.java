package io.optimism.network;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static tech.pegasys.teku.networking.p2p.connection.PeerConnectionType.STATIC;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import tech.pegasys.teku.networking.p2p.connection.PeerPools;
import tech.pegasys.teku.networking.p2p.connection.PeerSelectionStrategy;
import tech.pegasys.teku.networking.p2p.connection.TargetPeerRange;
import tech.pegasys.teku.networking.p2p.discovery.DiscoveryPeer;
import tech.pegasys.teku.networking.p2p.network.P2PNetwork;
import tech.pegasys.teku.networking.p2p.network.PeerAddress;
import tech.pegasys.teku.networking.p2p.peer.Peer;

/**
 * The type SimplePeerSelectionStrategy.
 *
 * @author grapebaba
 * @since 0.1.1
 */
public class SimplePeerSelectionStrategy implements PeerSelectionStrategy {
    private final TargetPeerRange targetPeerRange;

    /**
     * Instantiates a new Simple peer selection strategy.
     *
     * @param targetPeerRange the target peer range
     */
    public SimplePeerSelectionStrategy(final TargetPeerRange targetPeerRange) {
        this.targetPeerRange = targetPeerRange;
    }

    @Override
    public List<PeerAddress> selectPeersToConnect(
            final P2PNetwork<?> network,
            final PeerPools peerPools,
            final Supplier<? extends Collection<DiscoveryPeer>> candidates) {
        final int peersToAdd = targetPeerRange.getPeersToAdd(network.getPeerCount());
        if (peersToAdd == 0) {
            return emptyList();
        }
        return candidates.get().stream()
                .map(network::createPeerAddress)
                .limit(peersToAdd)
                .collect(toList());
    }

    @Override
    public List<Peer> selectPeersToDisconnect(final P2PNetwork<?> network, final PeerPools peerPools) {
        final int peersToDrop = targetPeerRange.getPeersToDrop(network.getPeerCount());
        return network.streamPeers()
                .filter(peer -> peerPools.getPeerConnectionType(peer.getId()) != STATIC)
                .limit(peersToDrop)
                .collect(toList());
    }
}
