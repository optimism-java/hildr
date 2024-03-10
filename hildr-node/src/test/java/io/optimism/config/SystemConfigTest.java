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
                "0x440a5e20000a6fe00000000000000000000000050000000065d62bbc00000000005169650000000000000000000000000000000000000000000000000000000234deaa2100000000000000000000000000000000000000000000000000000005dcb37612e680e3ec5290b08570531865205fe15ddc5dbac1f55b4156e2444e77f36d97f40000000000000000000000008f23bb38f531600e5d8fddaaec41f13fab46e98c";
        Config.SystemConfig systemConfig =
                Config.SystemConfig.fromEcotoneTxInput("", null, Numeric.hexStringToByteArray(txInput));
        Tuple2<BigInteger, BigInteger> blobAndBaseScalar = systemConfig.ecotoneScalars();
        assertEquals(BigInteger.ZERO, systemConfig.l1FeeOverhead());
        assertEquals(new BigInteger("684000"), blobAndBaseScalar.component1());
        assertEquals(new BigInteger("0"), blobAndBaseScalar.component2());
        assertEquals("0x8f23bb38f531600e5d8fddaaec41f13fab46e98c", systemConfig.batchSender());
    }
}
