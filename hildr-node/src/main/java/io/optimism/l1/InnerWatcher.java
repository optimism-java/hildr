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

package io.optimism.l1;

import io.optimism.common.BlockInfo;
import io.optimism.config.Config;
import io.optimism.derive.stages.Attributes;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import okhttp3.OkHttpClient;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bytes;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint8;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.http.HttpService;
import org.web3j.tuples.generated.Tuple2;

/**
 * the InnerWatcher class.
 *
 * @author thinkAfCod
 * @since 0.1.0
 */
@SuppressWarnings("WaitNotInLoop")
public class InnerWatcher {

  private static final Logger LOGGER = LoggerFactory.getLogger(InnerWatcher.class);

  private static final String CONFIG_UPDATE_TOPIC =
      EventEncoder.encode(
          new Event(
              "ConfigUpdate",
              Arrays.asList(
                  new TypeReference<Uint256>() {},
                  new TypeReference<Uint8>() {},
                  new TypeReference<Bytes>() {})));

  private static final String TRANSACTION_DEPOSITED_TOPIC =
      EventEncoder.encode(
          new Event(
              "TransactionDeposited",
              Arrays.asList(
                  new TypeReference<Address>() {},
                  new TypeReference<Address>() {},
                  new TypeReference<Uint256>() {},
                  new TypeReference<Bytes>() {})));

  private final Object lock = new Object();
  private final ExecutorService executor;

  /** Global Config. */
  private Config config;

  /** Ethers http provider for L1. */
  private Web3j provider;

  /** Channel to send block updates. */
  private BlockingQueue<BlockUpdate> blockUpdateSender;

  /** Most recent ingested block. */
  BigInteger currentBlock;

  /** Most recent block. */
  private BigInteger headBlock;

  /** Most recent finalized block. */
  private BigInteger finalizedBlock;

  /** List of blocks that have not been finalized yet. */
  private List<BlockInfo> unfinalizedBlocks;

  /**
   * Mapping from block number to user deposits. Past block deposits are removed as they are no
   * longer needed.
   */
  private HashMap<BigInteger, List<Attributes.UserDeposited>> deposits;

  /** Current system config value. */
  private Config.SystemConfig systemConfig;

  /**
   * Next system config if it exists and the L1 block number it activates BigInteger,
   * Config.SystemConfig.
   */
  private volatile Tuple2<BigInteger, Config.SystemConfig> systemConfigUpdate;

  /**
   * create a InnerWatcher instance.
   *
   * @param config the global config
   * @param queue the Queue to send block updates
   * @param l1StartBlock the start block number of l1
   * @param l2StartBlock the start block number of l2
   * @param executor the executor for async request
   * @throws IOException thrown if failed to get block
   * @throws ExecutionException thrown if CompletableFuture execute failed
   * @throws InterruptedException thrown if thread be interrupted
   */
  public InnerWatcher(
      Config config,
      BlockingQueue<BlockUpdate> queue,
      BigInteger l1StartBlock,
      BigInteger l2StartBlock,
      ExecutorService executor)
      throws IOException, ExecutionException, InterruptedException {
    this.executor = executor;
    this.provider = createClient(config.l1RpcUrl());

    if (l2StartBlock.equals(config.chainConfig().l2Genesis().number())) {
      this.systemConfig = config.chainConfig().systemConfig();
    } else {
      Web3j l2Client = createClient(config.l2RpcUrl());
      CompletableFuture<EthBlock> l2Future =
          this.getBlock(l2Client, l2StartBlock.subtract(BigInteger.ONE));
      EthBlock block = l2Future.get();
      EthBlock.TransactionObject tx =
          (EthBlock.TransactionObject) block.getBlock().getTransactions().get(0).get();
      final byte[] input = Hex.decode(tx.getInput().substring(2));

      // 获取batch_sender
      final String batchSender = "0x" + Hex.toHexString(Arrays.copyOfRange(input, 176, 196));
      var l1FeeOverhead = new BigInteger(Arrays.copyOfRange(input, 196, 228));
      var l1FeeScalar = new BigInteger(Arrays.copyOfRange(input, 228, 260));
      var gasLimit = block.getBlock().getGasLimit();
      this.systemConfig =
          new Config.SystemConfig(batchSender, gasLimit, l1FeeOverhead, l1FeeScalar);
      l2Client.shutdown();
    }

    this.config = config;
    this.blockUpdateSender = queue;
    this.currentBlock = l1StartBlock;
    this.headBlock = BigInteger.ZERO;
    this.finalizedBlock = BigInteger.ZERO;
    this.unfinalizedBlocks = new ArrayList<>();
    this.deposits = new HashMap<>();
    this.systemConfigUpdate = new Tuple2<>(l1StartBlock, null);
  }

