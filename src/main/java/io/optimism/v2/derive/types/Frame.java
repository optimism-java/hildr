package io.optimism.v2.derive.types;

import com.google.common.base.Objects;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Shorts;
import io.optimism.exceptions.InvalidFrameSizeException;
import io.optimism.v2.derive.exception.FrameParseException;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
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
 * @author grapebaba
 * @since 0.1.0
 */
public record Frame(
        BigInteger channelId, Integer frameNumber, Integer frameDataLen, byte[] frameData, Boolean isLastFrame) {

    private static final Logger LOGGER = LoggerFactory.getLogger(Frame.class);

    /** Derivation version. */
    public static final byte DERIVATION_VERSION_0 = 0;

    /**
     * Frame over head size.
     */
    public static final int FRAME_V0_OVER_HEAD_SIZE = 23;

    public static final int MAX_FRAME_LEN = 1000000;

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
     * @param chId channel id
     * @param frameNumber frame number
     * @param data frame data
     * @param isLastFrame is last frame
     * @return a new Frame instance
     */
    public static Frame create(BigInteger chId, int frameNumber, byte[] data, boolean isLastFrame) {
        var dataLen = data == null ? 0 : data.length;
        return new Frame(chId, frameNumber, dataLen, data, isLastFrame);
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
     * @return the immutable pair, left is frame, right is current read bytes
     */
    public static ImmutablePair<Frame, Integer> from(byte[] data) {
        if (data.length < FRAME_V0_OVER_HEAD_SIZE) {
            throw new InvalidFrameSizeException("invalid frame size");
        }
        var offset = 0;
        final BigInteger channelId = Numeric.toBigInt(ArrayUtils.subarray(data, offset, 16));
        offset += 16;
        final int frameNumber =
                Numeric.toBigInt(ArrayUtils.subarray(data, offset, offset + 2)).intValue();
        offset += 2;
        final int frameDataLen =
                Numeric.toBigInt(ArrayUtils.subarray(data, offset, offset + 4)).intValue();
        offset += 4;

        final int frameDataEnd = offset + frameDataLen;

        if (frameDataEnd < 0 || frameDataEnd > MAX_FRAME_LEN || data.length < frameDataEnd) {
            throw new FrameParseException("invalid frame size");
        }

        final byte[] frameData = ArrayUtils.subarray(data, offset, frameDataEnd);
        final boolean isLastFrame = data[frameDataEnd] != 0;
        final Frame frame = new Frame(channelId, frameNumber, frameDataLen, frameData, isLastFrame);
        LOGGER.debug(String.format("saw batcher tx: number=%d, is_last=%b", frameNumber, isLastFrame));

        return new ImmutablePair<>(frame, FRAME_V0_OVER_HEAD_SIZE + frameDataEnd);
    }

    /**
     * Parse frames.
     *
     * @param encoded the encoded
     * @return the list of frames
     */
    public static List<Frame> parseFrames(byte[] encoded) {
        if (encoded.length == 0) {
            throw new FrameParseException("No frames");
        }

        if (encoded[0] != DERIVATION_VERSION_0) {
            throw new FrameParseException("Unsupported version");
        }

        byte[] data = new byte[encoded.length - 1];
        System.arraycopy(encoded, 1, data, 0, data.length);

        List<Frame> frames = new ArrayList<>();
        int offset = 0;

        while (offset < data.length) {
            byte[] parseBytes = offset == 0 ? data : ArrayUtils.subarray(data, offset, data.length);
            ImmutablePair<Frame, Integer> result = Frame.from(parseBytes);
            frames.add(result.left);
            offset += result.right;
        }

        if (offset != data.length) {
            throw new FrameParseException("Data length mismatch");
        }

        if (frames.isEmpty()) {
            throw new FrameParseException("No frames decoded");
        }

        return frames;
    }
}
