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

package io.optimism.network;

import static org.web3j.crypto.Sign.recoverFromSignature;
import static org.web3j.utils.Assertions.verifyPrecondition;

import io.libp2p.core.pubsub.ValidationResult;
import io.optimism.derive.stages.BatcherTransactions;
import io.optimism.engine.ExecutionPayload;
import java.math.BigInteger;
import java.security.SignatureException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.RejectedExecutionException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.ssz.SSZ;
import org.apache.tuweni.units.bigints.UInt256;
import org.apache.tuweni.units.bigints.UInt64;
import org.bouncycastle.util.Arrays;
import org.jctools.queues.MessagePassingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.ECDSASignature;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;
import tech.pegasys.teku.infrastructure.async.AsyncRunner;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.networking.p2p.gossip.PreparedGossipMessage;
import tech.pegasys.teku.networking.p2p.gossip.PreparedGossipMessage.GossipDecodingException;
import tech.pegasys.teku.networking.p2p.gossip.PreparedGossipMessageFactory;
import tech.pegasys.teku.networking.p2p.gossip.TopicHandler;
import tech.pegasys.teku.networking.p2p.libp2p.config.LibP2PParamsFactory;
import tech.pegasys.teku.service.serviceutils.ServiceCapacityExceededException;
import tech.pegasys.teku.statetransition.validation.InternalValidationResult;

/**
 * The type BlockTopicHandler.
 *
 * @author grapebaba
 * @since 0.1.1
 */
@SuppressWarnings({
  "checkstyle:VariableDeclarationUsageDistance",
  "checkstyle:AbbreviationAsWordInName"
})
public class BlockTopicHandler implements TopicHandler {
  /** The constant EXECUTION_PAYLOAD_FIXED_PART. */
  // All fields (4s are offsets to dynamic data)
  private static final int EXECUTION_PAYLOAD_FIXED_PART =
      32 + 20 + 32 + 32 + 256 + 32 + 8 + 8 + 8 + 8 + 4 + 32 + 32 + 4;

  /** The constant MAX_TRANSACTIONS_PER_PAYLOAD. */
  // MAX_TRANSACTIONS_PER_PAYLOAD in consensus spec
  private static final int MAX_TRANSACTIONS_PER_PAYLOAD = 1 << 20;

  private static final Logger LOGGER = LoggerFactory.getLogger(BlockTopicHandler.class);
  private final PreparedGossipMessageFactory preparedGossipMessageFactory;

  private final String topic;

  private final AsyncRunner asyncRunner;

  private final UInt64 chainId;

  private final String unsafeBlockSigner;

  private final MessagePassingQueue<ExecutionPayload> unsafeBlockQueue;

  /**
   * Instantiates a new Block topic handler.
   *
   * @param preparedGossipMessageFactory the prepared gossip message factory
   * @param chainId the chain id
   * @param unsafeBlockSigner the unsafe block signer
   */
  public BlockTopicHandler(
      PreparedGossipMessageFactory preparedGossipMessageFactory,
      AsyncRunner asyncRunner,
      UInt64 chainId,
      String unsafeBlockSigner,
      MessagePassingQueue<ExecutionPayload> unsafeBlockQueue) {
    this.preparedGossipMessageFactory = preparedGossipMessageFactory;
    this.topic = String.format("/optimism/%s/0/blocks", chainId.toString());
    this.asyncRunner = asyncRunner;
    this.chainId = chainId;
    this.unsafeBlockSigner = unsafeBlockSigner;
    this.unsafeBlockQueue = unsafeBlockQueue;
  }

  @Override
  public PreparedGossipMessage prepareMessage(Bytes payload) {
    return preparedGossipMessageFactory.create(topic, payload, null);
  }

  @Override
  public SafeFuture<ValidationResult> handleMessage(PreparedGossipMessage message) {
    return SafeFuture.of(() -> deserialize(message))
        .thenCompose(
            deserialized ->
                asyncRunner.runAsync(
                    () ->
                        checkBlock(deserialized)
                            .thenApply(
                                internalValidation -> {
                                  processMessage(internalValidation, message);
                                  return fromInternalValidationResult(internalValidation);
                                })))
        .exceptionally(error -> handleMessageProcessingError(message, error));
  }

  public String getTopic() {
    return topic;
  }

  @Override
  public int getMaxMessageSize() {
    return LibP2PParamsFactory.MAX_COMPRESSED_GOSSIP_SIZE;
  }