  /**
   * try ingest block.
   *
   * @throws IOException thrown if failed to get data on chain
   * @throws ExecutionException thrown if CompletableFuture execute failed
   * @throws InterruptedException thrown if thread be interrupted
   */
  public void tryIngestBlock() throws IOException, InterruptedException, ExecutionException {
    if (this.currentBlock.compareTo(this.finalizedBlock) > 0) {
      BigInteger finalizedBlock = this.getFinalized().get();
      this.finalizedBlock = finalizedBlock;
      this.blockUpdateSender.offer(new BlockUpdate.FinalityUpdate(finalizedBlock));
      this.unfinalizedBlocks =
          this.unfinalizedBlocks.stream()
              .filter(blockInfo -> blockInfo.number().compareTo(this.finalizedBlock) > 0)
              .collect(Collectors.toList());
    }
    if (this.currentBlock.compareTo(this.headBlock) > 0) {
      this.headBlock = this.getHead().get();
    }

    if (this.currentBlock.compareTo(this.headBlock) <= 0) {
      updateSystemConfigWithNewestLog();
    } else {
      lock.wait(250);
    }
  }

  private void updateSystemConfigWithNewestLog() {
    try {
      this.updateSystemConfig();

      EthBlock blockResp = this.getBlock(this.provider, this.currentBlock).get();
      List<Attributes.UserDeposited> userDeposits = this.getDeposits(this.currentBlock);
      boolean finalized = this.currentBlock.compareTo(this.finalizedBlock) >= 0;
      L1Info l1Info =
          L1Info.create(
              blockResp.getBlock(),
              userDeposits,
              config.chainConfig().batchInbox(),
              finalized,
              this.systemConfig);

      if (l1Info.blockInfo().number().compareTo(this.finalizedBlock) >= 0) {
        BlockInfo blockInfo =
            new BlockInfo(
                l1Info.blockInfo().hash(),
                l1Info.blockInfo().number(),
                blockResp.getBlock().getParentHash(),
                l1Info.blockInfo().timestamp());
        this.unfinalizedBlocks.add(blockInfo);
      }

      BlockUpdate update =
          this.checkReorg() ? new BlockUpdate.Reorg() : new BlockUpdate.NewBlock(l1Info);
      this.blockUpdateSender.offer(update);
      this.currentBlock = this.currentBlock.add(BigInteger.ONE);
    } catch (ExecutionException | InterruptedException e) {
      LOGGER.error("", e);
      throw new RuntimeException(e);
    }
  }

  private void updateSystemConfig() throws ExecutionException, InterruptedException {
    BigInteger preLastUpdateBlock = this.systemConfigUpdate.component1();
    if (preLastUpdateBlock.compareTo(this.currentBlock) < 0) {
      BigInteger toBlock = preLastUpdateBlock.add(BigInteger.valueOf(1000L));

      CompletableFuture<EthLog> logFuture =
          this.getLog(
              preLastUpdateBlock.add(BigInteger.ONE),
              toBlock,
              this.config.chainConfig().systemConfigContract(),
              CONFIG_UPDATE_TOPIC);
      EthLog updates = logFuture.get();
      EthLog.LogResult update = updates.getLogs().iterator().next();

      BigInteger updateBlock = ((EthLog.LogObject) update).getBlockNumber();
      SystemConfigUpdate configUpdate = SystemConfigUpdate.tryFrom((EthLog.LogObject) update);
      if (updateBlock == null) {
        this.systemConfigUpdate = new Tuple2<>(toBlock, null);
      } else {
        Config.SystemConfig updateSystemConfig = parseSystemConfigUpdate(configUpdate);
        this.systemConfigUpdate = new Tuple2<>(updateBlock, updateSystemConfig);
      }
    }
    BigInteger lastUpdateBlock = this.systemConfigUpdate.component1();
    Config.SystemConfig nextConfig = this.systemConfigUpdate.component2();
    if (lastUpdateBlock.compareTo(currentBlock) == 0 && nextConfig != null) {
      LOGGER.info("system config updated");
      LOGGER.debug("{}", nextConfig);
      this.systemConfig = nextConfig;
    }
  }

