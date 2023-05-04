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

import java.math.BigInteger;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.ArrayUtils;

/**
 * The type PendingChannel.
 *
 * @author grapebaba
 * @since 0.1.0
 */
public class PendingChannel {

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
   * @param highestL1Block the highest l 1 block
   * @param lowestL1Block the lowest l 1 block
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
        .max(Comparator.comparing(Frame::l1InclusionBlock))
        .map(Frame::l1InclusionBlock)
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
        List.of(frame),
        size,
        frame.l1InclusionBlock(),
        frame.l1InclusionBlock());
  }
}
