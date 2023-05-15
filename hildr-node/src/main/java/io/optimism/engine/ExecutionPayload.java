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

package io.optimism.engine;

import io.optimism.common.Epoch;
import java.math.BigInteger;
import java.util.List;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlock.TransactionObject;

/**
 * The type ExecutionPayload.
 *
 * @param parentHash A 32 byte hash of the parent payload.
 * @param feeRecipient A 20 byte hash (aka Address) for the feeRecipient field of the new payload.
 * @param stateRoot A 32 byte state root hash.
 * @param receiptsRoot A 32 byte receipt root hash.
 * @param logsBloom A 32 byte logs bloom filter.
 * @param prevRandom A 32 byte beacon chain randomness value.
 * @param blockNumber A 64-bit number for the current block index.
 * @param gasLimit A 64-bit value for the gas limit.
 * @param gasUsed A 64-bit value for the gas used.
 * @param timestamp A 64-bit value for the timestamp field of the new payload.
 * @param extraData 0 to 32 byte value for extra data.
 * @param baseFeePerGas 256 bits for the base fee per gas.
 * @param blockHash The 32 byte block hash.
 * @param transactions An array of transaction objects where each object is a byte list.
 * @author grapebaba
 * @since 0.1.0
 */
public record ExecutionPayload(
    String parentHash,
    String feeRecipient,
    String stateRoot,
    String receiptsRoot,
    String logsBloom,
    String prevRandom,
    BigInteger blockNumber,
    BigInteger gasLimit,
    BigInteger gasUsed,
    BigInteger timestamp,
    String extraData,
    BigInteger baseFeePerGas,
    String blockHash,
    List<String> transactions) {

  /**
   * From execution payload.
   *
   * @param block the block
   * @return the execution payload
   */
  public static ExecutionPayload from(EthBlock.Block block) {
    List<String> encodedTxs =
        block.getTransactions().stream().map(tx -> ((TransactionObject) tx).getInput()).toList();

    return new ExecutionPayload(
        block.getParentHash(),
        block.getAuthor(),
        block.getStateRoot(),
        block.getReceiptsRoot(),
        block.getLogsBloom(),
        block.getMixHash(),
        block.getNumber(),
        block.getGasLimit(),
        block.getGasUsed(),
        block.getTimestamp(),
        block.getExtraData(),
        block.getBaseFeePerGas(),
        block.getHash(),
        encodedTxs);
  }

  /**
   * The type PayloadAttributes.
   *
   * <p>L2 extended payload attributes for Optimism. For more details, visit the [Optimism specs](<a
   * href="https://github.com/ethereum-optimism/optimism/blob/develop/specs/exec-engine.md#extended-payloadattributesv1">...</a>).
   *
   * @param timestamp 64 bit value for the timestamp field of the new payload.
   * @param prevRandao 32 byte value for the prevRandao field of the new payload.
   * @param suggestedFeeRecipient 20 bytes suggested value for the feeRecipient field of the new
   *     payload.
   * @param transactions List of transactions to be included in the new payload.
   * @param noTxPool Boolean value indicating whether the payload should be built without including
   *     transactions from the txpool.
   * @param gasLimit 64 bit value for the gasLimit field of the new payload.The gasLimit is optional
   *     w.r.t. compatibility with L1, but required when used as rollup.This field overrides the gas
   *     limit used during block-building.If not specified as rollup, a STATUS_INVALID is returned.
   * @param epoch The batch epoch number from derivation. This value is not expected by the engine
   *     is skipped during serialization and deserialization.
   * @param l1InclusionBlock The L1 block number when this batch was first fully derived. This value
   *     is not expected by the engine and is skipped during serialization and deserialization.
   * @param seqNumber The L2 sequence number of the block. This value is not expected by the engine
   *     and is skipped during serialization and deserialization.
   * @author zhouop0
   * @since 0.1.0
   */
  public record PayloadAttributes(
      BigInteger timestamp,
      String prevRandao,
      String suggestedFeeRecipient,
      List<String> transactions,
      boolean noTxPool,
      BigInteger gasLimit,
      Epoch epoch,
      BigInteger l1InclusionBlock,
      BigInteger seqNumber) {}

  /**
   * The type Status.
   *
   * @author zhouop0
   * @since 0.1.0
   */
  public enum Status {
    /** Valid status. */
    Valid,
    /** Invalid status. */
    Invalid,
    /** Syncing status. */
    Syncing,
    /** Accepted status. */
    Accepted,
    /** Invalid block hash status. */
    InvalidBlockHash,
  }

  /**
   * The type PayloadStatus.
   *
   * <p>status The status of the payload. latestValidHash 32 Bytes - the hash of the most recent
   * valid block in the branch defined by payload and its ancestors. validationError A message
   * providing additional details on the validation error if the payload is classified as INVALID or
   * INVALID_BLOCK_HASH.
   *
   * @author zhouop0
   * @since 0.1.0
   */
  public static class PayloadStatus {

    private Status status;
    private String latestValidHash;
    private String validationError;

    /** PayloadStatus constructor. */
    public PayloadStatus() {}

    /**
     * status get method.
     *
     * @return status. status
     */
    public Status getStatus() {
      return status;
    }

    /**
     * status set method.
     *
     * @param status status.
     */
    public void setStatus(Status status) {
      this.status = status;
    }

    /**
     * latestValidHash get method.
     *
     * @return latestValidHash. latest valid hash
     */
    public String getLatestValidHash() {
      return latestValidHash;
    }

    /**
     * latestValidHash set method.
     *
     * @param latestValidHash latestValidHash.
     */
    public void setLatestValidHash(String latestValidHash) {
      this.latestValidHash = latestValidHash;
    }

    /**
     * validationError get method.
     *
     * @return validationError. validation error
     */
    public String getValidationError() {
      return validationError;
    }

    /**
     * validationError set method.
     *
     * @param validationError validationError.
     */
    public void setValidationError(String validationError) {
      this.validationError = validationError;
    }
  }
}
