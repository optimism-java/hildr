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

package io.optimism.derive.stages;

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
