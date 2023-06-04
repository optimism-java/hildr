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

package io.optimism.runner;

import com.google.common.util.concurrent.AbstractExecutionThreadService;
import io.optimism.config.Config;
import io.optimism.config.Config.SyncMode;
import io.optimism.config.Config.SystemAccounts;
import io.optimism.driver.Driver;
import io.optimism.engine.EngineApi;
import io.optimism.engine.ExecutionPayload;
import io.optimism.engine.ExecutionPayload.Status;
import io.optimism.engine.ForkChoiceUpdate.ForkchoiceState;
import io.optimism.engine.OpEthForkChoiceUpdate;
import io.optimism.engine.OpEthPayloadStatus;
import java.math.BigInteger;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import jdk.incubator.concurrent.StructuredTaskScope;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.BooleanResponse;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlock.TransactionObject;
import org.web3j.protocol.http.HttpService;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.utils.Numeric;

/** The type Runner. */
public class Runner extends AbstractExecutionThreadService {

  private static final String TRUSTED_PEER_ENODE =
      "enode://e85ba0beec172b17f53b373b0ab72238754259aa39f1ae5290e3244e0120882f4cf95acd203661a27"
          + "c8618b27ca014d4e193266cb3feae43655ed55358eedb06@3.86.143.120:30303?discport=21693";
  private Config config;

  private SyncMode syncMode;

  private String checkpointHash;

  /**
   * Instantiates a new Runner.
   *
   * @param config the config
   * @param syncMode the sync mode
   * @param checkpointHash the checkpoint hash
   */
  public Runner(Config config, SyncMode syncMode, String checkpointHash) {
    this.config = config;
    this.syncMode = syncMode;
    this.checkpointHash = checkpointHash;
  }

  /**
   * Sets sync mode.
   *
   * @param syncMode the sync mode
   * @return the sync mode
   */
  public Runner setSyncMode(SyncMode syncMode) {
    this.syncMode = syncMode;
    return this;
  }

  /**
   * Sets checkpoint hash.
   *
   * @param checkpointHash the checkpoint hash
   * @return the checkpoint hash
   */
  public Runner setCheckpointHash(String checkpointHash) {
    this.checkpointHash = checkpointHash;
    return this;
  }

  /** Fast sync. */
  public void fastSync() {
    throw new UnsupportedOperationException("fast sync is not implemented yet");
  }

  /** Challenge sync. */
  public void challengeSync() {
    throw new UnsupportedOperationException("challenge sync is not implemented yet");
  }

  /**
   * Full sync.
   *
   * @throws ExecutionException the execution exception
   * @throws InterruptedException the interrupted exception
   */
  public void fullSync() throws ExecutionException, InterruptedException {
    waitDriverRunning();
  }