  /** The type Block message. */
  public record BlockMessage(
      ExecutionPayload payload, Sign.SignatureData signature, byte[] payloadHash) {

    /**
     * From block message.
     *
     * @param data the data
     * @return the block message
     */
    public static BlockMessage from(byte[] data) {
      Sign.SignatureData signature =
          new Sign.SignatureData(
              data[64], Bytes.wrap(data, 0, 32).toArray(), Bytes.wrap(data, 32, 32).toArray());
      Bytes payload = Bytes.wrap(ArrayUtils.subarray(data, 65, data.length));
      ExecutionPayloadSSZ executionPayloadSSZ = ExecutionPayloadSSZ.from(payload);
      ExecutionPayload executionPayload = ExecutionPayload.from(executionPayloadSSZ);
      byte[] payloadHash = Hash.sha3(payload.toArray());

      return new BlockMessage(executionPayload, signature, payloadHash);
    }
  }

  /**
   * The type Execution payload.
   *
   * @param parentHash the parent hash
   * @param feeRecipient the fee recipient
   * @param stateRoot the state root
   * @param receiptsRoot the receipts root
   * @param logsBloom the logs bloom
   * @param prevRandao the prev randao
   * @param blockNumber the block number
   * @param gasLimit the gas limit
   * @param gasUsed the gas used
   * @param timestamp the timestamp
   * @param extraData the extra data
   * @param baseFeePerGas the base fee per gas
   * @param blockHash the block hash
   * @param transactions the transactions
   */
  public record ExecutionPayloadSSZ(
      Bytes parentHash,
      Bytes feeRecipient,
      Bytes stateRoot,
      Bytes receiptsRoot,
      Bytes logsBloom,
      Bytes prevRandao,
      long blockNumber,
      long gasLimit,
      long gasUsed,
      long timestamp,
      Bytes extraData,
      UInt256 baseFeePerGas,
      Bytes blockHash,
      List<Bytes> transactions) {

    /**
     * From execution payload ssz.
     *
     * @param data the data
     * @return the execution payload ssz
     */
    public static ExecutionPayloadSSZ from(Bytes data) {
      final int dataSize = data.size();
      if (dataSize < EXECUTION_PAYLOAD_FIXED_PART) {
        throw new IllegalArgumentException(
            String.format("scope too small to decode execution payload: %d", data.size()));
      }

      return SSZ.decode(
          data,
          sszReader -> {
            Bytes parentHash = sszReader.readHash(32);
            Bytes feeRecipient = sszReader.readAddress();
            Bytes stateRoot = sszReader.readFixedBytes(32);
            Bytes receiptsRoot = sszReader.readFixedBytes(32);
            Bytes logsBloom = sszReader.readFixedBytes(256);
            Bytes prevRandao = sszReader.readFixedBytes(32);
            long blockNumber = sszReader.readUInt64();
            long gasLimit = sszReader.readUInt64();
            long gasUsed = sszReader.readUInt64();
            long timestamp = sszReader.readUInt64();
            long extraDataOffset = sszReader.readUInt32();
            if (extraDataOffset != EXECUTION_PAYLOAD_FIXED_PART) {
              throw new IllegalArgumentException(
                  String.format(
                      "unexpected extra data offset: %d <> %d",
                      extraDataOffset, EXECUTION_PAYLOAD_FIXED_PART));
            }
            UInt256 baseFeePerGas = sszReader.readUInt256();
            Bytes blockHash = sszReader.readHash(32);
            long transactionsOffset = sszReader.readUInt32();

            if (transactionsOffset > extraDataOffset + 32 || transactionsOffset > dataSize) {
              throw new IllegalArgumentException(
                  String.format(
                      "extra-data is too large: %d", transactionsOffset - extraDataOffset));
            }

            Bytes extraData = Bytes.EMPTY;
            if (transactionsOffset != extraDataOffset) {
              extraData = sszReader.readFixedBytes((int) (transactionsOffset - extraDataOffset));
            }

            List<Bytes> transactions;
            if (sszReader.isComplete()) {
              transactions = List.of();
            } else {
              Bytes transactionsBytes = sszReader.consumeRemainingBytes(Integer.MAX_VALUE);
              transactions =
                  SSZ.decode(
                      transactionsBytes,
                      txsSSZReader -> {
                        int transactionsBytesSize = transactionsBytes.size();
                        if (transactionsBytesSize < 4) {
                          throw new IllegalArgumentException(
                              String.format(
                                  "not enough scope to read first tx offset: %d",
                                  transactionsBytesSize));
                        }

                        long firstTxOffset = txsSSZReader.readUInt32();
                        if (firstTxOffset % 4 != 0) {
                          throw new IllegalArgumentException(
                              String.format(
                                  "invalid first tx offset: %d, not a multiple of offset size",
                                  firstTxOffset));
                        }
                        if (firstTxOffset > transactionsBytesSize) {
                          throw new IllegalArgumentException(
                              String.format(
                                  "invalid first tx offset: %d, out of scope %d",
                                  firstTxOffset, transactionsBytesSize));
                        }
                        int txCount = (int) firstTxOffset / 4;
                        if (txCount > MAX_TRANSACTIONS_PER_PAYLOAD) {
                          throw new IllegalArgumentException(
                              String.format(
                                  "too many transactions: %d > %d",
                                  txCount, MAX_TRANSACTIONS_PER_PAYLOAD));
                        }
                        List<Long> nextOffsets = new ArrayList<>(txCount);
                        long currentTxOffset = firstTxOffset;
                        for (int i = 0; i < txCount; i++) {
                          long nextTxOffset = transactionsBytesSize;
                          if (i + 1 < txCount) {
                            nextTxOffset = txsSSZReader.readUInt32();
                          }
                          if (nextTxOffset < currentTxOffset
                              || nextTxOffset > transactionsBytesSize) {
                            throw new IllegalArgumentException(
                                String.format(
                                    "tx %d has bad next offset: %d, current is %d, scope is %d",
                                    i, nextTxOffset, currentTxOffset, transactionsBytesSize));
                          }
                          nextOffsets.add(nextTxOffset);
                          currentTxOffset = nextTxOffset;
                        }
                        List<Bytes> txs = new ArrayList<>(txCount);
                        long currentTxOffset1 = firstTxOffset;
                        for (int i = 0; i < txCount; i++) {
                          long nextTxOffset = nextOffsets.get(i);
                          int currentTxSize = (int) (nextTxOffset - currentTxOffset1);
                          Bytes transaction = txsSSZReader.readFixedBytes(currentTxSize);
                          txs.add(transaction);
                          currentTxOffset1 = nextTxOffset;
                        }

                        if (!txsSSZReader.isComplete()) {
                          throw new IllegalArgumentException("txsSSZReader is not complete");
                        }
                        return txs;
                      });
            }

            return new ExecutionPayloadSSZ(
                parentHash,
                feeRecipient,
                stateRoot,
                receiptsRoot,
                logsBloom,
                prevRandao,
                blockNumber,
                gasLimit,
                gasUsed,
                timestamp,
                extraData,
                baseFeePerGas,
                blockHash,
                transactions);
          });
    }
  }

