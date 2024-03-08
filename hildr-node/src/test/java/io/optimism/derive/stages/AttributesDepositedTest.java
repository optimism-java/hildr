package io.optimism.derive.stages;

import java.math.BigInteger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.web3j.utils.Numeric;

/**
 * The type AttributesDepositedTest.
 *
 * @author thinkAfCod
 * @since 0.2.7
 */
public class AttributesDepositedTest {

    @Test
    void testEncode() {
        Attributes.AttributesDeposited deposited = new Attributes.AttributesDeposited(
                BigInteger.valueOf(5335397L),
                BigInteger.valueOf(1708534716L),
                new BigInteger("9476942369"),
                "0xe680e3ec5290b08570531865205fe15ddc5dbac1f55b4156e2444e77f36d97f4",
                BigInteger.valueOf(5L),
                "0x0000000000000000000000008f23bb38f531600e5d8fddaaec41f13fab46e98c",
                null,
                null,
                null,
                new BigInteger("25177585170"),
                BigInteger.valueOf(684000L),
                BigInteger.valueOf(0L),
                false);
        byte[] bytes = deposited.encodeInEcotone();
        Assertions.assertEquals(
                "0x440a5e20000a6fe00000000000000000000000050000000065d62bbc00000000005169650000000000000000000000000000000000000000000000000000000234deaa2100000000000000000000000000000000000000000000000000000005dcb37612e680e3ec5290b08570531865205fe15ddc5dbac1f55b4156e2444e77f36d97f40000000000000000000000008f23bb38f531600e5d8fddaaec41f13fab46e98c",
                Numeric.toHexString(bytes));
    }
}
