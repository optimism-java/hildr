package io.optimism.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.optimism.types.AttributesDepositedCall;
import io.optimism.types.Epoch;
import java.math.BigInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The type EpochTest.
 *
 * @author grapebaba
 * @since 0.1.0
 */
class EpochTest {

    /** From. */
    @Test
    @DisplayName("should convert from the deposited transaction calldata")
    void from() {
        final String callData =
                "0x015d8eb900000000000000000000000000000000000000000000000000000000008768240000000000000000"
                        + "000000000000000000000000000000000000000064443450000000000000000000000000000000000000"
                        + "000000000000000000000000000e0444c991c5fe1d7291ff34b3f5c3b44ee861f021396d33ba3255b83d"
                        + "f30e357d0000000000000000000000000000000000000000000000000000000000000005000000000000"
                        + "0000000000007431310e026b69bfc676c0013e12a1a11411eec900000000000000000000000000000000"
                        + "000000000000000000000000000008340000000000000000000000000000000000000000000000000000"
                        + "0000000f4240";

        final String expectedHash = "0x0444c991c5fe1d7291ff34b3f5c3b44ee861f021396d33ba3255b83df30e357d";
        final BigInteger expectedBlockNumber = BigInteger.valueOf(8874020L);
        final BigInteger expectedTimestamp = BigInteger.valueOf(1682191440L);

        final AttributesDepositedCall call = AttributesDepositedCall.from(callData);
        final Epoch epoch = call.toEpoch();

        assertEquals(expectedHash, epoch.hash());
        assertEquals(expectedBlockNumber, epoch.number());
        assertEquals(expectedTimestamp, epoch.timestamp());
    }
}