  private Config.SystemConfig parseSystemConfigUpdate(SystemConfigUpdate configUpdate) {
    Config.SystemConfig updateSystemConfig = null;
    if (configUpdate instanceof SystemConfigUpdate.BatchSender) {
      updateSystemConfig =
          new Config.SystemConfig(
              ((SystemConfigUpdate.BatchSender) configUpdate).getAddress(),
              this.systemConfig.gasLimit(),
              this.systemConfig.l1FeeOverhead(),
              this.systemConfig.l1FeeScalar());
    } else if (configUpdate instanceof SystemConfigUpdate.Fees) {
      updateSystemConfig =
          new Config.SystemConfig(
              this.systemConfig.batchSender(),
              this.systemConfig.gasLimit(),
              ((SystemConfigUpdate.Fees) configUpdate).getFeeOverhead(),
              ((SystemConfigUpdate.Fees) configUpdate).getFeeScalar());
    } else if (configUpdate instanceof SystemConfigUpdate.Gas) {
      updateSystemConfig =
          new Config.SystemConfig(
              this.systemConfig.batchSender(),
              ((SystemConfigUpdate.Gas) configUpdate).getGas(),
              this.systemConfig.l1FeeOverhead(),
              this.systemConfig.l1FeeScalar());
    }
    return updateSystemConfig;
  }

  private boolean checkReorg() {
    int size = this.unfinalizedBlocks.size();
    if (size >= 2) {
      BlockInfo last = this.unfinalizedBlocks.get(size - 1);
      BlockInfo parent = this.unfinalizedBlocks.get(size - 2);
      return !last.parentHash().equals(parent.hash());
    }
    return false;
  }

  private CompletableFuture<BigInteger> getFinalized() {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            EthBlock block =
                this.provider
                    .ethGetBlockByNumber(DefaultBlockParameterName.FINALIZED, false)
                    .send();
            return block.getBlock().getNumber();
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        },
        this.executor);
  }

  private CompletableFuture<BigInteger> getHead() {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            EthBlock block =
                this.provider.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false).send();
            return block.getBlock().getNumber();
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        },
        this.executor);
  }

  private CompletableFuture<EthBlock> getBlock(final Web3j client, final BigInteger blockNum) {
    return CompletableFuture.supplyAsync(
        () -> {
          try {
            return client.ethGetBlockByNumber(DefaultBlockParameter.valueOf(blockNum), true).send();
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        },
        this.executor);
  }

  private CompletableFuture<EthLog> getLog(
      BigInteger fromBlock, BigInteger toBlock, String contract, String topic) {
    final EthFilter ethFilter =
        new EthFilter(
                DefaultBlockParameter.valueOf(fromBlock),
                DefaultBlockParameter.valueOf(toBlock),
                contract)
            .addSingleTopic(topic);

    return CompletableFuture.supplyAsync(
        () -> {
          try {
            return this.provider.ethGetLogs(ethFilter).send();
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        });
  }

  private List<Attributes.UserDeposited> getDeposits(BigInteger blockNum)
      throws ExecutionException, InterruptedException {
    final List<Attributes.UserDeposited> removed = this.deposits.remove(blockNum);
    if (removed != null) {
      return removed;
    }
    final BigInteger endBlock = this.headBlock.min(blockNum.add(BigInteger.valueOf(1000L)));

    final CompletableFuture<EthLog> logFuture =
        this.getLog(
            blockNum,
            endBlock,
            this.config.chainConfig().depositContract(),
            TRANSACTION_DEPOSITED_TOPIC);
    final EthLog result = logFuture.get();
    List<Attributes.UserDeposited> depositLogs =
        result.getLogs().stream()
            .map(
                log -> {
                  if (log instanceof EthLog.LogObject) {
                    return Attributes.UserDeposited.fromLog((EthLog.LogObject) log);
                  } else {
                    throw new IllegalStateException(
                        "Unexpected result type: " + log.get() + " required LogObject");
                  }
                })
            .collect(Collectors.toList());

    for (BigInteger i = blockNum; i.compareTo(endBlock) < 0; i = i.add(BigInteger.ONE)) {
      final BigInteger num = i;
      final List<Attributes.UserDeposited> collect =
          depositLogs.stream()
              .filter(log -> log.l1BlockNum().compareTo(num) == 0)
              .collect(Collectors.toList());
      this.deposits.put(num, collect);
    }
    return this.deposits.remove(blockNum);
  }

  private Web3j createClient(String url) {
    OkHttpClient okHttpClient =
        new OkHttpClient.Builder().addInterceptor(new RetryRateLimitInterceptor()).build();
    return Web3j.build(new HttpService(url, okHttpClient));
  }
}
