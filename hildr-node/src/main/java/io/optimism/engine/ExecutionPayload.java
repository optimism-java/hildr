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
 * <p>
 * parentHash    A 32 byte hash of the parent payload.
 * feeRecipient  A 20 byte hash (aka Address) for the feeRecipient field of the new payload.
 * stateRoot     A 32 byte state root hash.
 * receiptsRoot  A 32 byte receipt root hash.
 * logsBloom     A 32 byte logs bloom filter.
 * prevRandom    A 32 byte beacon chain randomness value.
 * blockHash     The 32 byte block hash.
 * gasLimit      A 64 bit value for the gas limit.
 * gasUsed       A 64 bit value for the gas used.
 * timestamp     A 64 bit value for the timestamp field of the new payload.
 * baseFeePerGas 256 bits for the base fee per gas.
 * blockNumber   A 64 bit number for the current block index.
 * extraData     0 to 32 byte value for extra data.
 * transactions  An array of transaction objects where each object is a byte list.
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

  public String getParentHash() {
    return parentHash;
  }

  public void setParentHash(String parentHash) {
    this.parentHash = parentHash;
  }

  public String getFeeRecipient() {
    return feeRecipient;
  }

  public void setFeeRecipient(String feeRecipient) {
    this.feeRecipient = feeRecipient;
  }

  public String getStateRoot() {
    return stateRoot;
  }

  public void setStateRoot(String stateRoot) {
    this.stateRoot = stateRoot;
  }

  public String getReceiptsRoot() {
    return receiptsRoot;
  }

  public void setReceiptsRoot(String receiptsRoot) {
    this.receiptsRoot = receiptsRoot;
  }

  public String getLogsBloom() {
    return logsBloom;
  }

  public void setLogsBloom(String logsBloom) {
    this.logsBloom = logsBloom;
  }

  public String getPrevRandom() {
    return prevRandom;
  }

  public void setPrevRandom(String prevRandom) {
    this.prevRandom = prevRandom;
  }

  public BigInteger getBlockNumber() {
    return blockNumber;
  }

  public void setBlockNumber(BigInteger blockNumber) {
    this.blockNumber = blockNumber;
  }

  public BigInteger getGasLimit() {
    return gasLimit;
  }

  public void setGasLimit(BigInteger gasLimit) {
    this.gasLimit = gasLimit;
  }

  public BigInteger getGasUsed() {
    return gasUsed;
  }

  public void setGasUsed(BigInteger gasUsed) {
    this.gasUsed = gasUsed;
  }

  public BigInteger getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(BigInteger timestamp) {
    this.timestamp = timestamp;
  }

  public String getExtraData() {
    return extraData;
  }

  public void setExtraData(String extraData) {
    this.extraData = extraData;
  }

  public BigInteger getBaseFeePerGas() {
    return baseFeePerGas;
  }

  public void setBaseFeePerGas(BigInteger baseFeePerGas) {
    this.baseFeePerGas = baseFeePerGas;
  }

  public String getBlockHash() {
    return blockHash;
  }

  public void setBlockHash(String blockHash) {
    this.blockHash = blockHash;
  }

  public List<RawTransaction> getTransactions() {
    return transactions;
  }

  public void setTransactions(List<RawTransaction> transactions) {
    this.transactions = transactions;
  }
}
