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

import com.google.common.collect.Lists;
import io.optimism.config.Config;
import io.optimism.derive.PurgeableIterator;
import io.optimism.derive.stages.BatcherTransactions.BatcherTransaction;
import io.optimism.derive.stages.BatcherTransactions.Frame;
import io.optimism.derive.stages.Channels.Channel;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.ArrayUtils;

/**
 * The type Channels.
 *
 * @param <I> the type parameter
 * @author grapebaba
 * @since 0.1.0
 */
public class Channels<I extends PurgeableIterator<BatcherTransaction>>
    implements PurgeableIterator<Channel> {

  private final I batcherTxIterator;

  private final List<PendingChannel> pendingChannels;

  private final List<Frame> frameBank;

  private final BigInteger maxChannelSize;

  private final BigInteger channelTimeout;

  /**
   * Instantiates a new Channels.
   *
   * @param batcherTxIterator the batcher tx iterator
   * @param config the config
   */
  public Channels(I batcherTxIterator, Config config) {
    this.batcherTxIterator = batcherTxIterator;
    this.pendingChannels = Lists.newArrayList();
    this.frameBank = Lists.newArrayList();
    this.maxChannelSize = config.chainConfig().maxChannelSize();
    this.channelTimeout = config.chainConfig().channelTimeout();
  }

  @Override
  public void purge() {
    this.batcherTxIterator.purge();
    this.pendingChannels.clear();
    this.frameBank.clear();
  }

  @Override
  public Channel next() {
    return this.processFrames().orElse(null);
  }

  /**
   * Push frame.
   *
   * @param frame the frame
   */
  protected void pushFrame(Frame frame) {
    // Find a pending channel matching on the channel id
    Optional<PendingChannel> existedPc =
        this.pendingChannels.stream()
            .filter(c -> c.getChannelId().equals(frame.channelId()))
            .findFirst();

    // Insert frame if pending channel exists
    // Otherwise, construct a new pending channel with the frame's id
    if (existedPc.isPresent()) {
      existedPc.get().pushFrame(frame);
      if (existedPc.get().isTimedOut(this.channelTimeout)) {
        this.pendingChannels.remove(existedPc.get());
      }
    } else {
      PendingChannel pendingChannel = PendingChannel.create(frame);
      this.pendingChannels.add(pendingChannel);
    }
  }

  private void fillBank() {
    BatcherTransaction nextbatcherTransaction = this.batcherTxIterator.next();
    if (nextbatcherTransaction != null) {
      this.frameBank.addAll(nextbatcherTransaction.frames());
    }
  }

  /**
   * Fetch ready channel optional.
   *
   * @param id the id
   * @return the optional
   */
  protected Optional<Channel> fetchReadyChannel(BigInteger id) {
    return this.pendingChannels.stream()
        .filter(c -> c.getChannelId().equals(id) && c.isComplete())
        .findFirst()
        .map(
            pendingChannel -> {
              Channel channel = Channel.from(pendingChannel);
              pendingChannels.remove(pendingChannel);
              return channel;
            });
  }

  private Optional<Channel> processFrames() {
    this.fillBank();

    while (!this.frameBank.isEmpty()) {
      // Append the frame to the channel
      Frame frame = this.frameBank.remove(0);
      BigInteger frameChannelId = frame.channelId();
      this.pushFrame(frame);
      this.prune();

      Optional<Channel> channel = this.fetchReadyChannel(frameChannelId);
      if (channel.isPresent()) {
        return channel;
      }
    }

    return Optional.empty();
  }

  private Optional<PendingChannel> removePendingChannel() {
    if (this.pendingChannels.isEmpty()) {
      return Optional.empty();
    } else {
      return Optional.of(this.pendingChannels.remove(0));
    }
  }

  private long totalSize() {
    return this.pendingChannels.stream()
        .map(p -> p.frames.stream().map(Frame::frameDataLen).reduce(0, Integer::sum))
        .reduce(0, Integer::sum);
  }

  private void prune() {
    while (this.totalSize() > this.maxChannelSize.longValue()) {
      Optional<PendingChannel> removed = this.removePendingChannel();
      if (removed.isEmpty()) {
        throw new RuntimeException("should have removed a channel");
      }
    }
  }

  /**
   * Gets pending channels.
   *
   * @return the pending channels
   */
  public List<PendingChannel> getPendingChannels() {
    return pendingChannels;
  }

  /**
   * Create channels.
   *
   * @param <I> the type parameter
   * @param batcherTxIterator the batcher tx iterator
   * @param config the config
   * @return the channels
   */
  public static <I extends PurgeableIterator<BatcherTransaction>> Channels<I> create(
      I batcherTxIterator, Config config) {
    return new Channels<>(batcherTxIterator, config);
  }

  /**
   * The type Channel.
   *
   * @param id the id
   * @param data the data
   * @param l1InclusionBlock L1 inclusion block
   * @author grapebaba
   * @since 0.1.0
   */
  public record Channel(BigInteger id, byte[] data, BigInteger l1InclusionBlock) {

    /**
     * From channel.
     *
     * @param pendingChannel the pending channel
     * @return the channel
     */
    public static Channel from(PendingChannel pendingChannel) {
      return new Channel(
          pendingChannel.getChannelId(),
          pendingChannel.assemble(),
          pendingChannel.l1InclusionBlock());
    }
  }

  /**
   * The type PendingChannel.
   *
   * @author grapebaba
   * @since 0.1.0
   */
  public static class PendingChannel {

    private final BigInteger channelId;

    private final List<Frame> frames;

    private Integer size;

    private BigInteger highestL1Block;

    private BigInteger lowestL1Block;

    /**
     * Instantiates a new Pending channel.
     *
     * @param channelId the channel id
     * @param frames the frames
     * @param size the size
     * @param highestL1Block the highest L1 block
     * @param lowestL1Block the lowest L1 block
     */
    public PendingChannel(
        BigInteger channelId,
        List<Frame> frames,
        Integer size,
        BigInteger highestL1Block,
        BigInteger lowestL1Block) {
      this.channelId = channelId;
      this.frames = frames;
      this.size = size;
      this.highestL1Block = highestL1Block;
      this.lowestL1Block = lowestL1Block;
    }

    /**
     * Is complete boolean.
     *
     * @return the boolean
     */
    public boolean isComplete() {
      return size != null && frames.size() == size;
    }

    /**
     * Is timed out boolean.
     *
     * @param maxTimeout the max timeout
     * @return the boolean
     */
    public boolean isTimedOut(BigInteger maxTimeout) {
      return highestL1Block.subtract(lowestL1Block).compareTo(maxTimeout) > 0;
    }

    /**
     * Assemble byte [ ].
     *
     * @return the byte [ ]
     */
    public byte[] assemble() {
      return frames.stream()
          .sorted(Comparator.comparing(Frame::frameNumber))
          .map(Frame::frameData)
          .reduce(new byte[0], ArrayUtils::addAll);
    }

    /**
     * L 1 inclusion block big integer.
     *
     * @return the big integer
     */
    public BigInteger l1InclusionBlock() {
      return this.frames.stream()
          .map(Frame::l1InclusionBlock)
          .max(BigInteger::compareTo)
          .orElseThrow();
    }

    /**
     * Push frame.
     *
     * @param frame the frame
     */
    public void pushFrame(Frame frame) {
      final boolean hasSeen =
          this.frames.stream()
              .map(Frame::frameNumber)
              .anyMatch(number -> Objects.equals(number, frame.frameNumber()));

      if (!hasSeen) {
        if (frame.l1InclusionBlock().compareTo(this.highestL1Block) > 0) {
          this.highestL1Block = frame.l1InclusionBlock();
        } else if (frame.l1InclusionBlock().compareTo(this.lowestL1Block) < 0) {
          this.lowestL1Block = frame.l1InclusionBlock();
        }

        if (frame.isLastFrame()) {
          this.size = frame.frameNumber() + 1;
        }
        this.frames.add(frame);
      }
    }

    /**
     * Gets channel id.
     *
     * @return the channel id
     */
    public BigInteger getChannelId() {
      return channelId;
    }

    /**
     * Gets frames.
     *
     * @return the frames
     */
    public List<Frame> getFrames() {
      return frames;
    }

    /**
     * Gets size.
     *
     * @return the size
     */
    public Integer getSize() {
      return size;
    }

    /**
     * Gets highest l 1 block.
     *
     * @return the highest l 1 block
     */
    public BigInteger getHighestL1Block() {
      return highestL1Block;
    }

    /**
     * Gets lowest l 1 block.
     *
     * @return the lowest l 1 block
     */
    public BigInteger getLowestL1Block() {
      return lowestL1Block;
    }

    /**
     * Create pending channel.
     *
     * @param frame the frame
     * @return the pending channel
     */
    public static PendingChannel create(Frame frame) {
      final Integer size = frame.isLastFrame() ? frame.frameNumber() + 1 : null;
      return new PendingChannel(
          frame.channelId(),
          Lists.newArrayList(frame),
          size,
          frame.l1InclusionBlock(),
          frame.l1InclusionBlock());
    }
  }
}