  private CompletionStage<BlockMessage> deserialize(PreparedGossipMessage message)
      throws DecodingException {
    return SafeFuture.completedFuture(decode(message));
  }

  private BlockMessage decode(PreparedGossipMessage message) throws DecodingException {
    LOGGER.debug("Received gossip message {} on topic: {}", message, topic);
    try {
      return BlockMessage.from(
          message.getDecodedMessage().getDecodedMessageOrElseThrow().toArray());
    } catch (GossipDecodingException e) {
      LOGGER.error("Failed to decode gossip message", e);
      throw new DecodingException("Failed to decode gossip message", e);
    }
  }

  private SafeFuture<InternalValidationResult> checkBlock(BlockMessage blockMessage) {
    LOGGER.debug("Checking block {}", blockMessage);
    long now = Instant.now().getEpochSecond();
    if (blockMessage.payload.timestamp().longValue() > now + 5) {
      LOGGER.debug(
          "Block timestamp is too far in the future: {}, now: {}",
          blockMessage.payload.timestamp().longValue(),
          now);
      return SafeFuture.completedFuture(
          InternalValidationResult.reject(
              "Block timestamp is too far in the future: %d",
              blockMessage.payload.timestamp().longValue()));
    }

    if (blockMessage.payload.timestamp().longValue() < now - 60) {
      LOGGER.debug(
          "Block timestamp is too far in the past: {}, now: {}",
          blockMessage.payload.timestamp().longValue(),
          now);
      return SafeFuture.completedFuture(
          InternalValidationResult.reject(
              "Block timestamp is too far in the past: %d",
              blockMessage.payload.timestamp().longValue()));
    }

    byte[] msg = signatureMessage(chainId, blockMessage.payloadHash);

    try {
      BigInteger fromPub = signedMessageHashToKey(msg, blockMessage.signature());
      String from = Numeric.prependHexPrefix(Keys.getAddress(fromPub));

      if (!from.equalsIgnoreCase(this.unsafeBlockSigner)) {
        LOGGER.debug(
            "Block signature is invalid, from: {}, expected: {}", from, this.unsafeBlockSigner);
        return SafeFuture.completedFuture(
            InternalValidationResult.reject(
                "Block signature is invalid: %s", blockMessage.signature()));
      }

    } catch (SignatureException e) {
      return SafeFuture.completedFuture(
          InternalValidationResult.reject(
              "Block signature is invalid: %s", blockMessage.signature()));
    }

    this.unsafeBlockQueue.offer(blockMessage.payload);
    return SafeFuture.completedFuture(InternalValidationResult.ACCEPT);
  }

