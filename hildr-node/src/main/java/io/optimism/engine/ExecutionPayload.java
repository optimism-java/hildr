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

import io.optimism.common.RawTransaction;
import java.math.BigInteger;
import java.util.List;
import org.web3j.protocol.core.Response;

/**
 * The type ExecutionPayload.
 *
 * <p>parentHash A 32 byte hash of the parent payload. feeRecipient A 20 byte hash (aka Address) for
 * the feeRecipient field of the new payload. stateRoot A 32 byte state root hash. receiptsRoot A 32
 * byte receipt root hash. logsBloom A 32 byte logs bloom filter. prevRandom A 32 byte beacon chain
 * randomness value. blockHash The 32 byte block hash. gasLimit A 64 bit value for the gas limit.
 * gasUsed A 64 bit value for the gas used. timestamp A 64 bit value for the timestamp field of the
 * new payload. baseFeePerGas 256 bits for the base fee per gas. blockNumber A 64 bit number for the
 * current block index. extraData 0 to 32 byte value for extra data. transactions An array of
 * transaction objects where each object is a byte list.
 *
 * @author zhouop0
 * @since 0.1.0
 */
public class ExecutionPayload extends Response {
  private String parentHash;
  private String feeRecipient;
  private String stateRoot;
  private String receiptsRoot;
  private String logsBloom;
  private String prevRandom;
  private BigInteger blockNumber;
  private BigInteger gasLimit;
  private BigInteger gasUsed;
  private BigInteger timestamp;
  private String extraData;
  private BigInteger baseFeePerGas;
  private String blockHash;
  private List<RawTransaction> transactions;

  /** ExecutionPayload contractor. */
  public ExecutionPayload() {}

  /**
   * parentHash get method.
   *
   * @return parentHash.
   */
  public String getParentHash() {
    return parentHash;
  }

  /**
   * parentHash set method.
   *
   * @param parentHash parentHash.
   */
  public void setParentHash(String parentHash) {
    this.parentHash = parentHash;
  }

  /**
   * feeRecipient get method.
   *
   * @return feeRecipient.
   */
  public String getFeeRecipient() {
    return feeRecipient;
  }

  /**
   * feeRecipient set method.
   *
   * @param feeRecipient feeRecipient.
   */
  public void setFeeRecipient(String feeRecipient) {
    this.feeRecipient = feeRecipient;
  }

  /**
   * stateRoot get method.
   *
   * @return stateRoot.
   */
  public String getStateRoot() {
    return stateRoot;
  }

  /**
   * stateRoot set method.
   *
   * @param stateRoot stateRoot.
   */
  public void setStateRoot(String stateRoot) {
    this.stateRoot = stateRoot;
  }

  /**
   * receiptsRoot get method.
   *
   * @return receiptsRoot.
   */
  public String getReceiptsRoot() {
    return receiptsRoot;
  }

  /**
   * receiptsRoot set method.
   *
   * @param receiptsRoot receiptsRoot.
   */
  public void setReceiptsRoot(String receiptsRoot) {
    this.receiptsRoot = receiptsRoot;
  }

  /**
   * prevRandom get method.
   *
   * @return prevRandom.
   */
  public String getLogsBloom() {
    return logsBloom;
  }

  /**
   * prevRandom set method.
   *
   * @param logsBloom prevRandom.
   */
  public void setLogsBloom(String logsBloom) {
    this.logsBloom = logsBloom;
  }

  /**
   * prevRandom get method.
   *
   * @return prevRandom.
   */
  public String getPrevRandom() {
    return prevRandom;
  }

  /**
   * prevRandom set method.
   *
   * @param prevRandom prevRandom.
   */
  public void setPrevRandom(String prevRandom) {
    this.prevRandom = prevRandom;
  }

  /**
   * blockNumber get method.
   *
   * @return blockNumber.
   */
  public BigInteger getBlockNumber() {
    return blockNumber;
  }

  /**
   * blockNumber set method.
   *
   * @param blockNumber blockNumber.
   */
  public void setBlockNumber(BigInteger blockNumber) {
    this.blockNumber = blockNumber;
  }

  /**
   * gasLimit get method.
   *
   * @return gasLimit.
   */
  public BigInteger getGasLimit() {
    return gasLimit;
  }

  /**
   * gasLimit set method.
   *
   * @param gasLimit gasLimit.
   */
  public void setGasLimit(BigInteger gasLimit) {
    this.gasLimit = gasLimit;
  }

  /**
   * gasUsed get method.
   *
   * @return gasUsed.
   */
  public BigInteger getGasUsed() {
    return gasUsed;
  }

  /**
   * gasUsed set method.
   *
   * @param gasUsed gasUsed.
   */
  public void setGasUsed(BigInteger gasUsed) {
    this.gasUsed = gasUsed;
  }

  /**
   * timestamp get method.
   *
   * @return timestamp.
   */
  public BigInteger getTimestamp() {
    return timestamp;
  }

  /**
   * timestamp set method.
   *
   * @param timestamp timestamp.
   */
  public void setTimestamp(BigInteger timestamp) {
    this.timestamp = timestamp;
  }

  /**
   * extraData get method.
   *
   * @return extraData.
   */
  public String getExtraData() {
    return extraData;
  }

  /**
   * extraData set method.
   *
   * @param extraData extraData.
   */
  public void setExtraData(String extraData) {
    this.extraData = extraData;
  }

  /**
   * baseFeePerGas get method.
   *
   * @return baseFeePerGas.
   */
  public BigInteger getBaseFeePerGas() {
    return baseFeePerGas;
  }

  /**
   * baseFeePerGas set method.
   *
   * @param baseFeePerGas baseFeePerGas.
   */
  public void setBaseFeePerGas(BigInteger baseFeePerGas) {
    this.baseFeePerGas = baseFeePerGas;
  }

  /**
   * blockHash get method.
   *
   * @return blockHash.
   */
  public String getBlockHash() {
    return blockHash;
  }

  /**
   * blockHash set method.
   *
   * @param blockHash blockHash
   */
  public void setBlockHash(String blockHash) {
    this.blockHash = blockHash;
  }

  /**
   * transactions get method.
   *
   * @return transactions
   */
  public List<RawTransaction> getTransactions() {
    return transactions;
  }

  /**
   * transactions set method.
   *
   * @param transactions transactions.
   */
  public void setTransactions(List<RawTransaction> transactions) {
    this.transactions = transactions;
  }
}
