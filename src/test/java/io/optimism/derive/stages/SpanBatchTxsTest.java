package io.optimism.derive.stages;

import static io.optimism.types.SpanBatchTx.SIGNATURE_ALGORITHM;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import io.netty.buffer.Unpooled;
import io.optimism.types.SpanBatchTxs;
import io.optimism.utilities.spanbatch.SpanBatchUtils;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.crypto.SECPSignature;
import org.hyperledger.besu.datatypes.TransactionType;
import org.hyperledger.besu.datatypes.Wei;
import org.hyperledger.besu.ethereum.core.Transaction;
import org.hyperledger.besu.ethereum.core.encoding.EncodingContext;
import org.hyperledger.besu.ethereum.core.encoding.TransactionEncoder;
import org.hyperledger.besu.ethereum.rlp.BytesValueRLPInput;
import org.hyperledger.besu.ethereum.rlp.RLPInput;
import org.junit.jupiter.api.Test;
import org.web3j.utils.Numeric;

/**
 * The type SpanBatchTxsTest.
 *
 * @author grapebaba
 * @since 0.2.4
 */
public class SpanBatchTxsTest {
    /**
     * Decode and encode span batch bits.
     */
    @Test
    void decodeAndEncodeSpanBatchBits() {
        String test =
                "0xaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
        BigInteger res = SpanBatchUtils.decodeSpanBatchBits(
                Unpooled.wrappedBuffer(Bytes.fromHexString(test).toArray()), 544);
        var res1 = SpanBatchUtils.encodeSpanBatchBits(544, res);
        assertArrayEquals(Numeric.hexStringToByteArray(test), res1);
    }

    /**
     * Decode and encode tx sigs.
     *
     * @throws IOException the io exception
     */
    @Test
    void decodeAndEncodeTxSigs() throws IOException {
        URL url = Resources.getResource("txsigs.txt");
        String test = Resources.toString(url, Charsets.UTF_8);

        SpanBatchTxs txs = new SpanBatchTxs();
        txs.setTotalBlockTxCount(1887L);
        txs.decodeTxSigsRS(Unpooled.wrappedBuffer(Numeric.hexStringToByteArray(test)));
        var res = txs.encodeTxSigsRS();
        assertArrayEquals(Numeric.hexStringToByteArray(test), res);
    }

    /**
     * Decode and encode tx nonces.
     *
     * @throws IOException the io exception
     */
    @Test
    void decodeAndEncodeTxNonces() throws IOException {
        URL url = Resources.getResource("txnonces.txt");
        String test = Resources.toString(url, Charsets.UTF_8);

        SpanBatchTxs txs = new SpanBatchTxs();
        txs.setTotalBlockTxCount(1410L);
        txs.decodeTxNonces(Unpooled.wrappedBuffer(Numeric.hexStringToByteArray(test)));
        var res = txs.encodeTxNonces();
        assertArrayEquals(Numeric.hexStringToByteArray(test), res);
    }

    /**
     * Decode and encode tx gases.
     *
     * @throws IOException the io exception
     */
    @Test
    void decodeAndEncodeTxGases() throws IOException {
        URL url = Resources.getResource("txgases.txt");
        String test = Resources.toString(url, Charsets.UTF_8);

        SpanBatchTxs txs = new SpanBatchTxs();
        txs.setTotalBlockTxCount(149L);
        txs.decodeTxGases(Unpooled.wrappedBuffer(Numeric.hexStringToByteArray(test)));
        var res = txs.encodeTxGases();
        assertArrayEquals(Numeric.hexStringToByteArray(test), res);
    }

