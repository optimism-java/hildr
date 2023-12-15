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

import io.optimism.derive.PurgeableIterator;
import io.optimism.utilities.derive.stages.Frame;
import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.jctools.queues.MessagePassingQueue;

/**
 * The type BatcherTransactions.
 *
 * @author grapebaba
 * @since 0.1.0
 */
public class BatcherTransactions implements PurgeableIterator<BatcherTransactions.BatcherTransaction> {
    private Deque<BatcherTransaction> txs;

    private MessagePassingQueue<BatcherTransactionMessage> txMessagesQueue;

    /**
     * Instantiates a new Batcher transactions.
     *
     * @param txMessagesQueue the tx messages queue
     */
    public BatcherTransactions(MessagePassingQueue<BatcherTransactionMessage> txMessagesQueue) {
        txs = new ArrayDeque<>();
        this.txMessagesQueue = txMessagesQueue;
    }

    /** Process incoming. */
    public void processIncoming() {
        BatcherTransactionMessage m;
        while ((m = txMessagesQueue.poll()) != null) {
            BatcherTransactionMessage curr = m;
            curr.txs().forEach(txData -> txs.addLast(BatcherTransaction.create(txData, curr.l1Origin())));
        }
    }

    @Override
    public BatcherTransaction next() {
        this.processIncoming();
        return txs.pollFirst();
    }

    @Override
    public void purge() {

        while (this.txMessagesQueue.poll() != null) {}

        this.txs.clear();
    }

    /**
     * The type BatcherTransaction.
     *
     * @param version the version
     * @param frames the frames
     * @author grapebaba
     * @since 0.1.0
     */
    public record BatcherTransaction(byte version, List<Frame> frames) {

        /**
         * Create BatcherTransaction.
         *
         * @param data the data
         * @param l1Origin the L1 origin
         * @return the BatcherTransaction
         */
        public static BatcherTransaction create(byte[] data, BigInteger l1Origin) {
            final byte version = data[0];
            final byte[] framesData = ArrayUtils.subarray(data, 1, data.length);

            int offset = 0;
            List<Frame> frames = new ArrayList<>();
            while (offset < framesData.length) {
                final ImmutablePair<Frame, Integer> framePair = Frame.from(framesData, offset, l1Origin);
                Frame frame = framePair.getLeft();
                int nextOffset = framePair.getRight();
                frames.add(frame);
                offset = nextOffset;
            }

            return new BatcherTransaction(version, frames);
        }
    }

    /**
     * The type BatcherTransactionMessage.
     *
     * @param txs the txs
     * @param l1Origin the L1 origin
     * @author grapebaba
     * @since 0.1.0
     */
    public record BatcherTransactionMessage(List<byte[]> txs, BigInteger l1Origin) {}
}
