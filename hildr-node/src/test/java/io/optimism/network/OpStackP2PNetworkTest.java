package io.optimism.network;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.optimism.config.Config;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;
import tech.pegasys.teku.networking.p2p.discovery.DiscoveryPeer;

@SuppressWarnings("checkstyle:AbbreviationAsWordInName")
class OpStackP2PNetworkTest {

  @Test
  void start() throws InterruptedException, ExecutionException {
    OpStackP2PNetwork opStackP2PNetwork =
        new OpStackP2PNetwork(Config.ChainConfig.optimismGoerli());
    opStackP2PNetwork.start();

    int sum = 0;
    for (var i = 0; i < 5; i++) {
      List<DiscoveryPeer> peers = opStackP2PNetwork.searchPeers();
      peers.forEach(peer -> System.out.println(peer.getNodeAddress()));
      sum += peers.size();
      sleep(1000);
    }
    assertTrue(sum > 0);
    opStackP2PNetwork.stop();
  }
}