    /**
     * Decode and encode tx tos.
     *
     * @throws IOException the io exception
     */
    @Test
    void decodeAndEncodeTxTos() throws IOException {
        URL url = Resources.getResource("txtos.txt");
        String test = Resources.toString(url, Charsets.UTF_8);

        SpanBatchTxs txs = new SpanBatchTxs();
        txs.setTotalBlockTxCount(1625L);
        txs.setContractCreationBits(
                Numeric.toBigInt(
                        "0x93241abca2cd6bcf485b883fab54d2458d6ad00d09e56086e0cff4828c1c38166ccac0e9d1c32ee16ab3796a7d8433df02ce25b674efa6362cd044e7d5bb74cbc6ae2ad58b0eb7f8e8afbe13fd47470d2187872848468266ebd4914c1aefaacf1642381f001f0497261759e95f0ae2a5ac8fdd2883c9c7ca461178ded2a503661a491d1242f7236a3465a13c740ebabf5f927ca37812f3e22093345f4db5f30506cbbfac29a5fa8bdbb4d2690008701bad6b6ca8b8b5d17b6eb9e7487fcf07524564e36f5c1cbe15991e8e"));
        txs.decodeTxTos(Unpooled.wrappedBuffer(Numeric.hexStringToByteArray(test)));
        var res = txs.encodeTxTos();
        assertArrayEquals(Numeric.hexStringToByteArray(test), res);
    }

    /**
     * Decode and encode tx datas.
     *
     * @throws IOException the io exception
     */
    @Test
    void decodeAndEncodeTxDatas() throws IOException {
        URL url = Resources.getResource("txdatas.txt");
        String test = Resources.toString(url, Charsets.UTF_8);

        SpanBatchTxs txs = new SpanBatchTxs();
        txs.setTotalBlockTxCount(727L);
        txs.decodeTxDatas(Unpooled.wrappedBuffer(Numeric.hexStringToByteArray(test)));
        var res = txs.encodeTxDatas();
        assertArrayEquals(Numeric.hexStringToByteArray(test), res);
    }

    /**
     * Decode and encode contract creation bits.
     *
     * @throws IOException the io exception
     */
    @Test
    void decodeAndEncodeContractCreationBits() throws IOException {
        URL url = Resources.getResource("contractcreationbits.txt");
        String test = Resources.toString(url, Charsets.UTF_8);

        SpanBatchTxs txs = new SpanBatchTxs();
        txs.setTotalBlockTxCount(1804L);
        txs.decodeContractCreationBits(Unpooled.wrappedBuffer(Numeric.hexStringToByteArray(test)));
        var res = txs.encodeContractCreationBits();
        assertEquals(910L, txs.contractCreationCount());
        assertArrayEquals(Numeric.hexStringToByteArray(test), res);
    }

    /**
     * Decode and encode y parity bits.
     *
     * @throws IOException the io exception
     */
    @Test
    void decodeAndEncodeYParityBits() throws IOException {
        URL url = Resources.getResource("yparitybits.txt");
        String test = Resources.toString(url, Charsets.UTF_8);

        SpanBatchTxs txs = new SpanBatchTxs();
        txs.setTotalBlockTxCount(1088L);
        txs.decodeYParityBits(Unpooled.wrappedBuffer(Numeric.hexStringToByteArray(test)));
        var res = txs.encodeYParityBits();
        assertArrayEquals(Numeric.hexStringToByteArray(test), res);
    }

    /**
     * Decode and encode protected bits.
     *
     * @throws IOException the io exception
     */
    @Test
    void decodeAndEncodeProtectedBits() throws IOException {
        URL url = Resources.getResource("protectedbits.txt");
        String test = Resources.toString(url, Charsets.UTF_8);

        SpanBatchTxs txs = new SpanBatchTxs();
        txs.setTotalLegacyTxCount(544L);
        txs.decodeProtectedBits(Unpooled.wrappedBuffer(Numeric.hexStringToByteArray(test)));
        var res = txs.encodeProtectedBits();
        assertArrayEquals(Numeric.hexStringToByteArray(test), res);
    }

    /**
     * Decode and encode.
     *
     * @throws IOException the io exception
     */
    @Test
    void decodeAndEncode() throws IOException {
        URL url = Resources.getResource("spanbatchtxs.txt");
        String test = Resources.toString(url, Charsets.UTF_8);

        SpanBatchTxs txs = new SpanBatchTxs();
        txs.setTotalBlockTxCount(946L);
        txs.decode(Unpooled.wrappedBuffer(Numeric.hexStringToByteArray(test)));
        var res = txs.encode();
        assertArrayEquals(Numeric.hexStringToByteArray(test), res);
    }

