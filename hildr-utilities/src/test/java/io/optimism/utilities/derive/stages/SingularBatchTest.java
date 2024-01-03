// package io.optimism.utilities.derive.stages;
//
// import static org.junit.jupiter.api.Assertions.assertEquals;
//
// import java.security.InvalidAlgorithmParameterException;
// import java.security.NoSuchAlgorithmException;
// import java.security.NoSuchProviderException;
// import java.util.Random;
// import org.junit.jupiter.api.Test;
//
// public class SingularBatchTest {
//
//    @Test
//    public void TestSingularBatchForBatchInterface()
//            throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
//        Random random = new Random();
//        int txCount = 1 + random.nextInt(8);
//        int chainId = random.nextInt(1000);
//
//        SingularBatch singularBatch = BatchTest.randomSingularBatch(txCount, chainId);
//
//        assertEquals(singularBatch.getBatchType(), BatchType.SINGULAR_BATCH_TYPE.getCode());
//        assertEquals(singularBatch.getTimestamp(), singularBatch.timestamp());
//        assertEquals(singularBatch.epochNum(), singularBatch.getEpochNum());
//    }
// }
