package io.optimism.utilities.derive.stages;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.tuweni.bytes.Bytes;
import org.junit.jupiter.api.Test;
import org.web3j.rlp.RlpDecoder;
import org.web3j.rlp.RlpList;
import org.web3j.utils.Numeric;

/**
 * Created by IntelliJ IDEA.
 * Author: kaichen
 * Date: 2024/1/24
 * Time: 17:51
 */
class SpanBatchTest {

    @Test
    void testSpanBatchForBatchInterface() throws IOException {
        URL url = Resources.getResource("spanbatchfromsingular.txt");
        List<String> singularBatches = Resources.readLines(url, Charsets.UTF_8);

        List<SingularBatch> singularBatches1 = singularBatches.stream()
                .map(singularBatch -> {
                    RlpList rlpBatchData = (RlpList) RlpDecoder.decode(Numeric.hexStringToByteArray(singularBatch))
                            .getValues()
                            .getFirst();
                    return SingularBatch.decode(rlpBatchData);
                })
                .toList();

        SpanBatch spanBatch = SpanBatch.newSpanBatch(singularBatches1);
        assertEquals(spanBatch.getBatchType(), BatchType.SPAN_BATCH_TYPE);
        assertEquals(spanBatch.getTimestamp(), singularBatches1.getFirst().getTimestamp());
        assertEquals(spanBatch.getStartEpochNum(), singularBatches1.getFirst().getEpochNum());

        assertTrue(spanBatch.checkOriginHash(
                Bytes.fromHexString(singularBatches1.getLast().epochHash().substring(0, 40))));
        assertTrue(spanBatch.checkParentHash(
                Bytes.fromHexString(singularBatches1.getFirst().parentHash().substring(0, 40))));
    }

    @Test
    void testEmptySpanBatch() {
        SpanBatchTxs txs = SpanBatchTxs.newSpanBatchTxs(new ArrayList<>(), BigInteger.valueOf(28));
        RawSpanBatch rawSpanBatch = new RawSpanBatch();
        rawSpanBatch.spanbatchPrefix().setL1OriginNum(new BigInteger("8212862268335983754"));
        rawSpanBatch.spanbatchPrefix().setRelTimestamp(new BigInteger("464385576"));
        rawSpanBatch
                .spanbatchPrefix()
                .setParentCheck(Bytes.fromHexString("0x6ee96cb75044e6705d645aa57ed8fc0e437cb981"));
        rawSpanBatch
                .spanbatchPrefix()
                .setL1OriginCheck(Bytes.fromHexString("0x6894f802c9ee9bdb6a09e30c2eef5ff43513e741"));

        rawSpanBatch.spanbatchPayload().setBlockCount(0);
        rawSpanBatch.spanbatchPayload().setOriginBits(BigInteger.ZERO);
        rawSpanBatch.spanbatchPayload().setTxs(txs);

        byte[] blockCount = rawSpanBatch.spanbatchPayload().encodeBlockCount();

        RawSpanBatch rawSpanBatch1 = new RawSpanBatch();

        assertThrows(
                RuntimeException.class,
                () -> rawSpanBatch1.spanbatchPayload().decodeBlockCount(Unpooled.wrappedBuffer(blockCount)),
                "block count cannot be zero");
    }

    @Test
    void testSpanBatchOriginBits() throws IOException {
        URL url = Resources.getResource("spanbatchoriginbits.txt");
        String origin = Resources.toString(url, Charsets.UTF_8);

        RawSpanBatch rawSpanBatch = new RawSpanBatch();
        rawSpanBatch.decode(Unpooled.wrappedBuffer(Numeric.hexStringToByteArray(origin)));

        long blockCount = rawSpanBatch.spanbatchPayload().blockCount();

        byte[] originBits = rawSpanBatch.spanbatchPayload().encodeOriginBits();

        long originBitBufferLen = blockCount / 8;
        if (blockCount % 8 != 0) {
            originBitBufferLen++;
        }
        assertEquals(originBitBufferLen, originBits.length);

        RawSpanBatch rawSpanBatch1 = new RawSpanBatch();
        rawSpanBatch1.spanbatchPayload().setBlockCount(blockCount);
        rawSpanBatch1.spanbatchPayload().decodeOriginBits(Unpooled.wrappedBuffer(originBits));

        assertEquals(
                rawSpanBatch.spanbatchPayload().originBits(),
                rawSpanBatch1.spanbatchPayload().originBits());
    }

