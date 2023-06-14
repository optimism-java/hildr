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

import com.google.common.util.concurrent.AbstractExecutionThreadService;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.optimism.common.BlockInfo;
import io.optimism.common.BlockNotIncludedException;
import io.optimism.common.HildrServiceExecutionException;
import io.optimism.config.Config;
import io.optimism.config.Config.SystemConfig;
import io.optimism.derive.stages.Attributes;
import io.optimism.derive.stages.Attributes.UserDeposited;
import io.optimism.driver.L1AttributesDepositedTxNotFoundException;
import io.optimism.l1.BlockUpdate.FinalityUpdate;
import io.optimism.rpc.provider.Web3jProvider;
import io.optimism.telemetry.Logging;
import java.math.BigInteger;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;
import org.jctools.queues.MessagePassingQueue;
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
import org.web3j.protocol.core.methods.response.EthLog.LogObject;
import org.web3j.protocol.core.methods.response.EthLog.LogResult;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.utils.Numeric;

/**
 * the InnerWatcher class.
 *
 * @author thinkAfCod
 * @since 0.1.0
 */
@SuppressWarnings("WaitNotInLoop")
public class InnerWatcher extends AbstractExecutionThreadService {

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

  private final ExecutorService executor;

  /** Global Config. */
  private final Config config;

  /** Ethers http provider for L1. */
  private final Web3j provider;

  /** Channel to send block updates. */
  private final MessagePassingQueue<BlockUpdate> blockUpdateQueue;

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
  private final HashMap<BigInteger, List<Attributes.UserDeposited>> deposits;

  /** Current system config value. */
  private Config.SystemConfig systemConfig;

  /**
   * Next system config if it exists and the L1 block number it activates BigInteger,
   * Config.SystemConfig.
   */
  private volatile Tuple2<BigInteger, Config.SystemConfig> systemConfigUpdate;

  private boolean isShutdownTriggered = false;

  /**
   * create a InnerWatcher instance.
   *
   * @param config the global config
   * @param queue the Queue to send block updates
   * @param l1StartBlock the start block number of l1
   * @param l2StartBlock the start block number of l2
   * @param executor the executor for async request
   */
  public InnerWatcher(
      Config config,
      MessagePassingQueue<BlockUpdate> queue,
      BigInteger l1StartBlock,
      BigInteger l2StartBlock,
      ExecutorService executor) {
    this.executor = executor;
    this.provider = Web3jProvider.createClient(config.l1RpcUrl());
    this.config = config;

    if (l2StartBlock.equals(config.chainConfig().l2Genesis().number())) {
      this.systemConfig = config.chainConfig().systemConfig();
    } else {
      this.getMetadataFromL2(l2StartBlock);
    }

    this.blockUpdateQueue = queue;
    this.currentBlock = l1StartBlock;
    this.headBlock = BigInteger.ZERO;
    this.finalizedBlock = BigInteger.ZERO;
    this.unfinalizedBlocks = new ArrayList<>();
    this.deposits = new HashMap<>();
    this.systemConfigUpdate = new Tuple2<>(l1StartBlock, null);
  }

