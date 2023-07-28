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
    return nodeRecord
        .getTcpAddress()
        .map(address -> socketAddressToDiscoveryPeer(nodeRecord, address));
  }

  private static DiscoveryPeer socketAddressToDiscoveryPeer(
      final NodeRecord nodeRecord, final InetSocketAddress address) {

    return new DiscoveryPeer(
        ((Bytes) nodeRecord.get(EnrField.PKEY_SECP256K1)), address, Optional.empty(), null, null);
  }
}