  private byte[] signatureMessage(UInt64 chainId, byte[] payloadHash) {
    Bytes32 domain = Bytes32.ZERO;
    byte[] chainIdBytes = Numeric.toBytesPadded(chainId.toBigInteger(), 32);
    return Hash.sha3(Arrays.concatenate(domain.toArray(), chainIdBytes, payloadHash));
  }

  private void processMessage(
      final InternalValidationResult internalValidationResult,
      final PreparedGossipMessage message) {
    switch (internalValidationResult.code()) {
      case REJECT -> LOGGER.warn(
          "Rejecting gossip message on topic {}, reason: {}, decoded message: {}",
          topic,
          internalValidationResult.getDescription(),
          message.getDecodedMessage().getDecodedMessage().orElseThrow());
      case IGNORE -> LOGGER.debug("Ignoring message for topic: {}", topic);
      case SAVE_FOR_FUTURE -> LOGGER.debug("Deferring message for topic: {}", topic);
      case ACCEPT -> LOGGER.debug("Accepting message for topic: {}", topic);
      default -> throw new UnsupportedOperationException(
          "Unexpected validation result: " + internalValidationResult);
    }
  }

  private ValidationResult handleMessageProcessingError(
      final PreparedGossipMessage message, final Throwable err) {
    final ValidationResult response;
    if (ExceptionUtils.hasCause(err, DecodingException.class)) {
      LOGGER.warn(
          "Failed to decode gossip message on topic {}, raw message: {}, error: {}",
          topic,
          message.getOriginalMessage(),
          err);
      response = ValidationResult.Invalid;
    } else if (ExceptionUtils.hasCause(err, RejectedExecutionException.class)) {
      LOGGER.warn(
          "Discarding gossip message for topic {} because the executor queue is full", topic);
      response = ValidationResult.Ignore;
    } else if (ExceptionUtils.hasCause(err, ServiceCapacityExceededException.class)) {
      LOGGER.warn(
          "Discarding gossip message for topic {} because the signature verification queue is full",
          topic);
      response = ValidationResult.Ignore;
    } else {
      LOGGER.warn(
          "Encountered exception while processing message for topic {}, error: {}", topic, err);
      response = ValidationResult.Invalid;
    }

    return response;
  }

  private static ValidationResult fromInternalValidationResult(InternalValidationResult result) {
    return switch (result.code()) {
      case ACCEPT -> ValidationResult.Valid;
      case SAVE_FOR_FUTURE, IGNORE -> ValidationResult.Ignore;
      case REJECT -> ValidationResult.Invalid;
    };
  }

  private static BigInteger signedMessageHashToKey(
      byte[] messageHash, Sign.SignatureData signatureData) throws SignatureException {

    byte[] r = signatureData.getR();
    byte[] s = signatureData.getS();
    verifyPrecondition(r != null && r.length == 32, "r must be 32 bytes");
    verifyPrecondition(s != null && s.length == 32, "s must be 32 bytes");

    ECDSASignature sig =
        new ECDSASignature(
            new BigInteger(1, signatureData.getR()), new BigInteger(1, signatureData.getS()));

    BigInteger key = recoverFromSignature(signatureData.getV()[0], sig, messageHash);
    if (key == null) {
      throw new SignatureException("Could not recover public key from signature");
    }
    return key;
  }
}