    @Test
    void testSpanBatchPrefix() throws IOException {
        URL url = Resources.getResource("spanbatchoriginbits.txt");
        String origin = Resources.toString(url, Charsets.UTF_8);

        RawSpanBatch rawSpanBatch = new RawSpanBatch();
        rawSpanBatch.decode(Unpooled.wrappedBuffer(Numeric.hexStringToByteArray(origin)));

        rawSpanBatch.setSpanbatchPayload(new SpanBatchPayload());

        byte[] prefix = rawSpanBatch.spanbatchPrefix().encode();

        RawSpanBatch rawSpanBatch1 = new RawSpanBatch();
        rawSpanBatch1.spanbatchPrefix().decode(Unpooled.wrappedBuffer(prefix));

        assertEquals(rawSpanBatch, rawSpanBatch1);
    }

    @Test
    void testSpanBatchMaxOriginBitsLength() {
        RawSpanBatch rawSpanBatch = new RawSpanBatch();
        rawSpanBatch.spanbatchPayload().setBlockCount(Long.MAX_VALUE);

        assertThrows(
                RuntimeException.class,
                () -> rawSpanBatch.spanbatchPayload().decodeOriginBits(Unpooled.wrappedBuffer(new byte[] {})));
    }

    @Test
    void testSpanBatchMaxBlockCount() throws IOException {
        URL url = Resources.getResource("spanbatchoriginbits.txt");
        String origin = Resources.toString(url, Charsets.UTF_8);

        RawSpanBatch rawSpanBatch = new RawSpanBatch();
        rawSpanBatch.decode(Unpooled.wrappedBuffer(Numeric.hexStringToByteArray(origin)));

        rawSpanBatch.spanbatchPayload().setBlockCount(Long.MAX_VALUE);

        byte[] encodedBlockCount = rawSpanBatch.spanbatchPayload().encodeBlockCount();

        RawSpanBatch rawSpanBatch1 = new RawSpanBatch();

        assertThrows(
                RuntimeException.class,
                () -> rawSpanBatch1.spanbatchPayload().decodeBlockCount(Unpooled.wrappedBuffer(encodedBlockCount)),
                "span batch size limit reached");
    }

    @Test
    void testSpanBatchMaxBlockTxCount() throws IOException {
        URL url = Resources.getResource("spanbatchoriginbits.txt");
        String origin = Resources.toString(url, Charsets.UTF_8);

        RawSpanBatch rawSpanBatch = new RawSpanBatch();
        rawSpanBatch.decode(Unpooled.wrappedBuffer(Numeric.hexStringToByteArray(origin)));

        rawSpanBatch.spanbatchPayload().blockTxCounts().set(0, Long.MAX_VALUE);

        byte[] encodedBlockTxCounts = rawSpanBatch.spanbatchPayload().encodeBlockTxCounts();

        RawSpanBatch rawSpanBatch1 = new RawSpanBatch();
        rawSpanBatch1
                .spanbatchPayload()
                .setBlockCount(rawSpanBatch.spanbatchPayload().blockCount());

        assertThrows(
                RuntimeException.class,
                () -> rawSpanBatch1
                        .spanbatchPayload()
                        .decodeBlockTxCounts(Unpooled.wrappedBuffer(encodedBlockTxCounts)),
                "span batch size limit reached");
    }

    @Test
    void testSpanBatchTotalBlockTxCountNotOverflow() throws IOException {
        URL url = Resources.getResource("spanbatchoriginbits.txt");
        String origin = Resources.toString(url, Charsets.UTF_8);

        RawSpanBatch rawSpanBatch = new RawSpanBatch();
        rawSpanBatch.decode(Unpooled.wrappedBuffer(Numeric.hexStringToByteArray(origin)));

        rawSpanBatch.spanbatchPayload().blockTxCounts().set(0, SpanBatchUtils.MaxSpanBatchSize - 1);
        rawSpanBatch.spanbatchPayload().blockTxCounts().set(1, SpanBatchUtils.MaxSpanBatchSize - 1);

        byte[] encodedBlockTxCounts = rawSpanBatch.spanbatchPayload().encodeBlockTxCounts();

        RawSpanBatch rawSpanBatch1 = new RawSpanBatch();
        rawSpanBatch1
                .spanbatchPayload()
                .setBlockTxCounts(rawSpanBatch.spanbatchPayload().blockTxCounts());

        assertThrows(
                RuntimeException.class,
                () -> rawSpanBatch1.spanbatchPayload().decodeTxs(Unpooled.wrappedBuffer(encodedBlockTxCounts)),
                "span batch size limit reached");
    }
}
