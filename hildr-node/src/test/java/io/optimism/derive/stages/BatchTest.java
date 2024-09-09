// package io.optimism.utilities.derive.stages;
//
// import java.math.BigInteger;
// import java.security.InvalidAlgorithmParameterException;
// import java.security.NoSuchAlgorithmException;
// import java.security.NoSuchProviderException;
// import java.util.ArrayList;
// import java.util.Arrays;
// import java.util.List;
// import java.util.Random;
// import org.apache.tuweni.units.bigints.UInt64;
// import org.web3j.crypto.Credentials;
// import org.web3j.crypto.ECKeyPair;
// import org.web3j.crypto.Keys;
// import org.web3j.crypto.RawTransaction;
// import org.web3j.crypto.TransactionEncoder;
// import org.web3j.crypto.transaction.type.LegacyTransaction;
// import org.web3j.utils.Numeric;
//
// public class BatchTest {
//
//    public static RawSpanBatch randomRawSpanBatch(int chainId)
//            throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
//        Random random = new Random();
//        BigInteger originBits = BigInteger.ZERO;
//        long blockCountLong = 1 + (random.nextInt() & 0xFFL);
//        BigInteger blockCount = BigInteger.valueOf(blockCountLong);
//        List<BigInteger> blockTxCounts = new ArrayList<>();
//        BigInteger totalBlockTxCounts = BigInteger.ZERO;
//        for (int i = 0; i < blockCount.intValue(); i++) {
//            BigInteger blockTxCount = BigInteger.valueOf(random.nextInt(16));
//            blockTxCounts.add(blockTxCount);
//            totalBlockTxCounts = totalBlockTxCounts.add(blockTxCount);
//        }
//        ECKeyPair ecKeyPair = Keys.createEcKeyPair();
//        Credentials credentials = Credentials.create(ecKeyPair);
//        List<String> txs = new ArrayList<>();
//        for (int i = 0; i < totalBlockTxCounts.intValue(); i++) {
//            String tx = randomTransaction(credentials, chainId, UInt64.random().toBigInteger());
//            txs.add(tx);
//        }
//        SpanBatchTxs spanBatchTxs = SpanBatchTxs.newSpanBatchTxs(txs, chainId);
//
//        SpanBatchPrefix spanBatchPrefix = new SpanBatchPrefix(
//                UInt64.random().toBigInteger(),
//                UInt64.random().toBigInteger(),
//                Numeric.toHexString(generateRandomData(20)),
//                Numeric.toHexString(generateRandomData(20)));
//        SpanBatchPayload spanBatchPayload = new SpanBatchPayload(blockCount, originBits, blockTxCounts, spanBatchTxs);
//
//        return new RawSpanBatch(spanBatchPrefix, spanBatchPayload);
//    }
//
//    public static SingularBatch randomSingularBatch(int txCount, int chainId)
//            throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
//        ECKeyPair ecKeyPair = Keys.createEcKeyPair();
//        Credentials credentials = Credentials.create(ecKeyPair);
//        Random rng = new Random();
//        BigInteger maxLimit = new BigInteger("300000000000");
//        BigInteger randomBigInteger = new BigInteger(maxLimit.bitLength(), rng);
//        while (randomBigInteger.compareTo(maxLimit) >= 0) {
//            randomBigInteger = new BigInteger(maxLimit.bitLength(), rng);
//        }
//        List<String> txsEncoded = new ArrayList<>();
//        for (int i = 0; i < txCount; i++) {
//            String txEncode = randomTransaction(credentials, chainId, randomBigInteger);
//            txsEncoded.add(txEncode);
//        }
//        return new SingularBatch(
//                RandomUtils.randomHex(),
//                UInt64.random().toBigInteger(),
//                RandomUtils.randomHex(),
//                UInt64.random().toBigInteger(),
//                txsEncoded);
//    }
//
//    public static String randomTransaction(Credentials credentials, int chainId, BigInteger baseFee)
//            throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
//        List<Integer> list = Arrays.asList(0, 1, 2);
//        int index = new Random().nextInt(list.size());
//        return switch (list.get(index)) {
//            case 0 -> randomLegacyTx(credentials);
//            case 1 -> randomAccessListTx(credentials, chainId);
//            case 2 -> randomDynamicFeeTxWithBaseFee(credentials, chainId, baseFee);
//            default -> throw new RuntimeException("invalid tx type");
//        };
//    }
//
//    public static String randomLegacyTx(Credentials credentials)
//            throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
//        LegacyTransaction lt = buildTransaction();
//        RawTransaction rawTransaction = RawTransaction.createTransaction(
//                lt.getNonce(), lt.getGasPrice(), lt.getGasLimit(), lt.getTo(), lt.getValue(), lt.getData());
//        TransactionEncoder.signMessage(rawTransaction, credentials);
//        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
//        return Numeric.toHexString(signedMessage);
//    }
//
//    public static String randomAccessListTx(Credentials credentials, int chainId)
//            throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
//        LegacyTransaction lt = buildTransaction();
//        // 2930
//        RawTransaction rawTransaction = RawTransaction.createTransaction(
//                chainId,
//                lt.getNonce(),
//                lt.getGasPrice(),
//                lt.getGasLimit(),
//                lt.getTo(),
//                lt.getValue(),
//                lt.getData(),
//                new ArrayList<>());
//        TransactionEncoder.signMessage(rawTransaction, credentials);
//        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
//        return Numeric.toHexString(signedMessage);
//    }
//
//    public static String randomDynamicFeeTxWithBaseFee(Credentials credentials, int chainId, BigInteger baseFee)
//            throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
//        LegacyTransaction lt = buildTransaction();
//        Random random = new Random();
//        BigInteger tip = BigInteger.valueOf(random.nextInt(10)).multiply(BigInteger.TEN.pow(9));
//        BigInteger fee = baseFee.add(tip);
//        // 1559
//        RawTransaction rawTransaction = RawTransaction.createTransaction(
//                chainId, lt.getNonce(), lt.getGasLimit(), lt.getTo(), lt.getValue(), lt.getData(), tip, fee);
//        TransactionEncoder.signMessage(rawTransaction, credentials);
//        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
//        return Numeric.toHexString(signedMessage);
//    }
//
//    public static LegacyTransaction buildTransaction()
//            throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
//        Random random = new Random();
//        BigInteger nonce = BigInteger.valueOf(random.nextInt(10000));
//        BigInteger gasPrice = BigInteger.valueOf(random.nextInt(10000));
//        BigInteger gas = BigInteger.valueOf(random.nextInt(1999999)).add(new BigInteger("21000"));
//        String to = randomAddress();
//        BigInteger value = BigInteger.valueOf(random.nextInt(10)).multiply(BigInteger.TEN.pow(18));
//        byte[] data = generateRandomData(10);
//        return new LegacyTransaction(nonce, gasPrice, gas, to, value, Numeric.toHexString(data));
//    }
//
//    public static String randomAddress()
//            throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
//        ECKeyPair ecKeyPair = Keys.createEcKeyPair();
//        Credentials credentials = Credentials.create(ecKeyPair);
//        return credentials.getAddress();
//    }
//
//    public static byte[] generateRandomData(int size) {
//        Random rng = new Random();
//        byte[] out = new byte[size];
//        rng.nextBytes(out);
//        return out;
//    }
// }
