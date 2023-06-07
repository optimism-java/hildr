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

package io.optimism.derive.stages;

import io.optimism.common.BlockNotIncludedException;
import io.optimism.common.Epoch;
import io.optimism.config.Config;
import io.optimism.config.Config.SystemAccounts;
import io.optimism.derive.PurgeableIterator;
import io.optimism.derive.State;
import io.optimism.derive.stages.Batches.Batch;
import io.optimism.engine.ExecutionPayload.PayloadAttributes;
import io.optimism.l1.L1Info;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import net.osslabz.evm.abi.definition.AbiDefinition.Entry.Param;
import net.osslabz.evm.abi.definition.AbiDefinition.Event;
import net.osslabz.evm.abi.definition.SolidityType;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.TypeEncoder;
import org.web3j.abi.datatypes.Uint;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.crypto.Hash;
import org.web3j.protocol.core.methods.response.EthLog.LogObject;
import org.web3j.rlp.RlpEncoder;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpString;
import org.web3j.rlp.RlpType;
import org.web3j.utils.Numeric;

/**
 * The type Attributes.
 *
 * @param <I> the type parameter
 * @author grapebaba
 * @since 0.1.0
 */
public class Attributes<I extends PurgeableIterator<Batch>>
    implements PurgeableIterator<PayloadAttributes> {

  private static final Logger LOGGER = LoggerFactory.getLogger(Attributes.class);

  private final I batchIterator;

  private final AtomicReference<State> state;

  private BigInteger sequenceNumber;

  private String epochHash;

  private final Config config;

  /**
   * Instantiates a new Attributes.
   *
   * @param batchIterator the batch iterator
   * @param state the state
   * @param config the config
   * @param sequenceNumber the sequence number
   */
  public Attributes(
      I batchIterator, AtomicReference<State> state, Config config, BigInteger sequenceNumber) {
    this.batchIterator = batchIterator;
    this.state = state;
    this.config = config;
    this.sequenceNumber = sequenceNumber;
    this.epochHash = this.state.get().getSafeEpoch().hash();
  }

  @Override
  public PayloadAttributes next() {
    final Batch batch = this.batchIterator.next();
    return batch != null ? this.deriveAttributes(batch) : null;
  }

  private PayloadAttributes deriveAttributes(Batch batch) {
    LOGGER.debug("attributes derived from block {}", batch.epochNum());
    LOGGER.debug("batch epoch hash {}", batch.epochHash());

    this.updateSequenceNumber(batch.epochHash());

    State state = this.state.get();
    L1Info l1Info = state.l1Info(batch.epochHash());

    Epoch epoch = new Epoch(batch.epochNum(), batch.epochHash(), l1Info.blockInfo().timestamp());

    BigInteger timestamp = batch.timestamp();
    BigInteger l1InclusionBlock = batch.l1InclusionBlock();
    BigInteger seqNumber = this.sequenceNumber;
    String prevRandao = l1Info.blockInfo().mixHash();
    List<String> transactions = this.deriveTransactions(batch, l1Info);
    String suggestedFeeRecipient = SystemAccounts.defaultSystemAccounts().feeVault();
    BigInteger gasLimit = l1Info.systemConfig().gasLimit();

    return new PayloadAttributes(
        timestamp,
        prevRandao,
        suggestedFeeRecipient,
        transactions,
        true,
        gasLimit,
        epoch,
        l1InclusionBlock,
        seqNumber);
  }

  private List<String> deriveTransactions(Batch batch, L1Info l1Info) {
    List<String> transactions = new ArrayList<>();

    String attributesTx = this.deriveAttributesDeposited(l1Info, batch.timestamp());
    transactions.add(attributesTx);

    if (this.sequenceNumber.equals(BigInteger.ZERO)) {
      List<String> userDepositedTxs = this.deriveUserDeposited();
      transactions.addAll(userDepositedTxs);
    }

    List<String> rest = batch.transactions();
    transactions.addAll(rest);

    return transactions;
  }

  private List<String> deriveUserDeposited() {
    State state = this.state.get();
    L1Info l1Info = state.l1Info(this.epochHash);
    if (l1Info == null) {
      throw new L1InfoNotFoundException();
    }
    return l1Info.userDeposits().stream()
        .map(
            deposit -> {
              DepositedTransaction tx = DepositedTransaction.from(deposit);
              return Numeric.toHexString(tx.encode());
            })
        .collect(Collectors.toList());
  }

  private String deriveAttributesDeposited(L1Info l1Info, BigInteger batchTimestamp) {
    BigInteger seq = this.sequenceNumber;
    AttributesDeposited attributesDeposited =
        AttributesDeposited.fromBlockInfo(l1Info, seq, batchTimestamp, this.config);
    DepositedTransaction attributeTx = DepositedTransaction.from(attributesDeposited);
    return Numeric.toHexString(attributeTx.encode());
  }

  private void updateSequenceNumber(String batchEpochHash) {
    if (this.epochHash.equals(batchEpochHash)) {
      this.sequenceNumber = this.sequenceNumber.add(BigInteger.ONE);
    } else {
      this.sequenceNumber = BigInteger.ZERO;
    }

    this.epochHash = batchEpochHash;
  }

  @Override
  public void purge() {
    this.batchIterator.purge();
    this.sequenceNumber = BigInteger.ZERO;
    this.epochHash = this.state.get().getSafeEpoch().hash();
  }

  /**
   * The type UserDeposited.
   *
   * @param from the from address.
   * @param to the to address.
   * @param mint the mint amount.
   * @param value the value.
   * @param gas the gas.
   * @param isCreation the isCreation flag.
   * @param data the data.
   * @param l1BlockNum the L1 blockNum.
   * @param l1BlockHash the L1 blockHash.
   * @param logIndex the logIndex.
   * @author grapebaba
   * @since 0.1.0
   */
  public record UserDeposited(
      String from,
      String to,
      BigInteger mint,
      BigInteger value,
      BigInteger gas,
      boolean isCreation,
      byte[] data,
      BigInteger l1BlockNum,
      String l1BlockHash,
      BigInteger logIndex) {

    private static final Event event;

    static {
      Param from = new Param();
      from.setIndexed(true);
      from.setName("from");
      from.setType(new SolidityType.AddressType());
      Param to = new Param();
      to.setIndexed(true);
      to.setName("to");
      to.setType(new SolidityType.AddressType());
      Param version = new Param();
      version.setIndexed(true);
      version.setName("version");
      version.setType(new SolidityType.UnsignedIntType("uint256"));
      Param opaqueData = new Param();
      opaqueData.setIndexed(false);
      opaqueData.setName("opaqueData");
      opaqueData.setType(new SolidityType.BytesType());
      event = new Event(false, "UserDeposited", List.of(from, to, version, opaqueData), List.of());
    }

    /**
     * Derive UserDeposited from log.
     *
     * @param log the log
     * @return the UserDeposited
     */
    public static UserDeposited fromLog(LogObject log) {
      byte[][] topics =
          log.getTopics().stream().map(Numeric::hexStringToByteArray).toArray(byte[][]::new);
      List<?> decodedEvent = event.decode(Numeric.hexStringToByteArray(log.getData()), topics);
      byte[] opaqueData = (byte[]) decodedEvent.get(3);

      String from =
          Numeric.prependHexPrefix(
              StringUtils.substring(Numeric.cleanHexPrefix(log.getTopics().get(1)), 24));
      String to =
          Numeric.prependHexPrefix(
              StringUtils.substring(Numeric.cleanHexPrefix(log.getTopics().get(2)), 24));
      BigInteger mint = Numeric.toBigInt(ArrayUtils.subarray(opaqueData, 0, 32));
      BigInteger value = Numeric.toBigInt(ArrayUtils.subarray(opaqueData, 32, 64));
      BigInteger gas = Numeric.toBigInt(ArrayUtils.subarray(opaqueData, 64, 72));
      boolean isCreation = opaqueData[72] != (byte) 0;
      byte[] data = ArrayUtils.subarray(opaqueData, 73, opaqueData.length);
      BigInteger l1BlockNum = log.getBlockNumber();
      if (l1BlockNum == null) {
        throw new BlockNotIncludedException();
      }
      String l1BlockHash = log.getBlockHash();
      if (l1BlockHash == null) {
        throw new BlockNotIncludedException();
      }

      return new UserDeposited(
          from, to, mint, value, gas, isCreation, data, l1BlockNum, l1BlockHash, log.getLogIndex());
    }
  }

  /**
   * The type AttributesDeposited.
   *
   * @param number the number
   * @param timestamp the timestamp
   * @param baseFee the base fee
   * @param hash the hash
   * @param sequenceNumber the sequence number
   * @param batcherHash the batcher hash
   * @param feeOverhead the fee overhead
   * @param feeScalar the fee scalar
   * @param gas the gas
   * @param isSystemTx the is system tx
   * @author grapebaba
   * @since 0.1.0
   */
  public record AttributesDeposited(
      BigInteger number,
      BigInteger timestamp,
      BigInteger baseFee,
      String hash,
      BigInteger sequenceNumber,
      String batcherHash,
      BigInteger feeOverhead,
      BigInteger feeScalar,
      BigInteger gas,
      boolean isSystemTx) {

    /**
     * From block info attributes deposited.
     *
     * @param l1Info the l 1 info
     * @param sequenceNumber the sequence number
     * @param batchTimestamp the batch timestamp
     * @param config the config
     * @return the attributes deposited
     */
    public static AttributesDeposited fromBlockInfo(
        L1Info l1Info, BigInteger sequenceNumber, BigInteger batchTimestamp, Config config) {
      boolean isRegolith = batchTimestamp.compareTo(config.chainConfig().regolithTime()) >= 0;
      boolean isSystemTx = !isRegolith;
      BigInteger gas =
          isRegolith ? BigInteger.valueOf(1_000_000L) : BigInteger.valueOf(150_000_000L);

      return new AttributesDeposited(
          l1Info.blockInfo().number(),
          l1Info.blockInfo().timestamp(),
          l1Info.blockInfo().baseFee(),
          l1Info.blockInfo().hash(),
          sequenceNumber,
          l1Info.systemConfig().batcherHash(),
          l1Info.systemConfig().l1FeeOverhead(),
          l1Info.systemConfig().l1FeeScalar(),
          gas,
          isSystemTx);
    }

    /**
     * Encode bytes.
     *
     * @return the bytes
     */
    public byte[] encode() {
      StringBuilder sb = new StringBuilder();
      sb.append("015d8eb9"); // selector
      sb.append(TypeEncoder.encode(new Uint(this.number)));
      sb.append(TypeEncoder.encode(new Uint(this.timestamp)));
      sb.append(TypeEncoder.encode(new Uint(this.baseFee)));
      sb.append(TypeEncoder.encode(new Bytes32(Numeric.hexStringToByteArray(this.hash))));
      sb.append(TypeEncoder.encode(new Uint(this.sequenceNumber)));
      sb.append(TypeEncoder.encode(new Bytes32(Numeric.hexStringToByteArray(this.batcherHash))));
      sb.append(TypeEncoder.encode(new Uint(this.feeOverhead)));
      sb.append(TypeEncoder.encode(new Uint(this.feeScalar)));

      return Numeric.hexStringToByteArray(sb.toString());
    }
  }

  /**
   * The type DepositedTransaction.
   *
   * @param data the data
   * @param isSystemTx the is system tx
   * @param gas the gas
   * @param value the value
   * @param mint the mint
   * @param to the to address
   * @param from the from address
   * @param sourceHash the source hash
   * @author grapebaba
   * @since 0.1.0
   */
  public record DepositedTransaction(
      String sourceHash,
      String from,
      String to,
      BigInteger mint,
      BigInteger value,
      BigInteger gas,
      boolean isSystemTx,
      byte[] data) {

    /**
     * From deposited transaction.
     *
     * @param attributesDeposited the attributes deposited
     * @return the deposited transaction
     */
    public static DepositedTransaction from(AttributesDeposited attributesDeposited) {
      byte[] hash = Numeric.hexStringToByteArray(attributesDeposited.hash);
      byte[] seq = Numeric.toBytesPadded(attributesDeposited.sequenceNumber, 32);
      byte[] h = Hash.sha3(ArrayUtils.addAll(hash, seq));
      byte[] domain = Numeric.toBytesPadded(BigInteger.ONE, 32);
      byte[] sourceHash = Hash.sha3(ArrayUtils.addAll(domain, h));

      SystemAccounts systemAccounts = SystemAccounts.defaultSystemAccounts();
      String from = systemAccounts.attributesDepositor();
      String to = systemAccounts.attributesPreDeploy();

      byte[] data = attributesDeposited.encode();

      return new DepositedTransaction(
          Numeric.toHexString(sourceHash),
          from,
          to,
          BigInteger.ZERO,
          BigInteger.ZERO,
          attributesDeposited.gas,
          attributesDeposited.isSystemTx,
          data);
    }

    /**
     * From deposited transaction.
     *
     * @param userDeposited the user deposited
     * @return the deposited transaction
     */
    public static DepositedTransaction from(UserDeposited userDeposited) {
      byte[] hash = Numeric.hexStringToByteArray(userDeposited.l1BlockHash);
      byte[] logIndex = Numeric.toBytesPadded(userDeposited.logIndex, 32);
      byte[] h = Hash.sha3(ArrayUtils.addAll(hash, logIndex));
      byte[] domain = Numeric.toBytesPadded(BigInteger.ZERO, 32);
      byte[] sourceHash = Hash.sha3(ArrayUtils.addAll(domain, h));

      return new DepositedTransaction(
          Numeric.toHexString(sourceHash),
          userDeposited.from,
          userDeposited.isCreation ? null : userDeposited.to,
          userDeposited.mint,
          userDeposited.value,
          userDeposited.gas,
          false,
          userDeposited.data);
    }

    /**
     * Encode rlp list.
     *
     * @return the rlp list
     */
    public byte[] encode() {
      List<RlpType> result = new ArrayList<>();

      result.add(RlpString.create(Numeric.hexStringToByteArray(this.sourceHash)));
      result.add(RlpString.create(Numeric.hexStringToByteArray(this.from)));

      if (StringUtils.isNotEmpty(this.to)) {
        result.add(RlpString.create(Numeric.hexStringToByteArray(this.to)));
      } else {
        result.add(RlpString.create(""));
      }

      result.add(RlpString.create(this.mint));
      result.add(RlpString.create(this.value));
      result.add(RlpString.create(this.gas));
      result.add(RlpString.create(this.isSystemTx ? 1L : 0L));
      result.add(RlpString.create(this.data));

      RlpList rlpList = new RlpList(result);
      byte[] encoded = RlpEncoder.encode(rlpList);

      return ByteBuffer.allocate(encoded.length + 1).put((byte) 0x7e).put(encoded).array();
    }
  }
}
