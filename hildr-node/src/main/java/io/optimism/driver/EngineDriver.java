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

package io.optimism.driver;

import io.optimism.common.BlockInfo;
import io.optimism.common.Epoch;
import io.optimism.config.Config;
import io.optimism.engine.Engine;
import io.optimism.engine.EngineApi;
import io.optimism.engine.ExecutionPayload;
import io.optimism.engine.ExecutionPayload.PayloadAttributes;
import io.optimism.engine.ExecutionPayload.PayloadStatus;
import io.optimism.engine.ExecutionPayload.Status;
import io.optimism.engine.ForkChoiceUpdate;
import io.optimism.engine.ForkChoiceUpdate.ForkchoiceState;
import io.optimism.engine.OpEthExecutionPayload;
import io.optimism.engine.OpEthForkChoiceUpdate;
import io.optimism.engine.OpEthPayloadStatus;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import jdk.incubator.concurrent.StructuredTaskScope;
import org.web3j.crypto.Hash;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlock.TransactionHash;

/**
 * The type EngineDriver.
 *
 * @param <E> the type parameter
 * @author grapebaba
 * @since 0.1.0
 */
public class EngineDriver<E extends Engine> {

  private E engine;

  private Web3j web3j;

  private BigInteger blockTime;

  private BlockInfo unsafeHead;

  private BlockInfo safeHead;

  private Epoch safeEpoch;

  private BlockInfo finalizedHead;

  private Epoch finalizedEpoch;

  /**
   * Instantiates a new Engine driver.
   *
   * @param finalizedHead the finalized head
   * @param finalizedEpoch the finalized epoch
   * @param web3j the web 3 j
   * @param config the config
   */
  @SuppressWarnings("unchecked")
  public EngineDriver(BlockInfo finalizedHead, Epoch finalizedEpoch, Web3j web3j, Config config) {
    this.engine = (E) new EngineApi(config.l2EngineUrl(), config.jwtSecret());
    this.unsafeHead = finalizedHead;
    this.finalizedHead = finalizedHead;
    this.finalizedEpoch = finalizedEpoch;
    this.safeHead = finalizedHead;
    this.safeEpoch = finalizedEpoch;
    this.web3j = web3j;
    this.blockTime = config.chainConfig().blockTime();
  }

  /**
   * Gets block time.
   *
   * @return the block time
   */
  public BigInteger getBlockTime() {
    return blockTime;
  }

  /**
   * Gets unsafe head.
   *
   * @return the unsafe head
   */
  public BlockInfo getUnsafeHead() {
    return unsafeHead;
  }

  /**
   * Gets safe head.
   *
   * @return the safe head
   */
  public BlockInfo getSafeHead() {
    return safeHead;
  }

  /**
   * Gets safe epoch.
   *
   * @return the safe epoch
   */
  public Epoch getSafeEpoch() {
    return safeEpoch;
  }

  /**
   * Gets finalized head.
   *
   * @return the finalized head
   */
  public BlockInfo getFinalizedHead() {
    return finalizedHead;
  }

  /**
   * Gets finalized epoch.
   *
   * @return the finalized epoch
   */
  public Epoch getFinalizedEpoch() {
    return finalizedEpoch;
  }

  /**
   * Handle attributes completable future.
   *
   * @param attributes the attributes
   * @throws ExecutionException the execution exception
   * @throws InterruptedException the interrupted exception
   */
  public void handleAttributes(PayloadAttributes attributes)
      throws ExecutionException, InterruptedException {
    EthBlock block = this.blockAt(attributes.timestamp());
    if (block == null) {
      processAttributes(attributes);
    } else {
      if (this.shouldSkip(block, attributes)) {
        skipAttributes(attributes, block);
      } else {
        this.unsafeHead = this.safeHead;
        processAttributes(attributes);
      }
    }
  }

  /**
   * Handle unsafe payload completable future.
   *
   * @param payload the payload
   * @throws ExecutionException the execution exception
   * @throws InterruptedException the interrupted exception
   */
  public void handleUnsafePayload(ExecutionPayload payload)
      throws ExecutionException, InterruptedException {
    this.unsafeHead = BlockInfo.from(payload);
    pushPayloadAndUpdateForkchoice(payload);
  }

