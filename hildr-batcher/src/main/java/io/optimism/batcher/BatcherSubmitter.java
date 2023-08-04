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

package io.optimism.batcher;

import com.google.common.util.concurrent.AbstractExecutionThreadService;
import io.optimism.batcher.channel.ChannelManager;
import io.optimism.batcher.config.Config;
import io.optimism.batcher.ex.BatcherExecutionException;
import io.optimism.batcher.loader.BlockLoader;
import io.optimism.batcher.loader.LoaderConfig;
import io.optimism.batcher.publisher.ChannelDataPublisher;
import io.optimism.batcher.publisher.PublisherConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

/**
 * BatcherSubmitter class.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
@SuppressWarnings("UnusedVariable")
public class BatcherSubmitter extends AbstractExecutionThreadService {

  private static final Logger LOGGER = LoggerFactory.getLogger(BatcherSubmitter.class);

  private final Config config;

  private final ChannelManager channelManager;
  private final BlockLoader blockLoader;
  private final ChannelDataPublisher channelPublisher;

  private volatile boolean isShutdownTriggered = false;

  /**
   * Constructor of BatcherSubmitter.
   *
   * @param config BatcherSubmitter config
   */
  public BatcherSubmitter(Config config) {
    this.config = config;
    this.channelManager = new ChannelManager();
    this.blockLoader = new BlockLoader(LoaderConfig.from(config), this.channelManager::addL2Block);

    this.channelPublisher =
        new ChannelDataPublisher(
            PublisherConfig.from(config, this.blockLoader.getRollConfig().batchInboxAddress()),
            this.channelManager::txData,
            this::handleReceipt);
  }

  private void trySubmitBatchData() {
    this.blockLoader.loadBlock();
    // If no data has been sent, then sleep for a period of time.
    if (!this.channelPublisher.publishPendingBlock()) {
      try {
        Thread.sleep(config.pollInterval());
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new BatcherExecutionException(e);
      }
    }
  }

  private void handleReceipt(TransactionReceipt receipt) {
    if (receipt.isStatusOK()) {
      // todo this.channelManager.txConfirmed();
    } else {
      // todo this.channelManager.txFailed();
    }
  }

  @Override
  protected void run() throws Exception {
    while (isRunning() && !this.isShutdownTriggered) {
      this.trySubmitBatchData();
    }
  }

  @Override
  protected void shutDown() throws Exception {
    super.shutDown();
    this.blockLoader.close();
    this.channelPublisher.close();
    this.channelManager.clear();
  }

  @Override
  protected void triggerShutdown() {
    this.isShutdownTriggered = true;
  }
}
