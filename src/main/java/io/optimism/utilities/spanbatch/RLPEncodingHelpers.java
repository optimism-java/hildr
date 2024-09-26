package io.optimism.utilities.spanbatch;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.MutableBytes;

/**
 * Helper static methods to facilitate RLP encoding <b>within this package</b>. Neither this class
 * nor any of its method are meant to be exposed publicly, they are too low level.
 */
public class RLPEncodingHelpers {
    private RLPEncodingHelpers() {}

    /**
     * Is single rlp byte boolean.
     *
     * @param value the value
     * @return the boolean
     */
    public static boolean isSingleRLPByte(final Bytes value) {
        return value.size() == 1 && value.get(0) >= 0;
    }

    /**
     * Is short element boolean.
     *
     * @param value the value
     * @return the boolean
     */
    public static boolean isShortElement(final Bytes value) {
        return value.size() <= 55;
    }

    /**
     * Is short list boolean.
     *
     * @param payloadSize the payload size
     * @return the boolean
     */
    public static boolean isShortList(final int payloadSize) {
        return payloadSize <= 55;
    }

    /**
     * The encoded size of the provided value.
     *
     * @param value the value
     * @return the int
     */
    public static int elementSize(final Bytes value) {
        if (isSingleRLPByte(value)) return 1;

        if (isShortElement(value)) return 1 + value.size();

        return 1 + sizeLength(value.size()) + value.size();
    }

    /**
     * The encoded size of a list given the encoded size of its payload.  @param payloadSize the payload size
     *
     * @param payloadSize the payload size
     * @return the int
     */
    public static int listSize(final int payloadSize) {
        int size = 1 + payloadSize;
        if (!isShortList(payloadSize)) size += sizeLength(payloadSize);
        return size;
    }

    /**
     * Writes the result of encoding the provided value to the provided destination (which must be big
     * enough).
     *
     * @param value      the value
     * @param dest       the dest
     * @param destOffset the dest offset
     * @return the int
     */
    public static int writeElement(final Bytes value, final MutableBytes dest, final int destOffset) {
        final int size = value.size();
        if (isSingleRLPByte(value)) {
            dest.set(destOffset, value.get(0));
            return destOffset + 1;
        }

        if (isShortElement(value)) {
            dest.set(destOffset, (byte) (0x80 + size));
            value.copyTo(dest, destOffset + 1);
            return destOffset + 1 + size;
        }

        final int offset = writeLongMetadata(0xb7, size, dest, destOffset);
        value.copyTo(dest, offset);
        return offset + size;
    }

    /**
     * Writes the encoded header of a list provided its encoded payload size to the provided
     * destination (which must be big enough).
     *
     * @param payloadSize the payload size
     * @param dest        the dest
     * @param destOffset  the dest offset
     * @return the int
     */
    public static int writeListHeader(final int payloadSize, final MutableBytes dest, final int destOffset) {
        if (isShortList(payloadSize)) {
            dest.set(destOffset, (byte) (0xc0 + payloadSize));
            return destOffset + 1;
        }

        return writeLongMetadata(0xf7, payloadSize, dest, destOffset);
    }

    private static int writeLongMetadata(
            final int baseCode, final int size, final MutableBytes dest, final int destOffset) {
        final int sizeLength = sizeLength(size);
        dest.set(destOffset, (byte) (baseCode + sizeLength));
        int shift = 0;
        for (int i = 0; i < sizeLength; i++) {
            dest.set(destOffset + sizeLength - i, (byte) (size >> shift));
            shift += 8;
        }
        return destOffset + 1 + sizeLength;
    }

    private static int sizeLength(final int size) {
        final int zeros = Integer.numberOfLeadingZeros(size);
        return 4 - (zeros / 8);
    }
}
