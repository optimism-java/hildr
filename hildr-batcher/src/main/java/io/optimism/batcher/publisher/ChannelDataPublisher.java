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

import io.optimism.batcher.channel.Channel;
import io.optimism.batcher.exception.Web3jCallException;
import io.optimism.type.BlockId;
import io.optimism.type.L1BlockRef;
import io.optimism.type.TxCandidate;
import io.optimism.utilities.gas.GasCalculator;
import io.optimism.utilities.rpc.Web3jProvider;
import io.optimism.utilities.telemetry.TracerTaskWrapper;
import java.io.Closeable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import jdk.incubator.concurrent.StructuredTaskScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.RawTransactionManager;

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

  private final Web3j l1Client;

  private final RawTransactionManager txManager;

  private final Function<BlockId, Channel.TxData> dataSupplier;

  private final Consumer<TransactionReceipt> txReceiptReturn;

  private L1BlockRef lastL1Tip;

  /**
   * Constructor of ChannelDataPublisher.
   *
   * @param config publisher config
   * @param dataSupplier publisher data supplier
   * @param txReceiptReturn tx receipt return callback
   */
  public ChannelDataPublisher(
      PublisherConfig config,
      Function<BlockId, Channel.TxData> dataSupplier,
      Consumer<TransactionReceipt> txReceiptReturn) {
    this.config = config;
    this.l1Client = Web3jProvider.createClient(config.l1RpcUrl());
    this.txManager = new RawTransactionManager(l1Client, null);
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
    Channel.TxData txData = dataSupplier.apply(l1HeadBlockRef.toId());
    if (txData == null) {
      LOGGER.trace("no transaction data available");
      throw new NoDataPublishException("no transaction data available");
    }
    this.sendTx(txData);
    return true;
  }

  private void sendTx(Channel.TxData txData) {
    byte[] txBytes = txData.txBytes();
    long intrinsicGas =
        GasCalculator.intrinsicGasWithoutAccessList(txBytes, false, true, true, false);
    var txCandidate = new TxCandidate(txBytes, this.config.batchInboxAddress(), intrinsicGas);
    EthSendTransaction ethSendTransaction = null;
    String txHash = ethSendTransaction.getTransactionHash();
    var txReceipt = this.getTxReceipt(txHash);
    txReceiptReturn.accept(txReceipt.getTransactionReceipt().get());
    // todo use txManager send tx
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
}
