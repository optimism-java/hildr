package io.optimism.network;

import java.net.InetSocketAddress;
import java.util.Optional;
import org.apache.tuweni.bytes.Bytes;
import org.ethereum.beacon.discovery.schema.EnrField;
import org.ethereum.beacon.discovery.schema.NodeRecord;
import tech.pegasys.teku.networking.p2p.discovery.DiscoveryPeer;

/**
 * The type NodeRecordConverter.
 *
 * @author grapebaba
 * @since 0.1.1
 */
public class NodeRecordConverter {

    /** Instantiates a new Node record converter. */
    public NodeRecordConverter() {}

    /**
     * Convert to discovery peer optional.
     *
     * @param nodeRecord the node record
     * @return the optional
     */
    public Optional<DiscoveryPeer> convertToDiscoveryPeer(final NodeRecord nodeRecord) {
        if (nodeRecord.getTcpAddress().isPresent()) {
            return Optional.of(socketAddressToDiscoveryPeer(
                    nodeRecord, nodeRecord.getTcpAddress().get()));
        } else if (nodeRecord.getUdpAddress().isPresent()) {
            return Optional.of(socketAddressToDiscoveryPeer(
                    nodeRecord, nodeRecord.getUdpAddress().get()));
        } else {
            return Optional.empty();
        }
    }

    private static DiscoveryPeer socketAddressToDiscoveryPeer(
            final NodeRecord nodeRecord, final InetSocketAddress address) {

        return new DiscoveryPeer(
                ((Bytes) nodeRecord.get(EnrField.PKEY_SECP256K1)), address, Optional.empty(), null, null);
    }
}
