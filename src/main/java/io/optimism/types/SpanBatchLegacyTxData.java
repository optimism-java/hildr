package io.optimism.types;

import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.datatypes.TransactionType;
import org.hyperledger.besu.datatypes.Wei;
import org.hyperledger.besu.ethereum.rlp.BytesValueRLPOutput;
import org.hyperledger.besu.ethereum.rlp.RLPInput;

/**
 * The type SpanBatchLegacyTxData.
 *
 * @param value    the value
 * @param gasPrice the gas price
 * @param data     the data
 * @author zhouop0
 * @since 0.2.4
 */
public record SpanBatchLegacyTxData(Wei value, Wei gasPrice, Bytes data) implements SpanBatchTxData {

    @Override
    public TransactionType txType() {
        return TransactionType.FRONTIER;
    }

    /**
     * Encode span batch legacy tx data.
     *
     * @return the span batch legacy tx data
     */
    public byte[] encode() {
        BytesValueRLPOutput out = new BytesValueRLPOutput();
        out.startList();
        out.writeUInt256Scalar(value());
        out.writeUInt256Scalar(gasPrice());
        out.writeBytes(data());
        out.endList();
        return out.encoded().toArrayUnsafe();
    }

    /**
     * Decode span batch legacy tx data.
     *
     * @param input the rlp
     * @return the span batch legacy tx data
     */
    public static SpanBatchLegacyTxData decode(RLPInput input) {
        input.enterList();
        Wei value = Wei.of(input.readUInt256Scalar());
        Wei gasPrice = Wei.of(input.readUInt256Scalar());
        Bytes data = input.readBytes();
        input.leaveList();
        return new SpanBatchLegacyTxData(value, gasPrice, data);
    }
}