    /**
     * Recovery v unprotected.
     *
     * @throws IOException the io exception
     */
    @Test
    void recoveryVUnprotected() throws IOException {
        SpanBatchTxs txs = new SpanBatchTxs();
        txs.setTotalBlockTxCount(37L);

        URL url = Resources.getResource("recunprotectedsigs.txt");
        String recunprotectedsigs = Resources.toString(url, Charsets.UTF_8);
        txs.decodeTxSigsRS(Unpooled.wrappedBuffer(Numeric.hexStringToByteArray(recunprotectedsigs)));

        URL url2 = Resources.getResource("recunprotectedyparity.txt");
        String recunprotectedyparity = Resources.toString(url2, Charsets.UTF_8);
        txs.decodeYParityBits(Unpooled.wrappedBuffer(Numeric.hexStringToByteArray(recunprotectedyparity)));

        URL url3 = Resources.getResource("recunprotectedprotectedbits.txt");
        String recunprotectedprotectedbits = Resources.toString(url3, Charsets.UTF_8);
        txs.decodeProtectedBits(Unpooled.wrappedBuffer(Numeric.hexStringToByteArray(recunprotectedprotectedbits)));
        List<TransactionType> txTypes = new ArrayList<>();
        for (int i = 0; i < txs.getTotalBlockTxCount(); i++) {
            txTypes.add(TransactionType.FRONTIER);
        }
        txs.setTxTypes(txTypes);
        txs.recoverV(BigInteger.valueOf(108L));
        long[] vs = new long[] {
            27, 28, 27, 27, 28, 28, 28, 27, 28, 27, 28, 28, 28, 27, 28, 27, 27, 28, 28, 28, 27, 28, 28, 28, 27, 27, 28,
            27, 28, 28, 27, 28, 28, 27, 28, 27, 27
        };
        long[] res = txs.getTxSigs().stream()
                .map(spanBatchSignature -> spanBatchSignature.v().longValue())
                .mapToLong(Long::longValue)
                .toArray();

        assertArrayEquals(vs, res);
    }

    /**
     * Recovery v legacy.
     *
     * @throws IOException the io exception
     */
    @Test
    void recoveryVLegacy() throws IOException {
        SpanBatchTxs txs = new SpanBatchTxs();
        txs.setTotalBlockTxCount(37L);
        txs.setTotalLegacyTxCount(37L);

        URL url = Resources.getResource("reclegcysigs.txt");
        String recunprotectedsigs = Resources.toString(url, Charsets.UTF_8);
        txs.decodeTxSigsRS(Unpooled.wrappedBuffer(Numeric.hexStringToByteArray(recunprotectedsigs)));

        URL url2 = Resources.getResource("reclegcyyparity.txt");
        String recunprotectedyparity = Resources.toString(url2, Charsets.UTF_8);
        txs.decodeYParityBits(Unpooled.wrappedBuffer(Numeric.hexStringToByteArray(recunprotectedyparity)));

        URL url3 = Resources.getResource("reclegcyprotectedbits.txt");
        String recunprotectedprotectedbits = Resources.toString(url3, Charsets.UTF_8);
        txs.decodeProtectedBits(Unpooled.wrappedBuffer(Numeric.hexStringToByteArray(recunprotectedprotectedbits)));
        List<TransactionType> txTypes = new ArrayList<>();
        for (int i = 0; i < txs.getTotalBlockTxCount(); i++) {
            txTypes.add(TransactionType.FRONTIER);
        }
        txs.setTxTypes(txTypes);
        txs.recoverV(BigInteger.valueOf(108L));
        long[] vs = new long[] {
            251, 252, 251, 251, 251, 251, 252, 251, 252, 251, 251, 252, 252, 251, 252, 251, 251, 251, 251, 251, 252,
            252, 252, 251, 252, 251, 252, 251, 252, 252, 252, 252, 251, 251, 252, 252, 251
        };
        long[] res = txs.getTxSigs().stream()
                .map(spanBatchSignature -> spanBatchSignature.v().longValue())
                .mapToLong(Long::longValue)
                .toArray();

        assertArrayEquals(vs, res);
    }

