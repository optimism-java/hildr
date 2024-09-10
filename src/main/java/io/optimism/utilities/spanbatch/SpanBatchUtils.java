package io.optimism.utilities.spanbatch;

import io.netty.buffer.ByteBuf;
import java.math.BigInteger;
import org.web3j.utils.Numeric;

/**
 * The type SpanBatchUtils.
 *
 * @author grapebaba
 * @since 0.2.4
 */
public class SpanBatchUtils {

    /**
     * The constant MaxSpanBatchSize.
     */
    public static final long MaxSpanBatchSize = 10000000L;

    /**
     * Instantiates a new Span batch utils.
     */
    public SpanBatchUtils() {}

    /**
     * Decode span batch bits big integer.
     *
     * @param source    the source
     * @param bitLength the bit length
     * @return the big integer
     */
    public static BigInteger decodeSpanBatchBits(ByteBuf source, int bitLength) {
        var bufLen = bitLength / 8;
        if (bitLength % 8 != 0) {
            bufLen++;
        }
        if (bufLen > MaxSpanBatchSize) {
            throw new RuntimeException("span batch size limit reached");
        }
        byte[] buf = new byte[(int) bufLen];
        try {
            source.readBytes(buf);
        } catch (Exception e) {
            throw new RuntimeException("read error");
        }
        var res = Numeric.toBigInt(buf);
        if (res.bitLength() > bitLength) {
            throw new RuntimeException("invalid bit length");
        }
        return res;
    }

    /**
     * Encode span batch bits byte [ ].
     *
     * @param bitLength the bit length
     * @param bits      the bits
     * @return the byte [ ]
     */
    public static byte[] encodeSpanBatchBits(int bitLength, BigInteger bits) {
        if (bits.bitLength() > bitLength) {
            throw new RuntimeException(
                    "bitfield is larger than bitLength: %d > %d".formatted(bits.bitLength(), bitLength));
        }

        var bufLen = bitLength / 8;
        if (bitLength % 8 != 0) {
            bufLen++;
        }
        if (bufLen > MaxSpanBatchSize) {
            throw new RuntimeException("span batch size limit reached");
        }
        return Numeric.toBytesPadded(bits, bufLen);
    }

    /**
     * Reads an up to 64 bit long varint from the current position of the
     * given ByteBuffer and returns the decoded value as long.
     *
     * <p>The position of the buffer is advanced to the first byte after the
     * decoded varint.
     *
     * @param src the ByteBuffer to get the var int from
     * @return The integer value of the decoded long varint
     */
    public static long getVarLong(ByteBuf src) {
        long tmp;
        if ((tmp = src.readByte()) >= 0) {
            return tmp;
        }
        long result = tmp & 0x7f;
        if ((tmp = src.readByte()) >= 0) {
            result |= tmp << 7;
        } else {
            result |= (tmp & 0x7f) << 7;
            if ((tmp = src.readByte()) >= 0) {
                result |= tmp << 14;
            } else {
                result |= (tmp & 0x7f) << 14;
                if ((tmp = src.readByte()) >= 0) {
                    result |= tmp << 21;
                } else {
                    result |= (tmp & 0x7f) << 21;
                    if ((tmp = src.readByte()) >= 0) {
                        result |= tmp << 28;
                    } else {
                        result |= (tmp & 0x7f) << 28;
                        if ((tmp = src.readByte()) >= 0) {
                            result |= tmp << 35;
                        } else {
                            result |= (tmp & 0x7f) << 35;
                            if ((tmp = src.readByte()) >= 0) {
                                result |= tmp << 42;
                            } else {
                                result |= (tmp & 0x7f) << 42;
                                if ((tmp = src.readByte()) >= 0) {
                                    result |= tmp << 49;
                                } else {
                                    result |= (tmp & 0x7f) << 49;
                                    if ((tmp = src.readByte()) >= 0) {
                                        result |= tmp << 56;
                                    } else {
                                        result |= (tmp & 0x7f) << 56;
                                        result |= ((long) src.readByte()) << 63;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Encodes a long integer in a variable-length encoding, 7 bits per byte, to a
     * ByteBuffer sink.
     *
     * @param v    the value to encode
     * @param sink the ByteBuffer to add the encoded value
     */
    public static void putVarLong(long v, ByteBuf sink) {
        while (true) {
            int bits = ((int) v) & 0x7f;
            v >>>= 7;
            if (v == 0) {
                sink.writeByte((byte) bits);
                return;
            }
            sink.writeByte((byte) (bits | 0x80));
        }
    }
}
