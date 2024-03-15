package io.optimism.config;

import static org.junit.Assert.assertEquals;

import java.math.BigInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.utils.Numeric;

/**
 * The type of SystemConfigTest.
 *
 * @author thinkAfCod
 * @since 0.2.7
 */
public class SystemConfigTest {

    @Test
    @DisplayName("decode Bedrock tx input successfully")
    void decodeBedrockTxInput() {
        String txInput =
                "015d8eb900000000000000000000000000000000000000000000000000000000005169650000000000000000000000000000000000000000000000000000000065d62bbc0000000000000000000000000000000000000000000000000000000234deaa21e680e3ec5290b08570531865205fe15ddc5dbac1f55b4156e2444e77f36d97f400000000000000000000000000000000000000000000000000000000000000040000000000000000000000008f23bb38f531600e5d8fddaaec41f13fab46e98c00000000000000000000000000000000000000000000000000000000000000bc00000000000000000000000000000000000000000000000000000000000a6fe0";
        Config.SystemConfig systemConfig =
                Config.SystemConfig.fromBedrockTxInput("", null, Numeric.hexStringToByteArray(txInput));
        assertEquals(new BigInteger("188"), systemConfig.l1FeeOverhead());
        assertEquals(new BigInteger("684000"), systemConfig.l1FeeScalar());
        assertEquals("0x8f23bb38f531600e5d8fddaaec41f13fab46e98c", systemConfig.batchSender());
    }

    @Test
    @DisplayName("decode Ecotone tx input successfully")
    void decodeEcotoneTxInput() {
        String txInput =
                "0x440a5e2000001db0000d273000000000000000010000000065dadd6c000000000051c3c8000000000000000000000000000000000000000000000000000000000000006800000000000000000000000000000000000000000000000000000005029946931815ec5db1534f559ee75f81a4283d99a2418a0cdb46cf0acdf7214afc22a0bd0000000000000000000000008f23bb38f531600e5d8fddaaec41f13fab46e98c";
        Config.SystemConfig systemConfig =
                Config.SystemConfig.fromEcotoneTxInput("", null, Numeric.hexStringToByteArray(txInput));
        Tuple2<BigInteger, BigInteger> blobAndBaseScalar = systemConfig.ecotoneScalars();
        assertEquals(BigInteger.ZERO, systemConfig.l1FeeOverhead());
        assertEquals(new BigInteger("862000"), blobAndBaseScalar.component1());
        assertEquals(new BigInteger("7600"), blobAndBaseScalar.component2());
        assertEquals("0x8f23bb38f531600e5d8fddaaec41f13fab46e98c", systemConfig.batchSender());
    }
}
