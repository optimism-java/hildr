/*
 * Copyright 2023 q315xia@163.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package io.optimism.utilities.derive.stages;

import java.util.ArrayList;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.datatypes.TransactionType;
import org.hyperledger.besu.ethereum.core.Transaction;
import org.hyperledger.besu.ethereum.rlp.BytesValueRLPInput;
import org.hyperledger.besu.ethereum.rlp.RLPInput;

/**
 * The type SpanBatchTx.
 *
 * @author zhouop0
 * @since 0.2.4
 */
public class SpanBatchTx {
    private final SpanBatchTxData spanBatchTxData;

    /**
     * Instantiates a new Span batch tx.
     *
     * @param spanBatchTxData the span batch tx data
     */
    protected SpanBatchTx(SpanBatchTxData spanBatchTxData) {
        this.spanBatchTxData = spanBatchTxData;
    }

    /**
     * Tx type byte.
     *
     * @return the byte
     */
    public TransactionType txType() {
        return this.spanBatchTxData.txType();
    }

    /**
     * New span batch tx span batch tx.
     *
     * @param tx the tx
     * @return the span batch tx
     */
    public static SpanBatchTx newSpanBatchTx(Transaction tx) {
        SpanBatchTxData spanBatchTxData =
                switch (tx.getType()) {
                    case FRONTIER -> new SpanBatchLegacyTxData(
                            tx.getValue(), tx.getGasPrice().orElseThrow(), tx.getPayload());
                    case EIP1559 -> new SpanBatchDynamicFeeTxData(
                            tx.getValue(),
                            tx.getMaxPriorityFeePerGas().orElseThrow(),
                            tx.getMaxFeePerGas().orElseThrow(),
                            tx.getPayload(),
                            tx.getAccessList().orElse(new ArrayList<>()));
                    case ACCESS_LIST -> new SpanBatchAccessListTxData(
                            tx.getValue(),
                            tx.getGasPrice().orElseThrow(),
                            tx.getPayload(),
                            tx.getAccessList().orElse(new ArrayList<>()));
                    case BLOB -> throw new RuntimeException("blob tx not supported");
                };
        return new SpanBatchTx(spanBatchTxData);
    }

    /**
     * Marshal spanBatch tx.
     *
     * @return the span batch tx data
     */
    public byte[] marshalBinary() {
        if (spanBatchTxData.txType() == TransactionType.FRONTIER) {
            SpanBatchLegacyTxData spanBatchLegacyTxData = (SpanBatchLegacyTxData) this.spanBatchTxData;
            return spanBatchLegacyTxData.encode();
        }
        if (TransactionType.EIP1559 == spanBatchTxData.txType()) {
            SpanBatchDynamicFeeTxData spanBatchDynamicFeeTxData = (SpanBatchDynamicFeeTxData) this.spanBatchTxData;
            return spanBatchDynamicFeeTxData.encode();
        }
        if (TransactionType.ACCESS_LIST == spanBatchTxData.txType()) {
            SpanBatchAccessListTxData spanBatchAccessListTxData = (SpanBatchAccessListTxData) this.spanBatchTxData;
            return spanBatchAccessListTxData.encode();
        }
        return null;
    }

    /**
     * Unmarshal binary span batch tx data.
     *
     * @param b the b
     * @return the span batch tx data
     */
    public static SpanBatchTxData unmarshalBinary(byte[] b) {
        if (b.length <= 1) {
            throw new RuntimeException("typed transaction too short");
        }
        if ((b[0] & 0xFF) > 0x7F) {
            RLPInput input = new BytesValueRLPInput(Bytes.wrap(b), false);
            return SpanBatchLegacyTxData.decode(input);
        }

        byte[] spanBatchData = ArrayUtils.subarray(b, 1, b.length);
        RLPInput input = new BytesValueRLPInput(Bytes.wrap(spanBatchData), false);
        if (TransactionType.ACCESS_LIST.getEthSerializedType() == b[0]) {
            return SpanBatchAccessListTxData.decode(input);
        }
        if (TransactionType.EIP1559.getEthSerializedType() == b[0]) {
            return SpanBatchDynamicFeeTxData.decode(input);
        }
        return null;
    }
}
