package io.optimism.utilities.derive.stages;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import io.netty.buffer.Unpooled;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.List;
import org.apache.tuweni.bytes.Bytes;
import org.junit.jupiter.api.Test;
import org.web3j.utils.Numeric;

public class SpanBatchTxsTest {
    @Test
    void decodeAndEncodeSpanBatchBits() {
        String test =
                "0xaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";
        BigInteger res = SpanBatchTxs.decodeSpanBatchBits(
                Unpooled.wrappedBuffer(Bytes.fromHexString(test).toArray()), 544);
        var res1 = SpanBatchTxs.encodeSpanBatchBits(544, res);
        assertArrayEquals(Numeric.hexStringToByteArray(test), res1);
    }

    @Test
    void decodeAndEncodeTxSigs() throws IOException {
        URL url = Resources.getResource("txsigs.txt");
        String test = Resources.toString(url, Charsets.UTF_8);

        List<SpanBatchSignature> txSigs = SpanBatchTxs.decodeSpanBatchTxSigsRS(Numeric.hexStringToByteArray(test));
        SpanBatchTxs txs = new SpanBatchTxs();
        txs.setTxSigs(txSigs);
        var res = txs.encodeTxSigsRS();
        assertArrayEquals(Numeric.hexStringToByteArray(test), res);
    }

    @Test
    void decodeAndEncodeTxNonces() throws IOException {
        URL url = Resources.getResource("txnonces.txt");
        String test = Resources.toString(url, Charsets.UTF_8);

        List<BigInteger> txNonces = SpanBatchTxs.decodeSpanBatchTxNonces(Numeric.hexStringToByteArray(test));
        SpanBatchTxs txs = new SpanBatchTxs();
        txs.setTxNonces(txNonces);
        var res = txs.encodeTxNonces();
        assertArrayEquals(Numeric.hexStringToByteArray(test), res);
    }

    @Test
    void decodeAndEncodeTxGases() throws IOException {
        URL url = Resources.getResource("txgases.txt");
        String test = Resources.toString(url, Charsets.UTF_8);

        List<BigInteger> txGases = SpanBatchTxs.decodeSpanBatchTxGases(Numeric.hexStringToByteArray(test));
        SpanBatchTxs txs = new SpanBatchTxs();
        txs.setTxGases(txGases);
        var res = txs.encodeTxGases();
        assertArrayEquals(Numeric.hexStringToByteArray(test), res);
    }

    @Test
    void decodeAndEncodeTxTos() throws IOException {
        URL url = Resources.getResource("txtos.txt");
        String test = Resources.toString(url, Charsets.UTF_8);

        List<String> txTos = SpanBatchTxs.decodeSpanBatchTxTos(Numeric.hexStringToByteArray(test));
        SpanBatchTxs txs = new SpanBatchTxs();
        txs.setTxTos(txTos);
        var res = txs.encodeTxTos();
        assertArrayEquals(Numeric.hexStringToByteArray(test), res);
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
