package io.optimism.utilities.blob;

import org.apache.commons.lang3.ArrayUtils;

/**
 * The BlobCodec class.
 *
 * @author grapebaba
 * @since 0.2.6
 */
public final class BlobCodec {

    private static final int BLOB_SIZE = 4096 * 32;
    private static final int MAX_BLOB_DATA_SIZE = (4 * 31 + 3) * 1024 - 4;
    private static final int ENCODING_VERSION = 0;
    private static final int VERSION_OFFSET = 1; // offset of the version byte in the blob encoding
    private static final int ROUNDS = 1024;

    /**
     * Instantiates a new Blob codec.
     */
    public BlobCodec() {}

    /**
     * Decode blob.
     *
     * @param blob the blob
     * @return the byte[]
     */
    public static byte[] decode(byte[] blob) {
        // check the version
        if (blob[VERSION_OFFSET] != ENCODING_VERSION) {
            throw new IllegalArgumentException("invalid encoding version: expected version %d, got %d"
                    .formatted(blob[VERSION_OFFSET], ENCODING_VERSION));
        }

        // decode the 3-byte big-endian length value into a 4-byte integer

        int outputLength = (blob[2] & 0xFF) << 16 | (blob[3] & 0xFF) << 8 | (blob[4] & 0xFF);
        if (outputLength > MAX_BLOB_DATA_SIZE) {
            throw new IllegalArgumentException("invalid length for blob: output length %d exceeds maximum %d"
                    .formatted(outputLength, MAX_BLOB_DATA_SIZE));
        }

        // round 0 is special cased to copy only the remaining 27 bytes of the first field element into
        // the output due to version/length encoding already occupying its first 5 bytes.
        byte[] output = new byte[MAX_BLOB_DATA_SIZE];
        System.arraycopy(blob, 5, output, 0, 27);

        // now process remaining 3 field elements to complete round 0
        int opos = 28; // current position into output buffer
        int ipos = 32; // current position into the input blob

        byte[] encoded = new byte[4];
        encoded[0] = blob[0];
        for (int i = 1; i < 4; i++) {
            FieldElementMeta fieldElementMeta = decodeFieldElement(blob, output, opos, ipos);
            encoded[i] = fieldElementMeta.first;
            opos = fieldElementMeta.opos;
            ipos = fieldElementMeta.ipos;
        }

        opos = reassembleBytes(opos, encoded, output);

        // in each remaining round we decode 4 field elements (128 bytes) of the input into 127 bytes
        // of output
        for (int i = 1; i < ROUNDS && opos < outputLength; i++) {
            for (int j = 0; j < 4; j++) {
                // save the first byte of each field element for later re-assembly
                FieldElementMeta fieldElementMeta = decodeFieldElement(blob, output, opos, ipos);
                encoded[j] = fieldElementMeta.first;
                opos = fieldElementMeta.opos;
                ipos = fieldElementMeta.ipos;
            }
            opos = reassembleBytes(opos, encoded, output);
        }

        for (int i = outputLength; i < output.length; i++) {
            if (output[i] != 0) {
                throw new IllegalArgumentException(
                        "fe=%d: non-zero data encountered where field element should be empty".formatted(opos / 32));
            }
        }

        output = ArrayUtils.subarray(output, 0, outputLength);
        for (; ipos < BLOB_SIZE; ipos++) {
            if (blob[ipos] != 0) {
                throw new IllegalArgumentException(
                        "pos=%d: non-zero data encountered where blob should be empty".formatted(ipos));
            }
        }

        return output;
    }

    /**
     * The type Field element meta.
     *
     * @param first the first
     * @param opos  the opos
     * @param ipos  the ipos
     */
    public record FieldElementMeta(byte first, int opos, int ipos) {}

    private static FieldElementMeta decodeFieldElement(byte[] blob, byte[] output, int opos, int ipos) {
        // two highest order bits of the first byte of each field element should always be 0
        if ((blob[ipos] & 0b1100_0000) != 0) {
            throw new IllegalArgumentException("invalid field element: field element: %d".formatted(ipos));
        }

        System.arraycopy(blob, ipos + 1, output, opos, 31);
        return new FieldElementMeta(blob[ipos], opos + 32, ipos + 32);
    }

    private static int reassembleBytes(int opos, byte[] encoded, byte[] output) {
        opos--;
        byte x = (byte) ((encoded[0] & 0b0011_1111) | ((encoded[1] & 0b0011_0000) << 2));
        byte y = (byte) ((encoded[1] & 0b0000_1111) | ((encoded[3] & 0b0000_1111) << 4));
        byte z = (byte) ((encoded[2] & 0b0011_1111) | ((encoded[3] & 0b0011_0000) << 2));
        // put the re-assembled bytes in their appropriate output locations
        output[opos - 32] = z;
        output[opos - (32 * 2)] = y;
        output[opos - (32 * 3)] = x;
        return opos;
    }
}
