/*
 * Copyright 2023 q315xia@163.com
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

package io.optimism.batcher.loader;

import io.optimism.batcher.channel.ReorgException;
import io.optimism.batcher.exception.Web3jCallException;
import io.optimism.type.BlockId;
import io.optimism.type.Genesis;
import io.optimism.type.L1BlockInfo;
import io.optimism.type.L1BlockRef;
import io.optimism.type.L2BlockRef;
import io.optimism.type.OpEthSyncStatusRes;
import io.optimism.type.RollupConfigRes;
import io.optimism.utilities.rpc.Web3jProvider;
import java.io.Closeable;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import jdk.incubator.concurrent.StructuredTaskScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.utils.Numeric;

/**
 * BlockLoader class.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
@SuppressWarnings("UnusedVariable")
public class BlockLoader implements Closeable {

  private static final Logger LOGGER = LoggerFactory.getLogger(BlockLoader.class);

  private static final String DEPOSIT_TX_TYPE = "0x7E";

  private static final String OP_ROLLUP_CONFIG = "optimism_rollupConfig";

  private static final String OP_SYNC_STATUS = "optimism_syncStatus";

  private final Web3j l2Client;

  private final Web3j rollupClient;

  private final Web3jService rollupService;

  private final Consumer<EthBlock.Block> blockConsumer;

  private BlockId latestLoadedBlock;

  private RollupConfigRes.RollupConfig rollupConfig;

  /**
   * Constructor of BlockLoader.
   *
   * @param config LoaderConfig instance
   * @param blockConsumer consumer block loaded from L2
   */
  public BlockLoader(LoaderConfig config, Consumer<EthBlock.Block> blockConsumer) {
    this.l2Client = Web3jProvider.createClient(config.l2RpcUrl());
    Tuple2<Web3j, Web3jService> tuple = Web3jProvider.create(config.rollupUrl());
    this.rollupClient = tuple.component1();
    this.rollupService = tuple.component2();
    this.blockConsumer = blockConsumer;
    this.rollupConfig = loadRollConfig();
  }

  /**
   * Return rollup config object from rollup node.
   *
   * @return rollup config object
   */
  public RollupConfigRes.RollupConfig getRollConfig() {
    return this.rollupConfig;
  }

  /** Trigger load block from L2. */
  public void loadBlock() {
    this.loadBlocksIntoState();
  }

  @Override
  public void close() {
    this.l2Client.shutdown();
    this.rollupClient.shutdown();
  }

  private RollupConfigRes.RollupConfig loadRollConfig() {
    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
      var future =
          scope.fork(
              () ->
                  new Request<>(
                          OP_ROLLUP_CONFIG, List.of(), this.rollupService, RollupConfigRes.class)
                      .send()
                      .getConfig());
      scope.join();
      scope.throwIfFailed();
      return future.resultNow();
    } catch (ExecutionException | InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new Web3jCallException("failed to get op-rollup config", e);
    }
  }

  private void loadBlocksIntoState() {
    Tuple2<BlockId, BigInteger> blockNumbers = this.calculateL2BlockRangeToStore();
    final BigInteger start = blockNumbers.component1().number();
    final BigInteger end = blockNumbers.component2();
    var stopBlock = end.add(BigInteger.ONE);
    EthBlock.Block lastestBlock = null;
    for (BigInteger i = start.add(BigInteger.ONE);
        i.compareTo(stopBlock) < 0;
        i = i.add(BigInteger.ONE)) {
      EthBlock.Block block = this.loadBlockToChannel(i);
      this.latestLoadedBlock = BlockId.from(block);
      lastestBlock = block;
    }
    if (lastestBlock == null) {
      throw new BlockLoaderException("get latest block failed");
    }
    var ignore = l2BlockToBlockRef(lastestBlock, rollupConfig.genesis());
    // todo metrics.RecordL2BlocksLoaded l2Ref
  }

  private Tuple2<BlockId, BigInteger> calculateL2BlockRangeToStore() {
    final Request<?, OpEthSyncStatusRes> req =
        new Request<>(OP_SYNC_STATUS, List.of(), this.rollupService, OpEthSyncStatusRes.class);
    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
      var future = scope.fork(req::send);
      scope.join();
      scope.throwIfFailed();
      var syncStatus = future.resultNow().getOpEthSyncStatus();
      if (syncStatus.headL1().equals(L1BlockRef.emptyBlock)) {
        throw new SyncStatusException("empty sync status");
      }
      if (latestLoadedBlock == null || latestLoadedBlock.number().compareTo(BigInteger.ZERO) == 0) {
        LOGGER.info("Starting batch-submitter work at L2 safe-head: {}", syncStatus.safeL2());
        latestLoadedBlock = syncStatus.safeL2().toId();
      } else if (latestLoadedBlock.number().compareTo(syncStatus.safeL2().number()) <= 0) {
        LOGGER.warn(
            "last submitted block lagged behind L2 safe head: batch submission will continue");
        latestLoadedBlock = syncStatus.safeL2().toId();
      }

      if (syncStatus.safeL2().number().compareTo(syncStatus.unsafeL2().number()) >= 0
          || latestLoadedBlock.number().compareTo(syncStatus.unsafeL2().number()) >= 0) {
        throw new SyncStatusException("L2 safe head ahead of L2 unsafe head");
      }
      return new Tuple2<>(latestLoadedBlock, syncStatus.unsafeL2().number());
    } catch (ExecutionException | InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new Web3jCallException("failed to get syncStatus", e);
    }
  }

  private L2BlockRef l2BlockToBlockRef(final EthBlock.Block block, Genesis genesis) {
    BlockId l1Origin = null;
    BigInteger sequenceNumber = null;
    if (block.getNumber().compareTo(genesis.l2().number()) == 0) {
      if (!block.getHash().equals(genesis.l2().hash())) {
        throw new BlockLoaderException(
            String.format(
                "expected L2 genesis hash to match L2 block at genesis block number %d: %s <> %s",
                genesis.l2().number(), block.getHash(), genesis.l2().hash()));
      }
      l1Origin = genesis.l1();
      sequenceNumber = BigInteger.ZERO;
    } else {
      var txs = block.getTransactions();
      if (txs == null || txs.size() == 0) {
        throw new BlockLoaderException(
            String.format(
                "l2 block is missing L1 info deposit tx, block hash: %s", block.getHash()));
      }
      EthBlock.TransactionObject tx = (EthBlock.TransactionObject) txs.get(0).get();
      if (!DEPOSIT_TX_TYPE.equalsIgnoreCase(tx.getType())) {
        throw new BlockLoaderException(
            String.format("first payload tx has unexpected tx type: %s", tx.getType()));
      }
      final byte[] input = Numeric.hexStringToByteArray(tx.getInput());
      L1BlockInfo info = L1BlockInfo.from(input);
      l1Origin = info.toId();
      sequenceNumber = info.sequenceNumber();
    }
    return new L2BlockRef(
        block.getHash(),
        block.getNumber(),
        block.getParentHash(),
        block.getTimestamp(),
        l1Origin,
        sequenceNumber);
  }

  private EthBlock.Block loadBlockToChannel(BigInteger number) {
    final Request<?, EthBlock> ethBlockRequest =
        l2Client.ethGetBlockByNumber(DefaultBlockParameter.valueOf(number), true);
    EthBlock.Block block;
    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
      var blockFuture = scope.fork(ethBlockRequest::send);
      scope.join();
      scope.throwIfFailed();
      block = blockFuture.get().getBlock();
      if (block != null) {
        blockConsumer.accept(block);
      }
    } catch (ExecutionException | InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new SyncStatusException("failed to get block by number failed", e);
    } catch (ReorgException e) {
      this.latestLoadedBlock = null;
      throw e;
    }
    return block;
  }
}
