package io.optimism.types;

import com.google.common.base.Objects;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Shorts;
import io.optimism.exceptions.InvalidFrameSizeException;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.utils.Numeric;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(Frame.class);

    /** Derivation version. */
    public static final byte DERIVATION_VERSION_0 = 0;

    /**
     * Frame over head size.
     */
    public static final int FRAME_V0_OVER_HEAD_SIZE = 23;

    /**
     * Get tx bytes.
     *
     * @return tx bytes
     */
    public byte[] txBytes() {
        return ArrayUtils.addAll(new byte[] {DERIVATION_VERSION_0}, frameData());
    }

    /**
     * Get Tx data unique code.
     *
     * @return unique code.
     */
    public String code() {
        return String.valueOf(Objects.hashCode(channelId, frameNumber));
    }

    /**
     * Create a new Frame instance.
     *
     * @param id channel id
     * @param frameNumber frame number
     * @param data frame data
     * @param isLastFrame is last frame
     * @return a new Frame instance
     */
    public static Frame create(BigInteger id, int frameNumber, byte[] data, boolean isLastFrame) {
        var dataLen = data == null ? 0 : data.length;
        return new Frame(id, frameNumber, dataLen, data, isLastFrame, null);
    }

    /**
     * Encode this Frame to bytes.
     *
     * @return encoded bytes from frame
     */
    public byte[] encode() {
        var bos = new ByteArrayOutputStream();
        bos.writeBytes(Numeric.toBytesPadded(channelId, 16));
        bos.writeBytes(Shorts.toByteArray((short) frameNumber().intValue()));
        bos.writeBytes(Ints.toByteArray(frameData().length));
        bos.writeBytes(frameData());
        bos.write(isLastFrame() ? 1 : 0);
        return bos.toByteArray();
    }

    /**
     * From data immutable pair.
     *
     * @param data the data
     * @param offset the offset
     * @param l1InclusionBlock the L1 inclusion block
     * @return the immutable pair
     */
    public static ImmutablePair<Frame, Integer> from(byte[] data, int offset, BigInteger l1InclusionBlock) {
        final byte[] frameDataMessage = ArrayUtils.subarray(data, offset, data.length);
        if (frameDataMessage.length < 23) {
            throw new InvalidFrameSizeException("invalid frame size");
        }

        final BigInteger channelId = Numeric.toBigInt(ArrayUtils.subarray(frameDataMessage, 0, 16));
        final int frameNumber =
                Numeric.toBigInt(ArrayUtils.subarray(frameDataMessage, 16, 18)).intValue();
        final int frameDataLen =
                Numeric.toBigInt(ArrayUtils.subarray(frameDataMessage, 18, 22)).intValue();
        final int frameDataEnd = 22 + frameDataLen;

        if (frameDataMessage.length < frameDataEnd) {
            throw new InvalidFrameSizeException("invalid frame size");
        }

        final byte[] frameData = ArrayUtils.subarray(frameDataMessage, 22, frameDataEnd);
        final boolean isLastFrame = frameDataMessage[frameDataEnd] != 0;
        final Frame frame = new Frame(channelId, frameNumber, frameDataLen, frameData, isLastFrame, l1InclusionBlock);
        LOGGER.debug(String.format(
                "saw batcher tx: block=%d, number=%d, is_last=%b", l1InclusionBlock, frameNumber, isLastFrame));

        return new ImmutablePair<>(frame, offset + frameDataMessage.length);
    }
}