  private void waitDriverRunning() throws InterruptedException, ExecutionException {
    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
      var voidFuture =
          scope.fork(
              (Callable<Void>)
                  () -> {
                    Runner.this.startDriver();
                    return null;
                  });
      scope.join();
      scope.throwIfFailed();
      voidFuture.resultNow();
    }
  }

  /**
   * Checkpoint sync.
   *
   * @throws ExecutionException the execution exception
   * @throws InterruptedException the interrupted exception
   */
  public void checkpointSync() throws ExecutionException, InterruptedException {
    Web3j l2Provider = Web3j.build(new HttpService(this.config.l2RpcUrl()));
    if (StringUtils.isEmpty(this.config.checkpointSyncUrl())) {
      throw new RuntimeException("a checkpoint sync rpc url is required for checkpoint sync");
    }
    Web3j checkpointSyncUrl = Web3j.build(new HttpService(this.config.checkpointSyncUrl()));

    EthBlock checkpointBlock = null;
    if (StringUtils.isNotEmpty(this.checkpointHash)) {
      Tuple2<Boolean, EthBlock> isEpochBoundary =
          isEpochBoundary(this.checkpointHash, checkpointSyncUrl);
      if (isEpochBoundary.component1()) {
        checkpointBlock = isEpochBoundary.component2();
      } else {
        this.stopAsync().awaitTerminated();
      }
    } else {
      BigInteger blockNumber;
      try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
        Future<BigInteger> blockNumberFuture =
            scope.fork(() -> checkpointSyncUrl.ethBlockNumber().send().getBlockNumber());
        scope.join();
        scope.throwIfFailed();
        blockNumber = blockNumberFuture.get();
      }

      while (true) {
        Tuple2<Boolean, EthBlock> isEpochBoundary =
            isEpochBoundary(DefaultBlockParameter.valueOf(blockNumber), checkpointSyncUrl);
        if (isEpochBoundary.component1()) {
          checkpointBlock = isEpochBoundary.component2();
          break;
        } else {
          blockNumber = blockNumber.subtract(BigInteger.ONE);
        }
        if (!isRunning()) {
          break;
        }
      }
    }

    if (checkpointBlock == null) {
      throw new RuntimeException("could not find checkpoint block");
    }
    String checkpointHash = checkpointBlock.getBlock().getHash();
    EngineApi engineApi = new EngineApi(this.config.l2EngineUrl(), this.config.jwtSecret());
    while (isRunning()) {
      boolean isAvailable;
      try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
        Future<Boolean> isAvailableFuture = scope.fork(engineApi::isAvailable);

        scope.join();
        scope.throwIfFailed();
        isAvailable = isAvailableFuture.resultNow();
      }
      if (isAvailable) {
        break;
      } else {
        Thread.sleep(Duration.ofSeconds(3L));
      }
    }

    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
      Future<EthBlock> l2CheckpointBlockFuture =
          scope.fork(() -> l2Provider.ethGetBlockByHash(checkpointHash, true).send());

      scope.join();
      scope.throwIfFailed();
      EthBlock l2CheckpointBlock = l2CheckpointBlockFuture.resultNow();
      if (l2CheckpointBlock != null) {
        this.startDriver();
        return;
      }
    }

    // this is a temporary fix to allow execution layer peering to work
    // TODO: use a list of whitelisted bootnodes instead
    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
      Future<BooleanResponse> isPeerAddedFuture =
          scope.fork(() -> l2Provider.adminAddPeer(TRUSTED_PEER_ENODE).send());
      scope.join();
      scope.throwIfFailed();
      BooleanResponse isPeerAdded = isPeerAddedFuture.resultNow();
      if (!isPeerAdded.success()) {
        throw new RuntimeException("could not add peer");
      }
    }

    ExecutionPayload checkpointPayload = ExecutionPayload.from(checkpointBlock.getBlock());
    ForkchoiceState forkchoiceState = ForkchoiceState.fromSingleHead(checkpointHash);
    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
      Future<OpEthPayloadStatus> payloadStatusFuture =
          scope.fork(() -> engineApi.newPayload(checkpointPayload));

      Future<OpEthForkChoiceUpdate> forkChoiceUpdateFuture =
          scope.fork(() -> engineApi.forkchoiceUpdated(forkchoiceState, null));

      scope.join();
      scope.throwIfFailed();
      OpEthPayloadStatus payloadStatus = payloadStatusFuture.resultNow();
      OpEthForkChoiceUpdate forkChoiceUpdate = forkChoiceUpdateFuture.resultNow();

      if (payloadStatus.getPayloadStatus().getStatus() == Status.Invalid
          || payloadStatus.getPayloadStatus().getStatus() == Status.InvalidBlockHash) {
        throw new RuntimeException("the provided checkpoint payload is invalid");
      }

      if (forkChoiceUpdate.getForkChoiceUpdate().payloadStatus().getStatus() == Status.Invalid
          || forkChoiceUpdate.getForkChoiceUpdate().payloadStatus().getStatus()
              == Status.InvalidBlockHash) {
        throw new RuntimeException("could not accept forkchoice, exiting");
      }
    }

    while (isRunning()) {
      BigInteger blockNumber;
      try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
        Future<BigInteger> blockNumberFuture =
            scope.fork(() -> l2Provider.ethBlockNumber().send().getBlockNumber());

        scope.join();
        scope.throwIfFailed();
        blockNumber = blockNumberFuture.resultNow();
      }
      if (blockNumber.compareTo(checkpointPayload.blockNumber()) >= 0) {
        break;
      } else {
        Thread.sleep(Duration.ofSeconds(3L));
      }
    }

    waitDriverRunning();
  }

  private void startDriver() throws ExecutionException, InterruptedException {
    Driver<EngineApi> driver = Driver.from(this.config);
    driver.startAsync().awaitRunning();
  }

  private Tuple2<Boolean, EthBlock> isEpochBoundary(String blockHash, Web3j checkpointSyncUrl)
      throws InterruptedException, ExecutionException {
    EthBlock block;
    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
      Future<EthBlock> blockFuture =
          scope.fork(() -> checkpointSyncUrl.ethGetBlockByHash(blockHash, true).send());
      scope.join();
      scope.throwIfFailed();
      block = blockFuture.get();
      if (block == null) {
        throw new RuntimeException("could not find block from checkpoint sync url");
      }
    }

    return isBlockBoundary(block);
  }

  private Tuple2<Boolean, EthBlock> isEpochBoundary(
      DefaultBlockParameter blockParameter, Web3j checkpointSyncUrl)
      throws InterruptedException, ExecutionException {
    EthBlock block;
    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
      Future<EthBlock> blockFuture =
          scope.fork(() -> checkpointSyncUrl.ethGetBlockByNumber(blockParameter, true).send());
      scope.join();
      scope.throwIfFailed();
      block = blockFuture.get();
      if (block == null) {
        throw new RuntimeException("could not find block from checkpoint sync url");
      }
    }

    return isBlockBoundary(block);
  }

  @NotNull private Tuple2<Boolean, EthBlock> isBlockBoundary(EthBlock block) {
    String txInput =
        ((TransactionObject)
                block.getBlock().getTransactions().stream()
                    .filter(
                        transactionResult ->
                            ((TransactionObject) transactionResult)
                                .getTo()
                                .equalsIgnoreCase(
                                    SystemAccounts.defaultSystemAccounts().attributesPreDeploy()))
                    .findFirst()
                    .orElseThrow(
                        () ->
                            new RuntimeException(
                                "could not find setL1BlockValues tx in the epoch boundary"
                                    + " search")))
            .getInput();
    byte[] sequenceNumber = ArrayUtils.subarray(Numeric.hexStringToByteArray(txInput), 132, 164);
    if (Arrays.equals(sequenceNumber, new byte[32])) {
      return new Tuple2<>(true, block);
    } else {
      return new Tuple2<>(false, null);
    }
  }

  /**
   * Create runner.
   *
   * @param config the config
   * @return the runner
   */
  public static Runner create(Config config) {
    return new Runner(config, SyncMode.Full, null);
  }

  @Override
  protected void run() throws Exception {
    switch (this.syncMode) {
      case Fast -> this.fastSync();
      case Challenge -> this.challengeSync();
      case Full -> this.fullSync();
      case Checkpoint -> this.checkpointSync();
      default -> throw new RuntimeException("unknown sync mode");
    }
  }
}
