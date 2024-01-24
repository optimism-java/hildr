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
}
