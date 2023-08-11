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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.optimism.config.Config;
import io.optimism.config.Config.ChainConfig;
import io.optimism.derive.stages.BatcherTransactions.BatcherTransactionMessage;
import io.optimism.derive.stages.Channels.Channel;
import io.optimism.utilities.derive.stages.Frame;
import java.math.BigInteger;
import java.util.Optional;
import org.jctools.queues.MessagePassingQueue;
import org.jctools.queues.MpscGrowableArrayQueue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.web3j.tuples.generated.Tuple2;

/**
 * The type ChannelsTest.
 *
 * @author grapebaba
 * @since 0.1.0
 */
class ChannelsTest {

  @Test
  @DisplayName("test push single channel frame")
  void testPushSingleChannelFrame() {
    Tuple2<Channels<BatcherTransactions>, MessagePassingQueue<BatcherTransactionMessage>> tuple2 =
        createStage();
    Frame frame = new Frame(BigInteger.valueOf(5L), 0, 0, new byte[0], true, BigInteger.ZERO);

    tuple2.component1().pushFrame(frame);
    assertEquals(1, tuple2.component1().getPendingChannels().size());
    assertEquals(
        BigInteger.valueOf(5L), tuple2.component1().getPendingChannels().get(0).getChannelId());
    assertTrue(tuple2.component1().getPendingChannels().get(0).isComplete());
  }

  @Test
  @DisplayName("test push multi channel frames")
  void testPushMultiChannelFrames() {
    Tuple2<Channels<BatcherTransactions>, MessagePassingQueue<BatcherTransactionMessage>> tuple2 =
        createStage();
    Frame frame1 = new Frame(BigInteger.valueOf(5L), 0, 0, new byte[0], false, BigInteger.ZERO);

    tuple2.component1().pushFrame(frame1);
    assertEquals(1, tuple2.component1().getPendingChannels().size());
    assertEquals(
        BigInteger.valueOf(5L), tuple2.component1().getPendingChannels().get(0).getChannelId());
    assertFalse(tuple2.component1().getPendingChannels().get(0).isComplete());
    Frame frame2 = new Frame(BigInteger.valueOf(5L), 1, 0, new byte[0], true, BigInteger.ZERO);

    tuple2.component1().pushFrame(frame2);
    assertEquals(1, tuple2.component1().getPendingChannels().size());
    assertEquals(
        BigInteger.valueOf(5L), tuple2.component1().getPendingChannels().get(0).getChannelId());
    assertTrue(tuple2.component1().getPendingChannels().get(0).isComplete());
  }

  @Test
  @DisplayName("test ready channel")
  void testReadyChannel() {
    Tuple2<Channels<BatcherTransactions>, MessagePassingQueue<BatcherTransactionMessage>> tuple2 =
        createStage();
    Frame frame1 =
        new Frame(BigInteger.valueOf(5L), 0, 0, new byte[0], false, BigInteger.valueOf(43L));

    Frame frame2 =
        new Frame(BigInteger.valueOf(5L), 1, 0, new byte[0], true, BigInteger.valueOf(96L));

    tuple2.component1().pushFrame(frame1);
    tuple2.component1().pushFrame(frame2);
    Channel channel = tuple2.component1().fetchReadyChannel(BigInteger.valueOf(5L)).get();
    assertEquals(BigInteger.valueOf(5L), channel.id());
    assertEquals(BigInteger.valueOf(96L), channel.l1InclusionBlock());
  }

  @Test
  @DisplayName("test ready channel not found")
  void testReadyChannelStillPending() {
    Tuple2<Channels<BatcherTransactions>, MessagePassingQueue<BatcherTransactionMessage>> tuple2 =
        createStage();
    Frame frame1 =
        new Frame(BigInteger.valueOf(5L), 0, 0, new byte[0], false, BigInteger.valueOf(43L));

    tuple2.component1().pushFrame(frame1);
    Optional<Channel> channelOpt = tuple2.component1().fetchReadyChannel(BigInteger.valueOf(5L));
    assertTrue(channelOpt.isEmpty());
  }

  private Tuple2<Channels<BatcherTransactions>, MessagePassingQueue<BatcherTransactionMessage>>
      createStage() {
    Config config = new Config("", "", "", "", null, 9545, ChainConfig.optimismGoerli());
    MessagePassingQueue<BatcherTransactionMessage> transactionMessageMessagePassingQueue =
        new MpscGrowableArrayQueue<>(4096);
    Channels<BatcherTransactions> channels =
        Channels.create(new BatcherTransactions(transactionMessageMessagePassingQueue), config);
    return new Tuple2<>(channels, transactionMessageMessagePassingQueue);
  }
}
