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

package io.optimism.derive;

import com.google.common.collect.AbstractIterator;
import io.optimism.config.Config;
import io.optimism.derive.stages.Attributes;
import io.optimism.derive.stages.BatcherTransactions;
import io.optimism.derive.stages.BatcherTransactions.BatcherTransactionMessage;
import io.optimism.derive.stages.Batches;
import io.optimism.derive.stages.Channels;
import io.optimism.engine.PayloadAttributes;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.jctools.queues.MpscGrowableArrayQueue;

/**
 * The type Pipeline.
 *
 * @author grapebaba
 * @since 0.1.0
 */
public class Pipeline extends AbstractIterator<PayloadAttributes>
    implements PurgeableIterator<PayloadAttributes> {

  private MpscGrowableArrayQueue<BatcherTransactionMessage> batcherTransactionQueue;

  private Attributes<?> attributes;

  private PayloadAttributes pendingAttributes;

  /**
   * Instantiates a new Pipeline.
   *
   * @param state the state
   * @param config the config
   */
  public Pipeline(AtomicReference<State> state, Config config) {
    batcherTransactionQueue = new MpscGrowableArrayQueue<>(1024);
    BatcherTransactions batcherTransactions = new BatcherTransactions(batcherTransactionQueue);
    Channels<BatcherTransactions> channels = Channels.create(batcherTransactions, config);
    Batches<Channels<BatcherTransactions>> batches = Batches.create(channels, state, config);
    attributes = new Attributes<>(batches, state, config);
  }

  @Override
  protected PayloadAttributes computeNext() {
    if (this.pendingAttributes != null) {
      return this.pendingAttributes;
    } else {
      return this.attributes.next();
    }
  }

  /**
   * Push batcher transactions.
   *
   * @param txs the txs
   * @param l1origin the l 1 origin
   */
  public void pushBatcherTransactions(List<byte[]> txs, BigInteger l1origin) {
    this.batcherTransactionQueue.relaxedOffer(new BatcherTransactionMessage(txs, l1origin));
  }

  /**
   * Peek attributes payload attributes.
   *
   * @return the payload attributes
   */
  public PayloadAttributes peekAttributes() {
    if (this.pendingAttributes == null) {
      this.pendingAttributes = this.next();
    }

    return this.pendingAttributes;
  }

  @Override
  public void purge() {
    this.attributes.purge();
  }
}
