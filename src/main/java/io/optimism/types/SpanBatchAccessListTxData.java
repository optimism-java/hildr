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
 * The type SpanBatchAccessListTxData.
 *
 * @param value      the value
 * @param gasPrice   the gas price
 * @param data       the data
 * @param accessList the access list
 * @author grapebaba
 * @since 0.2.4
 */
public record SpanBatchAccessListTxData(Wei value, Wei gasPrice, Bytes data, List<AccessListEntry> accessList)
        implements SpanBatchTxData {

    @Override
    public TransactionType txType() {
        return TransactionType.ACCESS_LIST;
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
        out.writeUInt256Scalar(gasPrice());
        out.writeBytes(data());
        writeAccessList(out, Optional.ofNullable(accessList()));
        out.endList();
        return out.encoded().toArrayUnsafe();
    }

    /**
     * Decode span batch access list tx data.
     *
     * @param input the input
     * @return the span batch access list tx data
     */
    public static SpanBatchAccessListTxData decode(RLPInput input) {
        input.enterList();
        Wei value = Wei.of(input.readUInt256Scalar());
        Wei gasPrice = Wei.of(input.readUInt256Scalar());
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
        return new SpanBatchAccessListTxData(value, gasPrice, data, accessList);
    }
}