    /**
     * Recovery v access list.
     *
     * @throws IOException the io exception
     */
    @Test
    void recoveryVAccessList() throws IOException {
        SpanBatchTxs txs = new SpanBatchTxs();
        txs.setTotalBlockTxCount(37L);

        URL url = Resources.getResource("recaccsigs.txt");
        String recunprotectedsigs = Resources.toString(url, Charsets.UTF_8);
        txs.decodeTxSigsRS(Unpooled.wrappedBuffer(Numeric.hexStringToByteArray(recunprotectedsigs)));

        URL url2 = Resources.getResource("recaccyparity.txt");
        String recunprotectedyparity = Resources.toString(url2, Charsets.UTF_8);
        txs.decodeYParityBits(Unpooled.wrappedBuffer(Numeric.hexStringToByteArray(recunprotectedyparity)));

        URL url3 = Resources.getResource("recaccprotectedbits.txt");
        String recunprotectedprotectedbits = Resources.toString(url3, Charsets.UTF_8);
        txs.decodeProtectedBits(Unpooled.wrappedBuffer(Numeric.hexStringToByteArray(recunprotectedprotectedbits)));
        List<TransactionType> txTypes = new ArrayList<>();
        for (int i = 0; i < txs.getTotalBlockTxCount(); i++) {
            txTypes.add(TransactionType.ACCESS_LIST);
        }
        txs.setTxTypes(txTypes);
        txs.recoverV(BigInteger.valueOf(108L));
        long[] vs = new long[] {
            1, 1, 1, 0, 1, 1, 1, 0, 0, 1, 0, 0, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 0, 1, 0, 1, 1, 0, 0, 0, 1, 0, 0, 0, 1, 1,
            0
        };
        long[] res = txs.getTxSigs().stream()
                .map(spanBatchSignature -> spanBatchSignature.v().longValue())
                .mapToLong(Long::longValue)
                .toArray();

        assertArrayEquals(vs, res);
    }

    /**
     * Recovery ve 1559.
     *
     * @throws IOException the io exception
     */
    @Test
    void recoveryVE1559() throws IOException {
        SpanBatchTxs txs = new SpanBatchTxs();
        txs.setTotalBlockTxCount(37L);

        URL url = Resources.getResource("rece1559sigs.txt");
        String recunprotectedsigs = Resources.toString(url, Charsets.UTF_8);
        txs.decodeTxSigsRS(Unpooled.wrappedBuffer(Numeric.hexStringToByteArray(recunprotectedsigs)));

        URL url2 = Resources.getResource("rece1559yparity.txt");
        String recunprotectedyparity = Resources.toString(url2, Charsets.UTF_8);
        txs.decodeYParityBits(Unpooled.wrappedBuffer(Numeric.hexStringToByteArray(recunprotectedyparity)));

        URL url3 = Resources.getResource("rece1559protectedbits.txt");
        String recunprotectedprotectedbits = Resources.toString(url3, Charsets.UTF_8);
        txs.decodeProtectedBits(Unpooled.wrappedBuffer(Numeric.hexStringToByteArray(recunprotectedprotectedbits)));
        List<TransactionType> txTypes = new ArrayList<>();
        for (int i = 0; i < txs.getTotalBlockTxCount(); i++) {
            txTypes.add(TransactionType.EIP1559);
        }
        txs.setTxTypes(txTypes);
        txs.recoverV(BigInteger.valueOf(108L));
        long[] vs = new long[] {
            0, 0, 1, 1, 0, 0, 1, 1, 0, 0, 1, 0, 0, 1, 0, 1, 1, 1, 0, 1, 0, 1, 1, 0, 1, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1,
            0
        };
        long[] res = txs.getTxSigs().stream()
                .map(spanBatchSignature -> spanBatchSignature.v().longValue())
                .mapToLong(Long::longValue)
                .toArray();

        assertArrayEquals(vs, res);
    }

