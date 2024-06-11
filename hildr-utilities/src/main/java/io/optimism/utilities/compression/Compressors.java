package io.optimism.utilities.compression;

import com.aayushatharva.brotli4j.Brotli4jLoader;
import com.aayushatharva.brotli4j.decoder.Decoder;
import com.aayushatharva.brotli4j.decoder.DecoderJNI;
import com.aayushatharva.brotli4j.decoder.DirectDecompress;
import com.aayushatharva.brotli4j.encoder.Encoder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * The type Compressors.
 *
 * @author grapebaba
 * @since 0.4.0
 */
public class Compressors {

    /**
     * The constant ZlibCM8.
     */
    public static int ZlibCM8 = 8;

    /**
     * The constant ZlibCM15.
     */
    public static int ZlibCM15 = 15;

    /**
     * The constant ChannelVersionBrotli.
     */
    public static byte ChannelVersionBrotli = 0x01;

    static {
        // Load the native library
        Brotli4jLoader.ensureAvailability();
    }

    /**
     * Instantiates a new Compressors.
     */
    public Compressors() {}

    /**
     * Brotli compress byte [ ].
     *
     * @param data the data
     * @return the byte [ ]
     * @throws IOException the io exception
     */
    public static byte[] brotliCompress(byte[] data) throws IOException {
        return Encoder.compress(data);
    }

    /**
     * Brotli decompress byte [ ].
     *
     * @param data the data
     * @return the byte [ ]
     * @throws IOException the io exception
     */
    public static byte[] brotliDecompress(byte[] data) throws IOException {
        DirectDecompress res = Decoder.decompress(data);
        if (res.getResultStatus() == DecoderJNI.Status.DONE) {
            return res.getDecompressedData();
        } else {
            throw new IOException("Decompression failed");
        }
    }

    /**
     * Zlib decompress byte [ ].
     *
     * @param data the data
     * @return the byte [ ]
     * @throws IOException the io exception
     */
    public static byte[] zlibDecompress(byte[] data) throws IOException {
        try {
            Inflater inflater = new Inflater();
            inflater.setInput(data);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
            byte[] buffer = new byte[1024];
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                if (count == 0) {
                    break;
                }
                outputStream.write(buffer, 0, count);
            }

            return outputStream.toByteArray();
        } catch (DataFormatException e) {
            throw new IOException(e);
        }
    }

    /**
     * Zlib compress byte [ ].
     *
     * @param data the data
     * @return the byte [ ]
     */
    public static byte[] zlibCompress(byte[] data) {
        java.util.zip.Deflater deflater = new java.util.zip.Deflater();
        deflater.setInput(data);
        deflater.finish();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
        byte[] buffer = new byte[1024];
        while (!deflater.finished()) {
            int count = deflater.deflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        deflater.end();
        return outputStream.toByteArray();
    }
}