  private void getMetadataFromL2(BigInteger l2StartBlock) {
    Web3j l2Client = Web3jProvider.createClient(config.l2RpcUrl());
    EthBlock block;
    try {
      block = this.getBlock(l2Client, l2StartBlock.subtract(BigInteger.ONE));
    } catch (InterruptedException | ExecutionException e) {
      if (e instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
      l2Client.shutdown();
      throw new HildrServiceExecutionException(e);
    }
    if (block.getBlock().getTransactions().isEmpty()) {
      throw new L1AttributesDepositedTxNotFoundException();
    }
    EthBlock.TransactionObject tx =
        (EthBlock.TransactionObject) block.getBlock().getTransactions().get(0).get();
    final byte[] input = Numeric.hexStringToByteArray(tx.getInput());

    final String batchSender = Numeric.toHexString(Arrays.copyOfRange(input, 176, 196));
    var l1FeeOverhead = Numeric.toBigInt(Arrays.copyOfRange(input, 196, 228));
    var l1FeeScalar = Numeric.toBigInt(Arrays.copyOfRange(input, 228, 260));
    var gasLimit = block.getBlock().getGasLimit();
    this.systemConfig =
        new Config.SystemConfig(
            batchSender,
            gasLimit,
            l1FeeOverhead,
            l1FeeScalar,
            config.chainConfig().systemConfig().unsafeBlockSigner());
    l2Client.shutdown();
  }

  /**
   * try ingest block.
   *
   * @throws ExecutionException thrown if failed to get data from web3j client
   * @throws InterruptedException thrown if executor has been shutdown
   */
  public void tryIngestBlock() throws ExecutionException, InterruptedException {
    if (this.currentBlock.compareTo(this.finalizedBlock) > 0) {
      this.finalizedBlock = this.getFinalized();
      this.putBlockUpdate(new FinalityUpdate(finalizedBlock));
      this.unfinalizedBlocks =
          this.unfinalizedBlocks.stream()
              .filter(
                  blockInfo -> blockInfo.number().compareTo(InnerWatcher.this.finalizedBlock) > 0)
              .collect(Collectors.toList());
    }

    if (this.currentBlock.compareTo(this.headBlock) > 0) {
      this.headBlock = this.getHead();
    }

    if (this.currentBlock.compareTo(this.headBlock) <= 0) {
      updateSystemConfigWithNewestLog();
    } else {
      try {
        Thread.sleep(Duration.ofMillis(250L));
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new HildrServiceExecutionException(e);
      }
    }
  }

  private void updateSystemConfigWithNewestLog() throws ExecutionException, InterruptedException {
    this.updateSystemConfig();
    EthBlock blockResp = this.getBlock(this.provider, this.currentBlock);
    List<UserDeposited> userDeposits = this.getDeposits(this.currentBlock);

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
    this.putBlockUpdate(update);
    this.currentBlock = this.currentBlock.add(BigInteger.ONE);
  }

  private void putBlockUpdate(final BlockUpdate update) {
    while (true) {
      boolean isOffered = this.blockUpdateQueue.offer(update);
      if (isOffered) {
        break;
      }
    }
  }

  private void updateSystemConfig() throws ExecutionException, InterruptedException {
    BigInteger preLastUpdateBlock = this.systemConfigUpdate.component1();
    if (preLastUpdateBlock.compareTo(this.currentBlock) < 0) {
      BigInteger toBlock = preLastUpdateBlock.add(BigInteger.valueOf(1000L));

      EthLog updates =
          this.getLog(
              preLastUpdateBlock.add(BigInteger.ONE),
              toBlock,
              InnerWatcher.this.config.chainConfig().systemConfigContract(),
              CONFIG_UPDATE_TOPIC);

      if (updates.getLogs().isEmpty()) {
        this.systemConfigUpdate = new Tuple2<>(toBlock, null);
      } else {
        LogResult<?> update = updates.getLogs().iterator().next();
        BigInteger updateBlock = ((LogObject) update).getBlockNumber();
        SystemConfigUpdate configUpdate = SystemConfigUpdate.tryFrom((LogObject) update);
        if (updateBlock == null) {
          this.systemConfigUpdate = new Tuple2<>(toBlock, null);
        } else {
          SystemConfig updateSystemConfig = parseSystemConfigUpdate(configUpdate);
          this.systemConfigUpdate = new Tuple2<>(updateBlock, updateSystemConfig);
        }
      }
    }
    BigInteger lastUpdateBlock = this.systemConfigUpdate.component1();
    SystemConfig nextConfig = this.systemConfigUpdate.component2();
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
              this.systemConfig.l1FeeScalar(),
              this.systemConfig.unsafeBlockSigner());
    } else if (configUpdate instanceof SystemConfigUpdate.Fees) {
      updateSystemConfig =
          new Config.SystemConfig(
              this.systemConfig.batchSender(),
              this.systemConfig.gasLimit(),
              ((SystemConfigUpdate.Fees) configUpdate).getFeeOverhead(),
              ((SystemConfigUpdate.Fees) configUpdate).getFeeScalar(),
              this.systemConfig.unsafeBlockSigner());
    } else if (configUpdate instanceof SystemConfigUpdate.Gas) {
      updateSystemConfig =
          new Config.SystemConfig(
              this.systemConfig.batchSender(),
              ((SystemConfigUpdate.Gas) configUpdate).getGas(),
              this.systemConfig.l1FeeOverhead(),
              this.systemConfig.l1FeeScalar(),
              this.systemConfig.unsafeBlockSigner());
    } else if (configUpdate instanceof SystemConfigUpdate.UnsafeBlockSigner) {
      updateSystemConfig =
          new Config.SystemConfig(
              this.systemConfig.batchSender(),
              this.systemConfig.gasLimit(),
              this.systemConfig.l1FeeOverhead(),
              this.systemConfig.l1FeeScalar(),
              ((SystemConfigUpdate.UnsafeBlockSigner) configUpdate).getAddress());
    }
    return updateSystemConfig;
  }

  private boolean checkReorg() {
    int size = this.unfinalizedBlocks.size();
    if (size >= 2) {
      BlockInfo last = this.unfinalizedBlocks.get(size - 1);
      BlockInfo parent = this.unfinalizedBlocks.get(size - 2);
      return !last.parentHash().equalsIgnoreCase(parent.hash());
    }
    return false;
  }

  private BigInteger getFinalized() throws ExecutionException, InterruptedException {
    EthBlock.Block finalizedBlock =
        this.executor
            .submit(
                () -> {
                  EthBlock block =
                      this.provider
                          .ethGetBlockByNumber(DefaultBlockParameterName.FINALIZED, false)
                          .send();
                  return block.getBlock();
                })
            .get();
    if (finalizedBlock == null) {
      throw new BlockNotIncludedException();
    }
    if (finalizedBlock.getNumber() == null) {
      throw new BlockNotIncludedException();
    }

    return finalizedBlock.getNumber();
  }

  private BigInteger getHead() throws ExecutionException, InterruptedException {
    EthBlock.Block headBlock =
        this.executor
            .submit(
                () -> {
                  EthBlock block =
                      this.provider
                          .ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false)
                          .send();
                  return block.getBlock();
                })
            .get();
    if (headBlock == null) {
      throw new BlockNotIncludedException();
    }
    if (headBlock.getNumber() == null) {
      throw new BlockNotIncludedException();
    }

    return headBlock.getNumber();
  }

  private EthBlock getBlock(final Web3j client, final BigInteger blockNum)
      throws ExecutionException, InterruptedException {
    EthBlock block =
        this.executor
            .submit(
                () ->
                    client
                        .ethGetBlockByNumber(DefaultBlockParameter.valueOf(blockNum), true)
                        .send())
            .get();
    if (block == null) {
      throw new BlockNotIncludedException();
    }
    return block;
  }

  private EthLog getLog(BigInteger fromBlock, BigInteger toBlock, String contract, String topic)
      throws ExecutionException, InterruptedException {
    final EthFilter ethFilter =
        new EthFilter(
                DefaultBlockParameter.valueOf(fromBlock),
                DefaultBlockParameter.valueOf(toBlock),
                contract)
            .addSingleTopic(topic);

    return this.executor.submit(() -> this.provider.ethGetLogs(ethFilter).send()).get();
  }

  private List<Attributes.UserDeposited> getDeposits(BigInteger blockNum)
      throws ExecutionException, InterruptedException {
    final List<Attributes.UserDeposited> removed = this.deposits.remove(blockNum);
    if (removed != null) {
      return removed;
    }
    final BigInteger endBlock = this.headBlock.min(blockNum.add(BigInteger.valueOf(1000L)));

    EthLog result =
        this.getLog(
            blockNum,
            endBlock,
            this.config.chainConfig().depositContract(),
            TRANSACTION_DEPOSITED_TOPIC);

    List<UserDeposited> depositLogs =
        result.getLogs().stream()
            .map(
                log -> {
                  if (log instanceof LogObject) {
                    return UserDeposited.fromLog((LogObject) log);
                  } else {
                    throw new IllegalStateException(
                        "Unexpected result type: " + log.get() + " required LogObject");
                  }
                })
            .collect(Collectors.toList());

    for (BigInteger i = blockNum; i.compareTo(endBlock) <= 0; i = i.add(BigInteger.ONE)) {
      final BigInteger num = i;
      final List<UserDeposited> collect =
          depositLogs.stream()
              .filter(log -> log.l1BlockNum().compareTo(num) == 0)
              .collect(Collectors.toList());
      InnerWatcher.this.deposits.put(num, collect);
    }
    var remv = InnerWatcher.this.deposits.remove(blockNum);
    if (remv == null) {
      throw new DepositsNotFoundException();
    }
    return remv;
  }

  @Override
  protected void run() {
    while (isRunning() && !this.isShutdownTriggered) {
      Tracer tracer = Logging.INSTANCE.getTracer("structure-task-scope");
      Span span = tracer.nextSpan().name("call").start();
      try (var unused = tracer.withSpan(span)) {
        LOGGER.debug("fetching L1 data for block {}", currentBlock);
        this.tryIngestBlock();
      } catch (ExecutionException e) {
        LOGGER.error(String.format("error while fetching L1 data for block %d", currentBlock), e);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new HildrServiceExecutionException(e);
      } finally {
        span.end();
      }
    }
  }

  @Override
  protected Executor executor() {
    return this.executor;
  }

  @Override
  protected void shutDown() {
    this.executor.shutdown();
    this.provider.shutdown();
  }

  @Override
  protected void triggerShutdown() {
    this.isShutdownTriggered = true;
  }
}
