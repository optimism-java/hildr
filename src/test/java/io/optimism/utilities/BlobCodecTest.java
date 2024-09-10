package io.optimism.utilities;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import io.optimism.utilities.blob.BlobCodec;
import java.io.IOException;
import java.net.URL;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.web3j.utils.Numeric;

/**
 * The type Blob codec test.
 *
 * @author grapebaba
 * @since 0.2.6
 */
class BlobCodecTest {

    @Test
    @DisplayName("test decode sepolia blob data")
    void testSepoliaBlobCodec() throws IOException {
        URL url = Resources.getResource("blob9.txt");
        String origin = Resources.toString(url, Charsets.UTF_8);

        URL decodeUrl = Resources.getResource("blob9_decode.txt");
        String decodedData = Resources.toString(decodeUrl, Charsets.UTF_8);

        byte[] decode = BlobCodec.decode(Numeric.hexStringToByteArray(origin));
        assertEquals(decodedData, Numeric.toHexString(decode));
    }

    @Test
    @DisplayName("test decode blob.")
    void testBlobCodec() throws IOException {
        URL url = Resources.getResource("blob1.txt");
        String origin = Resources.toString(url, Charsets.UTF_8);

        assertEquals(
                "this is a test of blob encoding/decoding",
                new String(BlobCodec.decode(Numeric.hexStringToByteArray(origin)), Charsets.UTF_8));

        URL url1 = Resources.getResource("blob2.txt");
        String origin1 = Resources.toString(url1, Charsets.UTF_8);
        assertEquals("short", new String(BlobCodec.decode(Numeric.hexStringToByteArray(origin1)), Charsets.UTF_8));

        URL url2 = Resources.getResource("blob3.txt");
        String origin2 = Resources.toString(url2, Charsets.UTF_8);
        assertEquals("\u0000", new String(BlobCodec.decode(Numeric.hexStringToByteArray(origin2)), Charsets.UTF_8));

        URL url3 = Resources.getResource("blob4.txt");
        String origin3 = Resources.toString(url3, Charsets.UTF_8);
        assertEquals(
                "\u0000\u0001\u0000",
                new String(BlobCodec.decode(Numeric.hexStringToByteArray(origin3)), Charsets.UTF_8));

        URL url4 = Resources.getResource("blob5.txt");
        String origin4 = Resources.toString(url4, Charsets.UTF_8);
        assertEquals(
                "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000",
                new String(BlobCodec.decode(Numeric.hexStringToByteArray(origin4)), Charsets.UTF_8));

        URL url5 = Resources.getResource("blob6.txt");
        String origin5 = Resources.toString(url5, Charsets.UTF_8);
        assertEquals(
                "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000",
                new String(BlobCodec.decode(Numeric.hexStringToByteArray(origin5)), Charsets.UTF_8));

        URL url6 = Resources.getResource("blob7.txt");
        String origin6 = Resources.toString(url6, Charsets.UTF_8);
        assertEquals(
                "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000",
                new String(BlobCodec.decode(Numeric.hexStringToByteArray(origin6)), Charsets.UTF_8));

        URL url7 = Resources.getResource("blob8.txt");
        String origin7 = Resources.toString(url7, Charsets.UTF_8);
        assertEquals("", new String(BlobCodec.decode(Numeric.hexStringToByteArray(origin7)), Charsets.UTF_8));
    }
}
