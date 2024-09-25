package io.optimism.network;

import io.optimism.types.ExecutionPayloadSSZ;
import io.optimism.types.enums.BlockVersion;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt256;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.web3j.utils.Numeric;

/**
 * The type ExecutionPayloadSSZTest.
 *
 * @author grapebaba
 * @since 0.2.0
 */
public class ExecutionPayloadSSZTest {

    @Test
    public void zeroWithdrawalsSucceeds() {
        String hex =
                "0x0000000000000000000000000000000000000000000000000000000000000123000000000000000000000000000000000000045600000000000000000000000000000000000000000000000000000000000007890000000000000000000000000000000000000000000000000000000000000abc0d0e0f000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000111de000000000000004d01000000000000bc010000000000002b020000000000000002000009030000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000888000200000402000004000000";
        ExecutionPayloadSSZ payload =
                ExecutionPayloadSSZ.from(Bytes.wrap(Numeric.hexStringToByteArray(hex)), BlockVersion.V2);
        Assertions.assertEquals(payload.baseFeePerGas(), UInt256.valueOf(777L));
    }

    @Test
    public void zeroWithdrawalsFailsToDeserialize() {
        String hex =
                "0x0000000000000000000000000000000000000000000000000000000000000123000000000000000000000000000000000000045600000000000000000000000000000000000000000000000000000000000007890000000000000000000000000000000000000000000000000000000000000abc0d0e0f000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000111de000000000000004d01000000000000bc010000000000002b020000000000000002000009030000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000888000200000402000004000000";
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> {
            ExecutionPayloadSSZ.from(Bytes.wrap(Numeric.hexStringToByteArray(hex)), BlockVersion.V1);
        });
    }

    @Test
    public void withdrawalsSucceeds() {
        String hex =
                "0x0000000000000000000000000000000000000000000000000000000000000123000000000000000000000000000000000000045600000000000000000000000000000000000000000000000000000000000007890000000000000000000000000000000000000000000000000000000000000abc0d0e0f000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000111de000000000000004d01000000000000bc010000000000002b020000000000000002000009030000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000888000200000402000004000000db030000000000008e0200000000000000000000000000000000000000000000000008984101000000000000";
        ExecutionPayloadSSZ payload =
                ExecutionPayloadSSZ.from(Bytes.wrap(Numeric.hexStringToByteArray(hex)), BlockVersion.V2);
        Assertions.assertEquals(payload.baseFeePerGas(), UInt256.valueOf(777L));
    }

    @Test
    public void withdrawalsFailsToDeserialize() {
        String hex =
                "0x0000000000000000000000000000000000000000000000000000000000000123000000000000000000000000000000000000045600000000000000000000000000000000000000000000000000000000000007890000000000000000000000000000000000000000000000000000000000000abc0d0e0f000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000111de000000000000004d01000000000000bc010000000000002b020000000000000002000009030000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000888000200000402000004000000db030000000000008e0200000000000000000000000000000000000000000000000008984101000000000000";
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> {
            ExecutionPayloadSSZ.from(Bytes.wrap(Numeric.hexStringToByteArray(hex)), BlockVersion.V1);
        });
    }

    @Test
    public void maxWithdrawalsSucceeds() {
        String hex =
                "0x0000000000000000000000000000000000000000000000000000000000000123000000000000000000000000000000000000045600000000000000000000000000000000000000000000000000000000000007890000000000000000000000000000000000000000000000000000000000000abc0d0e0f000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000111de000000000000004d01000000000000bc010000000000002b020000000000000002000009030000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000888000200000402000004000000db030000000000008e0200000000000000000000000000000000000000000000000008984101000000000000db030000000000008e0200000000000000000000000000000000000000000000000008984101000000000000db030000000000008e0200000000000000000000000000000000000000000000000008984101000000000000db030000000000008e0200000000000000000000000000000000000000000000000008984101000000000000db030000000000008e0200000000000000000000000000000000000000000000000008984101000000000000db030000000000008e0200000000000000000000000000000000000000000000000008984101000000000000db030000000000008e0200000000000000000000000000000000000000000000000008984101000000000000db030000000000008e0200000000000000000000000000000000000000000000000008984101000000000000db030000000000008e0200000000000000000000000000000000000000000000000008984101000000000000db030000000000008e0200000000000000000000000000000000000000000000000008984101000000000000db030000000000008e0200000000000000000000000000000000000000000000000008984101000000000000db030000000000008e0200000000000000000000000000000000000000000000000008984101000000000000db030000000000008e0200000000000000000000000000000000000000000000000008984101000000000000db030000000000008e0200000000000000000000000000000000000000000000000008984101000000000000db030000000000008e0200000000000000000000000000000000000000000000000008984101000000000000db030000000000008e0200000000000000000000000000000000000000000000000008984101000000000000";
        ExecutionPayloadSSZ payload =
                ExecutionPayloadSSZ.from(Bytes.wrap(Numeric.hexStringToByteArray(hex)), BlockVersion.V2);
        Assertions.assertEquals(payload.baseFeePerGas(), UInt256.valueOf(777L));
    }

    @Test
    public void tooManyWithdrawalsErrors() {
        String hex =
                "0x0000000000000000000000000000000000000000000000000000000000000123000000000000000000000000000000000000045600000000000000000000000000000000000000000000000000000000000007890000000000000000000000000000000000000000000000000000000000000abc0d0e0f000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000111de000000000000004d01000000000000bc010000000000002b020000000000000002000009030000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000888000200000402000004000000db030000000000008e0200000000000000000000000000000000000000000000000008984101000000000000db030000000000008e0200000000000000000000000000000000000000000000000008984101000000000000db030000000000008e0200000000000000000000000000000000000000000000000008984101000000000000db030000000000008e0200000000000000000000000000000000000000000000000008984101000000000000db030000000000008e0200000000000000000000000000000000000000000000000008984101000000000000db030000000000008e0200000000000000000000000000000000000000000000000008984101000000000000db030000000000008e0200000000000000000000000000000000000000000000000008984101000000000000db030000000000008e0200000000000000000000000000000000000000000000000008984101000000000000db030000000000008e0200000000000000000000000000000000000000000000000008984101000000000000db030000000000008e0200000000000000000000000000000000000000000000000008984101000000000000db030000000000008e0200000000000000000000000000000000000000000000000008984101000000000000db030000000000008e0200000000000000000000000000000000000000000000000008984101000000000000db030000000000008e0200000000000000000000000000000000000000000000000008984101000000000000db030000000000008e0200000000000000000000000000000000000000000000000008984101000000000000db030000000000008e0200000000000000000000000000000000000000000000000008984101000000000000db030000000000008e0200000000000000000000000000000000000000000000000008984101000000000000db030000000000008e0200000000000000000000000000000000000000000000000008984101000000000000";
        Assertions.assertThrowsExactly(IllegalArgumentException.class, () -> {
            ExecutionPayloadSSZ.from(Bytes.wrap(Numeric.hexStringToByteArray(hex)), BlockVersion.V2);
        });
    }
}