    /**
     * Full tx unprotected.
     *
     * @throws IOException the io exception
     */
    @Test
    void fullTxUnprotected() throws IOException {
        URL url = Resources.getResource("fulltxunprotected.txt");
        List<String> txs = Resources.readLines(url, Charsets.UTF_8);

        SpanBatchTxs spanBatchTxs = SpanBatchTxs.newSpanBatchTxs(
                txs.stream().map(Numeric::hexStringToByteArray).collect(Collectors.toList()), BigInteger.valueOf(697L));

        List<String> txs1 = spanBatchTxs.fullTxs(BigInteger.valueOf(697L)).stream()
                .map(Numeric::toHexString)
                .collect(Collectors.toList());

        assertEquals(txs, txs1);
    }

    /**
     * Full tx legacy.
     *
     * @throws IOException the io exception
     */
    @Test
    void fullTxLegacy() throws IOException {
        URL url = Resources.getResource("fulltxlegacy.txt");
        List<String> txs = Resources.readLines(url, Charsets.UTF_8);

        SpanBatchTxs spanBatchTxs = SpanBatchTxs.newSpanBatchTxs(
                txs.stream().map(Numeric::hexStringToByteArray).collect(Collectors.toList()), BigInteger.valueOf(697L));

        List<String> txs1 = spanBatchTxs.fullTxs(BigInteger.valueOf(697L)).stream()
                .map(Numeric::toHexString)
                .collect(Collectors.toList());

        assertEquals(txs, txs1);
    }

    /**
     * Full tx access list.
     *
     * @throws IOException the io exception
     */
    @Test
    void fullTxAccessList() throws IOException {
        URL url = Resources.getResource("fulltxacc.txt");
        List<String> txs = Resources.readLines(url, Charsets.UTF_8);

        SpanBatchTxs spanBatchTxs = SpanBatchTxs.newSpanBatchTxs(
                txs.stream().map(Numeric::hexStringToByteArray).collect(Collectors.toList()), BigInteger.valueOf(697L));

        List<String> txs1 = spanBatchTxs.fullTxs(BigInteger.valueOf(697L)).stream()
                .map(Numeric::toHexString)
                .collect(Collectors.toList());

        assertEquals(txs, txs1);
    }

    /**
     * Full tx dynamic.
     *
     * @throws IOException the io exception
     */
    @Test
    void fullTxDynamic() throws IOException {
        URL url = Resources.getResource("fulltxdyn.txt");
        List<String> txs = Resources.readLines(url, Charsets.UTF_8);

        SpanBatchTxs spanBatchTxs = SpanBatchTxs.newSpanBatchTxs(
                txs.stream().map(Numeric::hexStringToByteArray).collect(Collectors.toList()), BigInteger.valueOf(697L));

        List<String> txs1 = spanBatchTxs.fullTxs(BigInteger.valueOf(697L)).stream()
                .map(Numeric::toHexString)
                .collect(Collectors.toList());

        assertEquals(txs, txs1);
    }

    /**
     * Test span batch max tx data.
     */
    @Test
    void testSpanBatchMaxTxData() {
        Transaction.Builder builder = Transaction.builder();
        SecureRandom rng = new SecureRandom();
        byte[] randomBytes = new byte[(int) (SpanBatchUtils.MaxSpanBatchSize + 1)];
        rng.nextBytes(randomBytes);
        builder.type(TransactionType.EIP1559)
                .chainId(BigInteger.valueOf(108L))
                .maxFeePerGas(Wei.of(5))
                .maxPriorityFeePerGas(Wei.of(5))
                .value(Wei.ZERO)
                .payload(Bytes.wrap(randomBytes));
        final SECPSignature signature =
                SIGNATURE_ALGORITHM.get().createSignature(BigInteger.ONE, BigInteger.ONE, (byte) 1);
        builder.signature(signature);

        Bytes txEncoded = TransactionEncoder.encodeOpaqueBytes(builder.build(), EncodingContext.BLOCK_BODY);

        RLPInput rlpInput = new BytesValueRLPInput(txEncoded, false);
        assertThrows(RuntimeException.class, () -> SpanBatchTxs.readTxData(rlpInput, true));
    }

