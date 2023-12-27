package io.optimism.utilities.derive.stages;

import java.util.ArrayList;
import org.apache.commons.lang3.ArrayUtils;
import org.web3j.crypto.transaction.type.ITransaction;
import org.web3j.crypto.transaction.type.Transaction1559;
import org.web3j.crypto.transaction.type.Transaction2930;
import org.web3j.crypto.transaction.type.TransactionType;
import org.web3j.rlp.RlpDecoder;
import org.web3j.rlp.RlpList;

public class SpanBatchTx {
    private final SpanBatchTxData spanBatchTxData;

    protected SpanBatchTx(SpanBatchTxData spanBatchTxData) {
        this.spanBatchTxData = spanBatchTxData;
    }

    public byte txType() {
        return this.spanBatchTxData.txType();
    }

    public static SpanBatchTx newSpanBatchTx(ITransaction tx) {
        SpanBatchTxData spanBatchTxData;
        switch (tx.getType()) {
            case LEGACY:
                spanBatchTxData = new SpanBatchLegacyTxData(tx.getValue(), tx.getGasPrice(), tx.getData());
                break;
            case EIP1559:
                Transaction1559 transaction1559 = (Transaction1559) tx;
                spanBatchTxData = new SpanBatchDynamicFeeTxData(
                        transaction1559.getValue(),
                        transaction1559.getMaxPriorityFeePerGas(),
                        transaction1559.getMaxFeePerGas(),
                        transaction1559.getData(),
                        new ArrayList<>());
                break;
            case EIP2930:
                Transaction2930 transaction2930 = (Transaction2930) tx;
                spanBatchTxData = new SpanBatchAccessListTxData(
                        transaction2930.getValue(),
                        transaction2930.getGasPrice(),
                        transaction2930.getData(),
                        transaction2930.getAccessList());
                break;
            default:
                throw new RuntimeException("invalid tx type:" + tx.getType());
        }
        return new SpanBatchTx(spanBatchTxData);
    }

    public byte[] marshalBinary() {
        if (spanBatchTxData.txType() == 0) {
            SpanBatchLegacyTxData spanBatchLegacyTxData = (SpanBatchLegacyTxData) this.spanBatchTxData;
            return spanBatchLegacyTxData.encode();
        }
        if (TransactionType.EIP1559.getRlpType() == spanBatchTxData.txType()) {
            SpanBatchDynamicFeeTxData spanBatchDynamicFeeTxData = (SpanBatchDynamicFeeTxData) this.spanBatchTxData;
            return spanBatchDynamicFeeTxData.encode();
        }
        if (TransactionType.EIP2930.getRlpType() == spanBatchTxData.txType()) {
            SpanBatchAccessListTxData spanBatchAccessListTxData = (SpanBatchAccessListTxData) this.spanBatchTxData;
            return spanBatchAccessListTxData.encode();
        }
        return null;
    }

    public SpanBatchTxData unMarshalBinary(byte[] b) {
        if (b.length <= 1) {
            throw new RuntimeException("typed transaction too short");
        }
        byte[] spanBatchData = ArrayUtils.subarray(b, 1, b.length);
        RlpList rlpBatchData =
                (RlpList) RlpDecoder.decode(spanBatchData).getValues().get(0);
        if ((b[0] & 0xFF) > 0x7F) {
            return SpanBatchLegacyTxData.decode(rlpBatchData);
        }
        if (TransactionType.EIP2930.getRlpType() == b[0]) {
            return SpanBatchAccessListTxData.decode(rlpBatchData);
        }
        if (TransactionType.EIP1559.getRlpType() == b[0]) {
            return SpanBatchDynamicFeeTxData.decode(rlpBatchData);
        }
        return null;
    }
}