  /**
   * Update finalized.
   *
   * @param head the head
   * @param epoch the epoch
   */
  public void updateFinalized(BlockInfo head, Epoch epoch) {
    this.finalizedHead = head;
    this.finalizedEpoch = epoch;
  }

  /** Reorg. */
  public void reorg() {
    this.unsafeHead = this.finalizedHead;
    this.safeHead = this.finalizedHead;
    this.safeEpoch = this.finalizedEpoch;
  }

  /**
   * Engine ready completable future.
   *
   * @return the completable future
   * @throws InterruptedException the interrupted exception
   */
  public boolean engineReady() throws InterruptedException {
    ForkchoiceState forkchoiceState = createForkchoiceState();

    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
      var unused =
          scope.fork(() -> EngineDriver.this.engine.forkChoiceUpdate(forkchoiceState, null));
      scope.join();
      return scope.exception().isEmpty();
    }
  }

  @SuppressWarnings("preview")
  private boolean shouldSkip(EthBlock block, PayloadAttributes attributes) {
    List<String> attributesHashes = attributes.transactions().stream().map(Hash::sha3).toList();

    return block.getBlock().getTransactions().stream()
            .map(transactionResult -> ((TransactionHash) transactionResult).get())
            .toList()
            .equals(attributesHashes)
        || attributes.timestamp().equals(block.getBlock().getTimestamp())
        || attributes.prevRandao().equals(block.getBlock().getMixHash())
        || attributes.suggestedFeeRecipient().equals(block.getBlock().getAuthor())
        || attributes.gasLimit().equals(block.getBlock().getGasLimit());
  }

  @SuppressWarnings("preview")
  private EthBlock blockAt(BigInteger timestamp) throws InterruptedException, ExecutionException {
    BigInteger timeDiff = timestamp.subtract(this.finalizedHead.timestamp());
    BigInteger blocks = timeDiff.divide(this.blockTime);
    BigInteger blockNumber = this.finalizedHead.number().add(blocks);

    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
      Future<EthBlock> ethBlockFuture =
          scope.fork(
              () ->
                  web3j
                      .ethGetBlockByNumber(DefaultBlockParameter.valueOf(blockNumber), false)
                      .send());

      scope.join();
      scope.throwIfFailed();

      return ethBlockFuture.resultNow();
    }
  }

  private ForkchoiceState createForkchoiceState() {
    return new ForkchoiceState(
        this.unsafeHead.hash(), this.safeHead.hash(), this.finalizedHead.hash());
  }

  private void updateSafeHead(BlockInfo newHead, Epoch newEpoch, boolean reorgUnsafe) {
    if (!this.safeHead.equals(newHead)) {
      this.safeHead = newHead;
      this.safeEpoch = newEpoch;
    }
    if (reorgUnsafe || this.safeHead.number().compareTo(this.unsafeHead.number()) > 0) {
      this.unsafeHead = newHead;
    }
  }

  private void updateForkchoice() throws InterruptedException, ExecutionException {
    ForkchoiceState forkchoiceState = createForkchoiceState();

    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
      Future<OpEthForkChoiceUpdate> forkChoiceUpdateFuture =
          scope.fork(() -> EngineDriver.this.engine.forkChoiceUpdate(forkchoiceState, null));

      scope.join();
      scope.throwIfFailed();
      ForkChoiceUpdate forkChoiceUpdate = forkChoiceUpdateFuture.resultNow().getForkChoiceUpdate();

      if (forkChoiceUpdate.payloadStatus().getStatus() != Status.Valid) {
        throw new RuntimeException(
            String.format(
                "could not accept new forkchoice: %s",
                forkChoiceUpdate.payloadStatus().getValidationError()));
      }
    }
  }

  private void pushPayload(ExecutionPayload payload)
      throws InterruptedException, ExecutionException {
    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
      Future<OpEthPayloadStatus> payloadStatusFuture =
          scope.fork(() -> EngineDriver.this.engine.newPayload(payload));

      scope.join();
      scope.throwIfFailed();
      PayloadStatus payloadStatus = payloadStatusFuture.resultNow().getPayloadStatus();

      if (payloadStatus.getStatus() != Status.Valid
          && payloadStatus.getStatus() != Status.Accepted) {
        throw new RuntimeException("invalid execution payload");
      }
    }
  }

  private void pushPayloadAndUpdateForkchoice(ExecutionPayload payload)
      throws InterruptedException, ExecutionException {
    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
      ForkchoiceState forkchoiceState = createForkchoiceState();
      Future<OpEthPayloadStatus> payloadStatusFuture =
          scope.fork(() -> EngineDriver.this.engine.newPayload(payload));

      Future<OpEthForkChoiceUpdate> forkChoiceUpdateFuture =
          scope.fork(() -> EngineDriver.this.engine.forkChoiceUpdate(forkchoiceState, null));

      scope.join();
      scope.throwIfFailed();
      PayloadStatus payloadStatus = payloadStatusFuture.resultNow().getPayloadStatus();
      ForkChoiceUpdate forkChoiceUpdate = forkChoiceUpdateFuture.resultNow().getForkChoiceUpdate();

      if (payloadStatus.getStatus() != Status.Valid
          && payloadStatus.getStatus() != Status.Accepted) {
        throw new RuntimeException("invalid execution payload");
      }

      if (forkChoiceUpdate.payloadStatus().getStatus() != Status.Valid) {
        throw new RuntimeException(
            String.format(
                "could not accept new forkchoice: %s",
                forkChoiceUpdate.payloadStatus().getValidationError()));
      }
    }
  }

  private OpEthExecutionPayload buildPayload(PayloadAttributes attributes)
      throws InterruptedException, ExecutionException {
    ForkchoiceState forkchoiceState = createForkchoiceState();

    ForkChoiceUpdate forkChoiceUpdate;
    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
      Future<OpEthForkChoiceUpdate> forkChoiceUpdateFuture =
          scope.fork(() -> EngineDriver.this.engine.forkChoiceUpdate(forkchoiceState, attributes));

      scope.join();
      scope.throwIfFailed();
      forkChoiceUpdate = forkChoiceUpdateFuture.resultNow().getForkChoiceUpdate();
      if (forkChoiceUpdate.payloadStatus().getStatus() != Status.Valid) {
        throw new RuntimeException("invalid payload attributes");
      }

      if (forkChoiceUpdate.payloadId() == null) {
        throw new RuntimeException("invalid payload attributes");
      }

      try (var scope1 = new StructuredTaskScope.ShutdownOnFailure()) {
        Future<OpEthExecutionPayload> payloadFuture =
            scope1.fork(() -> EngineDriver.this.engine.getPayload(forkChoiceUpdate.payloadId()));

        scope1.join();
        scope1.throwIfFailed();
        return payloadFuture.get();
      }
    }
  }

  private void skipAttributes(PayloadAttributes attributes, EthBlock block)
      throws ExecutionException, InterruptedException {
    Epoch newEpoch = attributes.epoch();
    BlockInfo newHead = BlockInfo.from(block.getBlock());
    this.updateSafeHead(newHead, newEpoch, false);
    this.updateForkchoice();
  }

  private void processAttributes(PayloadAttributes attributes)
      throws ExecutionException, InterruptedException {
    Epoch newEpoch = attributes.epoch();
    OpEthExecutionPayload opEthExecutionPayload = this.buildPayload(attributes);
    ExecutionPayload executionPayload = opEthExecutionPayload.getExecutionPayload();
    BlockInfo newHead =
        new BlockInfo(
            executionPayload.blockHash(),
            executionPayload.blockNumber(),
            executionPayload.parentHash(),
            executionPayload.timestamp());

    updateSafeHead(newHead, newEpoch, false);

    pushPayloadAndUpdateForkchoice(executionPayload);
  }
}