    //
    //    @Test
    //    void TestSpanBatchTxsContractCreationBits()
    //            throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
    //        Random random = new Random();
    //        int chainId = random.nextInt(1000);
    //        RawSpanBatch rawSpanBatch = BatchTest.randomRawSpanBatch(chainId);
    //        BigInteger contractCreationBits = rawSpanBatch.spanbatchPayload().txs().getContractCreationBits();
    //        Long totalBlockTxCount = rawSpanBatch.spanbatchPayload().txs().getTotalBlockTxCount();
    //
    //        SpanBatchTxs sbt = new SpanBatchTxs();
    //        sbt.setContractCreationBits(contractCreationBits);
    //        sbt.setTotalBlockTxCount(totalBlockTxCount);
    //
    //        byte[] contractCreationBitsBuffer = sbt.encodeContractCreationBits();
    //
    //        Long contractCreationBitBufferLen = totalBlockTxCount / 8;
    //        if (totalBlockTxCount % 8 != 0) {
    //            contractCreationBitBufferLen++;
    //        }
    //        assertEquals(contractCreationBitBufferLen, contractCreationBitsBuffer.length);
    //
    //        sbt.decodeContractCreationBits(contractCreationBitsBuffer);
    //
    //        assertEquals(contractCreationBits, sbt.getContractCreationBits());
    //    }
    //
    //    @Test
    //    public void TestSpanBatchTxsContractCreationCount()
    //            throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
    //        Random random = new Random();
    //        int chainId = random.nextInt(1000);
    //        RawSpanBatch rawSpanBatch = BatchTest.randomRawSpanBatch(chainId);
    //        BigInteger contractCreationBits = rawSpanBatch.spanbatchPayload().txs().getContractCreationBits();
    //        Long contractCreationCount = rawSpanBatch.spanbatchPayload().txs().contractCreationCount();
    //        Long totalBlockTxCount = rawSpanBatch.spanbatchPayload().txs().getTotalBlockTxCount();
    //
    //        SpanBatchTxs sbt = new SpanBatchTxs();
    //        sbt.setContractCreationBits(contractCreationBits);
    //        sbt.setTotalBlockTxCount(totalBlockTxCount);
    //
    //        byte[] contractCreationBitsBuffer = sbt.encodeContractCreationBits();
    //        sbt.setContractCreationBits(null);
    //        sbt.decodeContractCreationBits(contractCreationBitsBuffer);
    //
    //        Long contractCreationCount2 = sbt.contractCreationCount();
    //        assertEquals(contractCreationCount, contractCreationCount2);
    //    }
    //
    //    @Test
    //    public void TestSpanBatchTxsYParityBits()
    //            throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
    //        Random random = new Random();
    //        int chainId = random.nextInt(1000);
    //
    //        RawSpanBatch rawSpanBatch = BatchTest.randomRawSpanBatch(chainId);
    //        BigInteger yParityBits = rawSpanBatch.spanbatchPayload().txs().getyParityBits();
    //        Long totalBlockTxCount = rawSpanBatch.spanbatchPayload().txs().getTotalBlockTxCount();
    //
    //        SpanBatchTxs sbt = new SpanBatchTxs();
    //        sbt.setyParityBits(yParityBits);
    //        sbt.setTotalBlockTxCount(totalBlockTxCount);
    //
    //        byte[] yParityBitsBuffer = sbt.encodeYParityBits();
    //        Long yParityBitBufferLen = totalBlockTxCount / 8;
    //        if (totalBlockTxCount % 8 != 0) {
    //            yParityBitBufferLen++;
    //        }
    //        assertEquals(yParityBitBufferLen, yParityBitsBuffer.length);
    //
    //        sbt.setyParityBits(null);
    //        sbt.decodeYParityBits(yParityBitsBuffer);
    //        assertEquals(yParityBits, sbt.getyParityBits());
    //    }
    //
    //    @Test
    //    public void TestSpanBatchTxsTxSigs()
    //            throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
    //        Random random = new Random();
    //        int chainId = random.nextInt(1000);
    //
    //        RawSpanBatch rawSpanBatch = BatchTest.randomRawSpanBatch(chainId);
    //        List<SpanBatchSignature> txSigs = rawSpanBatch.spanbatchPayload().txs().getTxSigs();
    //        Long totalBlockTxCount = rawSpanBatch.spanbatchPayload().txs().getTotalBlockTxCount();
    //
    //        SpanBatchTxs sbt = new SpanBatchTxs();
    //        sbt.setTotalBlockTxCount(totalBlockTxCount);
    //
    //        sbt.setTxSigs(txSigs);
    //
    //        byte[] txSigsBuffer = sbt.encodeTxSigsRS();
    //
    //        assertEquals(totalBlockTxCount * 64, txSigsBuffer.length);
    //
    //        sbt.setTxSigs(null);
    //
    //        sbt.decodeTxSigsRS(txSigsBuffer);
    //
    //        for (int i = 0; i < totalBlockTxCount; i++) {
    //            assertEquals(txSigs.get(i).r(), sbt.getTxSigs().get(i).r());
    //            assertEquals(txSigs.get(i).s(), sbt.getTxSigs().get(i).s());
    //        }
    //    }
    //
    //    @Test
    //    public void TestSpanBatchTxsTxNonces()
    //            throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
    //        Random random = new Random();
    //        int chainId = random.nextInt(1000);
    //
    //        RawSpanBatch rawSpanBatch = BatchTest.randomRawSpanBatch(chainId);
    //        List<BigInteger> txNonces = rawSpanBatch.spanbatchPayload().txs().getTxNonces();
    //        Long totalBlockTxCount = rawSpanBatch.spanbatchPayload().txs().getTotalBlockTxCount();
    //
    //        SpanBatchTxs sbt = new SpanBatchTxs();
    //        sbt.setTotalBlockTxCount(totalBlockTxCount);
    //        sbt.setTxNonces(txNonces);
    //
    //        Bytes txNoncesBuffer = sbt.encodeTxNonces();
    //        sbt.setTxNonces(null);
    //        sbt.decodeTxNonces(txNoncesBuffer);
    //
    //        assertEquals(txNonces, sbt.getTxNonces());
    //    }
    //
    //    @Test
    //    public void TestSpanBatchTxsTxGases()
    //            throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
    //        Random random = new Random();
    //        int chainId = random.nextInt(1000);
    //
    //        RawSpanBatch rawSpanBatch = BatchTest.randomRawSpanBatch(chainId);
    //        List<BigInteger> txGases = rawSpanBatch.spanbatchPayload().txs().getTxGases();
    //        Long totalBlockTxCount = rawSpanBatch.spanbatchPayload().txs().getTotalBlockTxCount();
    //
    //        SpanBatchTxs sbt = new SpanBatchTxs();
    //        sbt.setTotalBlockTxCount(totalBlockTxCount);
    //        sbt.setTxGases(txGases);
    //        Bytes txGasesBuffer = sbt.encodeTxGases();
    //        sbt.setTxGases(null);
    //        sbt.decodeTxGases(txGasesBuffer);
    //        assertEquals(txGases, sbt.getTxGases());
    //    }
    //
    //    @Test
    //    public void TestSpanBatchTxsTxTos()
    //            throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
    //        Random random = new Random();
    //        int chainId = random.nextInt(1000);
    //
    //        RawSpanBatch rawSpanBatch = BatchTest.randomRawSpanBatch(chainId);
    //        List<String> txTos = rawSpanBatch.spanbatchPayload().txs().getTxTos();
    //        Long totalBlockTxCount = rawSpanBatch.spanbatchPayload().txs().getTotalBlockTxCount();
    //        BigInteger contractCreationBits = rawSpanBatch.spanbatchPayload().txs().getContractCreationBits();
    //
    //        SpanBatchTxs sbt = new SpanBatchTxs();
    //        sbt.setTxTos(txTos);
    //        sbt.setContractCreationBits(contractCreationBits);
    //        sbt.setTotalBlockTxCount(totalBlockTxCount);
    //
    //        byte[] txTosBuffer = sbt.encodeTxTos();
    //        assertEquals(totalBlockTxCount * 20, txTosBuffer.length);
    //
    //        sbt.setTxTos(null);
    //
    //        sbt.decodeTxTos(txTosBuffer);
    //        assertEquals(txTos, sbt.getTxTos());
    //    }
}
