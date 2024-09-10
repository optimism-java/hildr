package io.optimism.derive.stages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import io.netty.buffer.Unpooled;
import io.optimism.types.Epoch;
import io.optimism.types.L2BlockRef;
import io.optimism.types.RawSpanBatch;
import io.optimism.types.SingularBatch;
import io.optimism.types.SpanBatch;
import io.optimism.types.SpanBatchPayload;
import io.optimism.types.SpanBatchTxs;
import io.optimism.types.enums.BatchType;
import io.optimism.utilities.spanbatch.SpanBatchUtils;
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
 * The SpanBatchTest type.
 *
 * @author grapebaba
 * @since 0.2.4
 */
class SpanBatchTest {

    /**
     * Test span batch for batch interface.
     *
     * @throws IOException the io exception
     */
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
                Bytes.fromHexString(singularBatches1.getLast().epochHash())));
        assertTrue(spanBatch.checkParentHash(
                Bytes.fromHexString(singularBatches1.getFirst().parentHash())));
    }

    /**
     * Test empty span batch.
     */
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

    /**
     * Test span batch origin bits.
     *
     * @throws IOException the io exception
     */
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

    /**
     * Test span batch prefix.
     *
     * @throws IOException the io exception
     */
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

    /**
     * Test span batch max origin bits length.
     */
    @Test
    void testSpanBatchRelTimestamp() throws IOException {
        URL url = Resources.getResource("spanbatchoriginbits.txt");
        String origin = Resources.toString(url, Charsets.UTF_8);

        RawSpanBatch rawSpanBatch = new RawSpanBatch();
        rawSpanBatch.decode(Unpooled.wrappedBuffer(Numeric.hexStringToByteArray(origin)));

        byte[] relTimestamp = rawSpanBatch.spanbatchPrefix().encodeRelTimestamp();
        RawSpanBatch rawSpanBatch1 = new RawSpanBatch();
        rawSpanBatch1.spanbatchPrefix().decodeRelTimestamp(Unpooled.wrappedBuffer(relTimestamp));

        assertEquals(
                rawSpanBatch.spanbatchPrefix().relTimestamp(),
                rawSpanBatch1.spanbatchPrefix().relTimestamp());
    }

    @Test
    void testSpanBatchL1OriginNum() throws IOException {
        URL url = Resources.getResource("spanbatchoriginbits.txt");
        String origin = Resources.toString(url, Charsets.UTF_8);

        RawSpanBatch rawSpanBatch = new RawSpanBatch();
        rawSpanBatch.decode(Unpooled.wrappedBuffer(Numeric.hexStringToByteArray(origin)));

        byte[] l1OriginNum = rawSpanBatch.spanbatchPrefix().encodeL1OriginNum();
        RawSpanBatch rawSpanBatch1 = new RawSpanBatch();
        rawSpanBatch1.spanbatchPrefix().decodeL1OriginNum(Unpooled.wrappedBuffer(l1OriginNum));

        assertEquals(
                rawSpanBatch.spanbatchPrefix().l1OriginNum(),
                rawSpanBatch1.spanbatchPrefix().l1OriginNum());
    }

    @Test
    void testSpanBatchParentCheck() throws IOException {
        URL url = Resources.getResource("spanbatchoriginbits.txt");
        String origin = Resources.toString(url, Charsets.UTF_8);

        RawSpanBatch rawSpanBatch = new RawSpanBatch();
        rawSpanBatch.decode(Unpooled.wrappedBuffer(Numeric.hexStringToByteArray(origin)));

        byte[] parentCheck = rawSpanBatch.spanbatchPrefix().encodeParentCheck();
        assertEquals(20, parentCheck.length);

        RawSpanBatch rawSpanBatch1 = new RawSpanBatch();
        rawSpanBatch1.spanbatchPrefix().decodeParentCheck(Unpooled.wrappedBuffer(parentCheck));

        assertEquals(
                rawSpanBatch.spanbatchPrefix().parentCheck(),
                rawSpanBatch1.spanbatchPrefix().parentCheck());
    }

    @Test
    void testSpanBatchL1OriginCheck() throws IOException {
        URL url = Resources.getResource("spanbatchoriginbits.txt");
        String origin = Resources.toString(url, Charsets.UTF_8);

        RawSpanBatch rawSpanBatch = new RawSpanBatch();
        rawSpanBatch.decode(Unpooled.wrappedBuffer(Numeric.hexStringToByteArray(origin)));

        byte[] l1OriginCheck = rawSpanBatch.spanbatchPrefix().encodeL1OriginCheck();
        assertEquals(20, l1OriginCheck.length);

        RawSpanBatch rawSpanBatch1 = new RawSpanBatch();
        rawSpanBatch1.spanbatchPrefix().decodeL1OriginCheck(Unpooled.wrappedBuffer(l1OriginCheck));

        assertEquals(
                rawSpanBatch.spanbatchPrefix().l1OriginCheck(),
                rawSpanBatch1.spanbatchPrefix().l1OriginCheck());
    }

    @Test
    void testSpanBatchPayload() throws IOException {
        URL url = Resources.getResource("spanbatchoriginbits.txt");
        String origin = Resources.toString(url, Charsets.UTF_8);

        RawSpanBatch rawSpanBatch = new RawSpanBatch();
        rawSpanBatch.decode(Unpooled.wrappedBuffer(Numeric.hexStringToByteArray(origin)));
        rawSpanBatch.spanbatchPayload().txs().recoverV(BigInteger.valueOf(28));

        byte[] payload = rawSpanBatch.spanbatchPayload().encode();

        RawSpanBatch rawSpanBatch1 = new RawSpanBatch();
        rawSpanBatch1.spanbatchPayload().decode(Unpooled.wrappedBuffer(payload));
        rawSpanBatch1.spanbatchPayload().txs().recoverV(BigInteger.valueOf(28));

        assertEquals(rawSpanBatch.spanbatchPayload(), rawSpanBatch1.spanbatchPayload());
    }

    @Test
    void testSpanBatchBlockCount() throws IOException {
        URL url = Resources.getResource("spanbatchoriginbits.txt");
        String origin = Resources.toString(url, Charsets.UTF_8);
        RawSpanBatch rawSpanBatch = new RawSpanBatch();
        rawSpanBatch.decode(Unpooled.wrappedBuffer(Numeric.hexStringToByteArray(origin)));

        byte[] blockCount = rawSpanBatch.spanbatchPayload().encodeBlockCount();

        RawSpanBatch rawSpanBatch1 = new RawSpanBatch();
        rawSpanBatch1.spanbatchPayload().decodeBlockCount(Unpooled.wrappedBuffer(blockCount));

        assertEquals(
                rawSpanBatch.spanbatchPayload().blockCount(),
                rawSpanBatch1.spanbatchPayload().blockCount());
    }

    @Test
    void testSpanBatchBlockTxCounts() throws IOException {
        URL url = Resources.getResource("spanbatchoriginbits.txt");
        String origin = Resources.toString(url, Charsets.UTF_8);
        RawSpanBatch rawSpanBatch = new RawSpanBatch();
        rawSpanBatch.decode(Unpooled.wrappedBuffer(Numeric.hexStringToByteArray(origin)));

        byte[] blockTxCounts = rawSpanBatch.spanbatchPayload().encodeBlockTxCounts();

        RawSpanBatch rawSpanBatch1 = new RawSpanBatch();
        rawSpanBatch1
                .spanbatchPayload()
                .setBlockCount(rawSpanBatch.spanbatchPayload().blockCount());
        rawSpanBatch1.spanbatchPayload().decodeBlockTxCounts(Unpooled.wrappedBuffer(blockTxCounts));

        assertEquals(
                rawSpanBatch.spanbatchPayload().blockTxCounts(),
                rawSpanBatch1.spanbatchPayload().blockTxCounts());
    }

    @Test
    void testSpanBatchTxs() throws IOException {
        URL url = Resources.getResource("spanbatchoriginbits.txt");
        String origin = Resources.toString(url, Charsets.UTF_8);
        RawSpanBatch rawSpanBatch = new RawSpanBatch();
        rawSpanBatch.decode(Unpooled.wrappedBuffer(Numeric.hexStringToByteArray(origin)));
        rawSpanBatch.spanbatchPayload().txs().recoverV(BigInteger.valueOf(28));

        byte[] txs = rawSpanBatch.spanbatchPayload().encodeTxs();

        RawSpanBatch rawSpanBatch1 = new RawSpanBatch();
        rawSpanBatch1
                .spanbatchPayload()
                .setBlockTxCounts(rawSpanBatch.spanbatchPayload().blockTxCounts());
        rawSpanBatch1.spanbatchPayload().decodeTxs(Unpooled.wrappedBuffer(txs));
        rawSpanBatch1.spanbatchPayload().txs().recoverV(BigInteger.valueOf(28));

        assertEquals(
                rawSpanBatch.spanbatchPayload().txs(),
                rawSpanBatch1.spanbatchPayload().txs());
    }

    @Test
    void testSpanBatchDerive() throws IOException {
        BigInteger l2BlockTime = BigInteger.TWO;
        for (int originChangedBit = 0; originChangedBit < 2; originChangedBit++) {
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
            L2BlockRef l2BlockRef = new L2BlockRef(
                    singularBatches1.getFirst().parentHash(),
                    // random biginteger
                    RandomUtils.randomBigInt(),
                    // random string
                    RandomUtils.randomHex(),
                    // random biginteger
                    RandomUtils.randomBigInt(),
                    new Epoch(
                            RandomUtils.randomBigInt(), RandomUtils.randomHex(),
                            RandomUtils.randomBigInt(), RandomUtils.randomBigInt()),
                    RandomUtils.randomBigInt());

            BigInteger genesisTimeStamp =
                    BigInteger.ONE.add(singularBatches1.getFirst().timestamp()).subtract(BigInteger.valueOf(128));

            SpanBatch spanBatch = SpanBatch.newSpanBatch(singularBatches1);
            RawSpanBatch rawSpanBatch =
                    spanBatch.toRawSpanBatch(originChangedBit, genesisTimeStamp, BigInteger.valueOf(589));

            long blockCount = singularBatches1.size();
            SpanBatch spanBatchDerived = rawSpanBatch.derive(l2BlockTime, genesisTimeStamp, BigInteger.valueOf(589));

            assertEquals(
                    l2BlockRef.hash().substring(0, 40),
                    spanBatchDerived.getParentCheck().toHexString());
            assertEquals(
                    singularBatches1.getLast().epoch().hash().substring(0, 40),
                    spanBatchDerived.getL1OriginCheck().toHexString());
            assertEquals(blockCount, spanBatchDerived.getBlockCount());

            for (int i = 1; i < blockCount; i++) {
                assertEquals(
                        spanBatchDerived.getBatches().get(i).timestamp(),
                        spanBatchDerived.getBatches().get(i - 1).timestamp().add(l2BlockTime));
            }

            for (int i = 0; i < blockCount; i++) {
                assertEquals(
                        spanBatchDerived.getBatches().get(i).epochNum(),
                        singularBatches1.get(i).epochNum());
                assertEquals(
                        spanBatchDerived.getBatches().get(i).timestamp(),
                        singularBatches1.get(i).timestamp());
                assertEquals(
                        spanBatchDerived.getBatches().get(i).transactions(),
                        singularBatches1.get(i).transactions());
            }
        }
    }

    @Test
    void testSpanBatchMaxOriginBitsLength() {
        RawSpanBatch rawSpanBatch = new RawSpanBatch();
        rawSpanBatch.spanbatchPayload().setBlockCount(Long.MAX_VALUE);

        assertThrows(
                RuntimeException.class,
                () -> rawSpanBatch.spanbatchPayload().decodeOriginBits(Unpooled.wrappedBuffer(new byte[] {})));
    }

    /**
     * Test span batch max block count.
     *
     * @throws IOException the io exception
     */
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

    /**
     * Test span batch max block tx count.
     *
     * @throws IOException the io exception
     */
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

    /**
     * Test span batch total block tx count not overflow.
     *
     * @throws IOException the io exception
     */
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
