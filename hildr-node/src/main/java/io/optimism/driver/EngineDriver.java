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
import io.optimism.engine.ExecutionPayload.Status;
import io.optimism.engine.ForkChoiceUpdate;
import io.optimism.engine.ForkChoiceUpdate.ForkchoiceState;
import io.optimism.engine.OpEthExecutionPayload;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.function.Function;
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
   * Handle attributes completable future.
   *
   * @param attributes the attributes
   * @return the completable future
   */
  public CompletableFuture<Void> handleAttributes(PayloadAttributes attributes) {
    return this.blockAt(attributes.timestamp())
        .thenCompose(
            block -> {
              if (block == null) {
                return processAttributes(attributes);
              } else {
                if (this.shouldSkip(block, attributes)) {
                  return skipAttributes(attributes, block);
                } else {
                  this.unsafeHead = this.safeHead;
                  return processAttributes(attributes);
                }
              }
            });
  }

  /**
   * Handle unsafe payload completable future.
   *
   * @param payload the payload
   * @return the completable future
   */
  public CompletableFuture<Void> handleUnsafePayload(ExecutionPayload payload) {
    this.unsafeHead = BlockInfo.from(payload);
    return CompletableFuture.allOf(this.pushPayload(payload), this.updateForkchoice());
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
   */
  public CompletableFuture<Boolean> engineReady() {
    ForkchoiceState forkchoiceState = createForkchoiceState();
    return this.engine
        .forkChoiceUpdate(forkchoiceState, null)
        .thenApply(Objects::nonNull)
        .exceptionally(throwable -> false);
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
  private CompletableFuture<EthBlock> blockAt(BigInteger timestamp) {
    BigInteger timeDiff = timestamp.subtract(this.finalizedHead.timestamp());
    BigInteger blocks = timeDiff.divide(this.blockTime);
    BigInteger blockNumber = this.finalizedHead.number().add(blocks);
    try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
      return CompletableFuture.supplyAsync(
          () -> {
            try {
              return web3j
                  .ethGetBlockByNumber(DefaultBlockParameter.valueOf(blockNumber), false)
                  .send();
            } catch (Exception e) {
              throw new RuntimeException(e);
            }
          },
          executor);
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

  private CompletableFuture<Void> updateForkchoice() {
    ForkchoiceState forkchoiceState = createForkchoiceState();

    return this.engine
        .forkChoiceUpdate(forkchoiceState, null)
        .thenAccept(
            forkChoiceUpdate -> {
              if (forkChoiceUpdate.getForkChoiceUpdate().payloadStatus().getStatus()
                  != Status.Valid) {
                throw new RuntimeException(
                    String.format(
                        "could not accept new forkchoice: %s",
                        forkChoiceUpdate
                            .getForkChoiceUpdate()
                            .payloadStatus()
                            .getValidationError()));
              }
            });
  }

  private CompletableFuture<Void> pushPayload(ExecutionPayload payload) {
    return this.engine
        .newPayload(payload)
        .thenAccept(
            payloadStatus -> {
              if (payloadStatus.getPayloadStatus().getStatus() != Status.Valid
                  && payloadStatus.getPayloadStatus().getStatus() != Status.Accepted) {
                throw new RuntimeException("invalid execution payload");
              }
            });
  }

  private CompletableFuture<OpEthExecutionPayload> buildPayload(PayloadAttributes attributes) {
    ForkchoiceState forkchoiceState = createForkchoiceState();

    return this.engine
        .forkChoiceUpdate(forkchoiceState, attributes)
        .thenCompose(
            opEthForkChoiceUpdate -> {
              ForkChoiceUpdate forkChoiceUpdate = opEthForkChoiceUpdate.getForkChoiceUpdate();
              if (forkChoiceUpdate.payloadStatus().getStatus() != Status.Valid) {
                throw new RuntimeException("invalid payload attributes");
              }
              if (forkChoiceUpdate.payloadId() == null) {
                throw new RuntimeException("invalid payload attributes");
              }
              return engine.getPayload(forkChoiceUpdate.payloadId());
            });
  }

  private CompletableFuture<Void> skipAttributes(PayloadAttributes attributes, EthBlock block) {
    Epoch newEpoch = attributes.epoch();
    BlockInfo newHead = BlockInfo.from(block.getBlock());
    this.updateSafeHead(newHead, newEpoch, false);
    return this.updateForkchoice();
  }

  private CompletableFuture<Void> processAttributes(PayloadAttributes attributes) {
    Epoch newEpoch = attributes.epoch();
    return this.buildPayload(attributes)
        .thenCompose(
            (Function<OpEthExecutionPayload, CompletableFuture<Void>>)
                opEthExecutionPayload -> {
                  ExecutionPayload executionPayload = opEthExecutionPayload.getExecutionPayload();
                  BlockInfo newHead =
                      new BlockInfo(
                          executionPayload.blockHash(),
                          executionPayload.blockNumber(),
                          executionPayload.parentHash(),
                          executionPayload.timestamp());

                  updateSafeHead(newHead, newEpoch, false);
                  return CompletableFuture.allOf(pushPayload(executionPayload), updateForkchoice());
                });
  }
}
