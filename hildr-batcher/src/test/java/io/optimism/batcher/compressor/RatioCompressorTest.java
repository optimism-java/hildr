package io.optimism.batcher.compressor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import org.junit.jupiter.api.Test;

/**
 * Ratio compressor test case.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
class RatioCompressorTest {

    @Test
    public void testCompressor() throws DataFormatException, IOException {
        var cprsConfig = new CompressorConfig(10_000, 1, "0.4", Compressors.RatioKind);
        var cprr = new RatioCompressor(cprsConfig);

        final String source = "test compressor data 123456789987654321";
        byte[] sourceBytes = source.getBytes(StandardCharsets.UTF_8);
        cprr.write(sourceBytes);
        byte[] tmp = new byte[100];
        int n = cprr.read(tmp);
        cprr.close();

        Inflater inflater = new Inflater();
        inflater.setInput(Arrays.copyOf(tmp, n));

        byte[] uncompressed = new byte[sourceBytes.length];
        inflater.inflate(uncompressed);
        inflater.finished();
        inflater.end();

        assertEquals(source, new String(uncompressed, StandardCharsets.UTF_8));
    }
}
