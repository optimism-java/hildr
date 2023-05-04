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
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;

/**
 * The type Frame.
 *
 * @param channelId the channel id
 * @param frameNumber the frame number
 * @param frameDataLen the frame data len
 * @param frameData the frame data
 * @param isLastFrame the is last frame
 * @param l1InclusionBlock the L1 inclusion block
 * @author grapebaba
 * @since 0.1.0
 */
public record Frame(
    BigInteger channelId,
    Integer frameNumber,
    Integer frameDataLen,
    byte[] frameData,
    Boolean isLastFrame,
    BigInteger l1InclusionBlock) {

  /**
   * From data immutable pair.
   *
   * @param data the data
   * @param offset the offset
   * @param l1InclusionBlock the L1 inclusion block
   * @return the immutable pair
   */
  public static ImmutablePair<Frame, Integer> from(
      byte[] data, int offset, BigInteger l1InclusionBlock) {
    final byte[] frameDataMessage = ArrayUtils.subarray(data, offset, data.length);
    if (frameDataMessage.length < 23) {
      throw new InvalidFrameSizeException("invalid frame size");
    }
    final BigInteger channelId = new BigInteger(ArrayUtils.subarray(frameDataMessage, 0, 16));
    final int frameNumber =
        new BigInteger(ArrayUtils.subarray(frameDataMessage, 16, 18)).intValue();
    final int frameDataLen =
        new BigInteger(ArrayUtils.subarray(frameDataMessage, 18, 22)).intValue();
    final int frameDataEnd = 22 + frameDataLen;

    if (frameDataMessage.length < frameDataEnd) {
      throw new InvalidFrameSizeException("invalid frame size");
    }

    final byte[] frameData = ArrayUtils.subarray(frameDataMessage, 22, frameDataEnd);
    final boolean isLastFrame = frameDataMessage[frameDataEnd] != 0;
    final Frame frame =
        new Frame(channelId, frameNumber, frameDataLen, frameData, isLastFrame, l1InclusionBlock);

    return new ImmutablePair<>(frame, offset + frameDataMessage.length);
  }
}
