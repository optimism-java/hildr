/*
 * Copyright 2023 q315xia@163.com
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

package io.optimism.batcher.publisher;

import io.optimism.batcher.exception.Web3jCallException;
import io.optimism.type.BlockId;
import io.optimism.type.L1BlockRef;
import io.optimism.utilities.derive.stages.Frame;
import io.optimism.utilities.gas.GasCalculator;
import io.optimism.utilities.rpc.Web3jProvider;
import io.optimism.utilities.telemetry.TracerTaskWrapper;
import java.io.Closeable;
import java.math.BigInteger;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.function.Function;
import jdk.incubator.concurrent.StructuredTaskScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthEstimateGas;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.RawTransactionManager;
import org.web3j.utils.Numeric;

/**
 * ChannelDataPublisher class. It will get tx data from channelManager and push it to L1.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
@SuppressWarnings("UnusedVariable")
public class ChannelDataPublisher implements Closeable {

  private static final Logger LOGGER = LoggerFactory.getLogger(ChannelDataPublisher.class);

  private final PublisherConfig config;

  private final String fromAddress;

  private final Web3j l1Client;

  private final RawTransactionManager txManager;

  private final Function<BlockId, Frame> dataSupplier;

  private final BiConsumer<Frame, TransactionReceipt> txReceiptReturn;

  private L1BlockRef lastL1Tip;

  private BigInteger nonce;

  /**
   * Constructor of ChannelDataPublisher.
   *
   * @param config publisher config
   * @param dataSupplier publisher data supplier
   * @param txReceiptReturn tx receipt return callback
   */
  public ChannelDataPublisher(
      PublisherConfig config,
      Function<BlockId, Frame> dataSupplier,
      BiConsumer<Frame, TransactionReceipt> txReceiptReturn) {
    this.config = config;
    this.l1Client = Web3jProvider.createClient(config.l1RpcUrl());
    var credentials = Credentials.create(config.l1Signer());
    this.fromAddress = credentials.getAddress();
    this.txManager =
        new RawTransactionManager(l1Client, credentials, config.l1chainId().longValue());
    this.dataSupplier = dataSupplier;
    this.txReceiptReturn = txReceiptReturn;
  }

  /**
   * Publish pending block data.
   *
   * @return return true if there any data has been published, otherwise false.
   */
  public boolean publishPendingBlock() {
    boolean hasData = false;
    boolean sendData = true;
    while (sendData) {
      sendData = this.publishTxToL1();
      if (!hasData) {
        hasData = sendData;
      }
    }
    return hasData;
  }

  @Override
  public void close() {
    this.l1Client.shutdown();
  }

  private boolean publishTxToL1() {
    final L1BlockRef l1HeadBlockRef = getL1HeadBlockRef();
    this.recordL1Head(l1HeadBlockRef);
    var txData = dataSupplier.apply(l1HeadBlockRef.toId());
    if (txData == null) {
      LOGGER.trace("no transaction data available");
      throw new NoDataPublishException("no transaction data available");
    }
    this.sendTx(txData);
    return true;
  }

  private void sendTx(final Frame txData) {
    final String txBytes = Numeric.toHexString(txData.txBytes());
    final String to = this.config.batchInboxAddress();
    long intrinsicGas =
        GasCalculator.intrinsicGasWithoutAccessList(txData.txBytes(), false, true, true, false);

    var maxPriorityFeePerGas = this.getMaxPriorityFeePerGas();
    var baseFee = this.getBaseFee();
    var gasFeeCap = GasCalculator.calcGasFeeCap(baseFee, maxPriorityFeePerGas);
    var gasLimit =
        intrinsicGas == 0L
            ? this.getEstimateGas(this.config.batchInboxAddress(), txBytes)
            : BigInteger.valueOf(intrinsicGas);

    var rawTx =
        RawTransaction.createTransaction(
            this.config.l1chainId().longValue(),
            this.nextNonce(),
            gasLimit,
            to,
            BigInteger.ZERO,
            txBytes,
            maxPriorityFeePerGas,
            gasFeeCap);
    String txHash = this.signAndSend(rawTx);
    var txReceipt = this.getTxReceipt(txHash);
    txReceiptReturn.accept(txData, txReceipt.getTransactionReceipt().get());
  }

  private EthGetTransactionReceipt getTxReceipt(final String txHash) {
    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
      var receiptFuture = scope.fork(() -> l1Client.ethGetTransactionReceipt(txHash).send());
      scope.join();
      scope.throwIfFailed();
      return receiptFuture.resultNow();
    } catch (InterruptedException | ExecutionException e) {
      Thread.currentThread().interrupt();
      throw new Web3jCallException("failed to get TxReceipt", e);
    }
  }

  private L1BlockRef getL1HeadBlockRef() {
    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
      Future<EthBlock.Block> headBlockFuture =
          scope.fork(
              TracerTaskWrapper.wrap(
                  () ->
                      l1Client
                          .ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false)
                          .send()
                          .getBlock()));
      scope.join();
      scope.throwIfFailed();
      var block = headBlockFuture.get();
      if (block == null) {
        throw new Web3jCallException("get l1 latest block failed");
      }
      return L1BlockRef.from(block);
    } catch (ExecutionException | InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new Web3jCallException("failed to get L1HeadBlockRef", e);
    }
  }

  private void recordL1Head(L1BlockRef headRef) {
    if (this.lastL1Tip.equals(headRef)) {
      return;
    }
    this.lastL1Tip = headRef;
    // todo metrics LatestL1Block
  }

  private synchronized BigInteger nextNonce() {
    if (this.nonce != null) {
      this.nonce = this.nonce.add(BigInteger.ONE);
      return this.nonce;
    }
    EthGetTransactionCount txCount = null;
    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
      var countFuture =
          scope.fork(
              TracerTaskWrapper.wrap(
                  () -> {
                    var countReq =
                        this.l1Client.ethGetTransactionCount(
                            this.fromAddress, DefaultBlockParameterName.LATEST);
                    return countReq.send();
                  }));
      scope.join();
      scope.throwIfFailed();
      txCount = countFuture.resultNow();
      this.nonce = txCount.getTransactionCount();
      return this.nonce;
    } catch (InterruptedException | ExecutionException e) {
      Thread.currentThread().interrupt();
      throw new Web3jCallException("get tx count failed", e);
    }
  }

  private BigInteger getMaxPriorityFeePerGas() {
    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
      var gasFuture =
          scope.fork(
              TracerTaskWrapper.wrap(
                  () -> {
                    var countReq = this.l1Client.ethMaxPriorityFeePerGas();
                    return countReq.send();
                  }));
      scope.join();
      scope.throwIfFailed();
      return gasFuture.resultNow().getMaxPriorityFeePerGas();
    } catch (InterruptedException | ExecutionException e) {
      Thread.currentThread().interrupt();
      throw new Web3jCallException("get max priority fee gas failed", e);
    }
  }

  private BigInteger getBaseFee() {
    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
      var gasFuture =
          scope.fork(
              TracerTaskWrapper.wrap(
                  () -> {
                    var headReq =
                        this.l1Client.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false);
                    return headReq.send();
                  }));
      scope.join();
      scope.throwIfFailed();
      return gasFuture.resultNow().getBlock().getBaseFeePerGas();
    } catch (InterruptedException | ExecutionException e) {
      Thread.currentThread().interrupt();
      throw new Web3jCallException("get l1 head block failed", e);
    }
  }

  private BigInteger getEstimateGas(final String to, final String data) {
    EthEstimateGas gas = null;
    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
      var gasFuture =
          scope.fork(
              TracerTaskWrapper.wrap(
                  () -> {
                    final Transaction txParam =
                        new Transaction(this.fromAddress, null, null, null, to, null, data);
                    return this.l1Client.ethEstimateGas(txParam).send();
                  }));
      scope.join();
      scope.throwIfFailed();
      return gasFuture.resultNow().getAmountUsed();
    } catch (InterruptedException | ExecutionException e) {
      Thread.currentThread().interrupt();
      throw new Web3jCallException("get tx count failed", e);
    }
  }

  private String signAndSend(final RawTransaction rawTx) {
    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
      var txResultFuture = scope.fork(TracerTaskWrapper.wrap(() -> txManager.signAndSend(rawTx)));
      scope.join();
      scope.throwIfFailed();
      return txResultFuture.resultNow().getTransactionHash();
    } catch (InterruptedException | ExecutionException e) {
      Thread.currentThread().interrupt();
      throw new Web3jCallException("sign and send tx failed", e);
    }
  }
}
