package io.optimism.types;

import static org.hyperledger.besu.ethereum.core.encoding.AccessListTransactionEncoder.writeAccessList;

import java.util.List;
import java.util.Optional;
import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.datatypes.AccessListEntry;
import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.datatypes.TransactionType;
import org.hyperledger.besu.datatypes.Wei;
import org.hyperledger.besu.ethereum.rlp.BytesValueRLPOutput;
import org.hyperledger.besu.ethereum.rlp.RLPInput;

/**
 * The type SpanBatchDynamicFeeTxData.
 *
 * @param value      the value
 * @param gasTipCap  the gas tip cap
 * @param gasFeeCap  the gas fee cap
 * @param data       the data
 * @param accessList the access list
 * @author grapebaba
 * @since 0.2.4
 */
public record SpanBatchDynamicFeeTxData(
        Wei value, Wei gasTipCap, Wei gasFeeCap, Bytes data, List<AccessListEntry> accessList)
        implements SpanBatchTxData {
    @Override
    public TransactionType txType() {
        return TransactionType.EIP1559;
    }

    /**
     * Encode byte [ ].
     *
     * @return the byte [ ]
     */
    public byte[] encode() {
        BytesValueRLPOutput out = new BytesValueRLPOutput();
        out.writeByte(txType().getEthSerializedType());
        out.startList();
        out.writeUInt256Scalar(value());
        out.writeUInt256Scalar(gasTipCap());
        out.writeUInt256Scalar(gasFeeCap());
        out.writeBytes(data());
        writeAccessList(out, Optional.ofNullable(accessList()));
        out.endList();
        return out.encoded().toArrayUnsafe();
    }

    /**
     * Decode span batch dynamic fee tx data.
     *
     * @param input the input
     * @return the span batch dynamic fee tx data
     */
    public static SpanBatchDynamicFeeTxData decode(RLPInput input) {
        input.enterList();
        Wei value = Wei.of(input.readUInt256Scalar());
        Wei gasTipCap = Wei.of(input.readUInt256Scalar());
        Wei gasFeeCap = Wei.of(input.readUInt256Scalar());
        Bytes data = input.readBytes();
        List<AccessListEntry> accessList = input.readList(accessListEntryRLPInput -> {
            accessListEntryRLPInput.enterList();
            final AccessListEntry accessListEntry = new AccessListEntry(
                    Address.wrap(accessListEntryRLPInput.readBytes()),
                    accessListEntryRLPInput.readList(RLPInput::readBytes32));
            accessListEntryRLPInput.leaveList();
            return accessListEntry;
        });

        input.leaveList();
        return new SpanBatchDynamicFeeTxData(value, gasTipCap, gasFeeCap, data, accessList);
    }
}
