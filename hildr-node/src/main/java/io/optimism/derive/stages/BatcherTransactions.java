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

import java.util.ArrayDeque;
import java.util.Deque;
import org.jctools.queues.MpscGrowableArrayQueue;

/**
 * The type BatcherTransactions.
 *
 * @author grapebaba
 * @since 0.1.0
 */
public class BatcherTransactions {

  private Deque<BatcherTransaction> txs;

  private MpscGrowableArrayQueue<BatcherTransactionMessage> txMessagesQueue;

  /**
   * Instantiates a new Batcher transactions.
   *
   * @param txMessagesQueue the tx messages queue
   */
  public BatcherTransactions(MpscGrowableArrayQueue<BatcherTransactionMessage> txMessagesQueue) {
    txs = new ArrayDeque<>();
    this.txMessagesQueue = txMessagesQueue;
  }

  /** Process incoming. */
  public void processIncoming() {
    txMessagesQueue.drain(
        e ->
            e.txs()
                .forEach(
                    txData -> {
                      try {
                        txs.addLast(BatcherTransaction.create(txData, e.l1Origin()));
                      } catch (Throwable e1) {
                        e1.printStackTrace();
                      }
                    }));
  }
}
