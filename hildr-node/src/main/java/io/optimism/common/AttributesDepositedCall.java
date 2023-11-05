/*
 * Copyright 2023 281165273grape@gmail.com
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

package io.optimism.common;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import net.osslabz.evm.abi.decoder.AbiDecoder;
import net.osslabz.evm.abi.decoder.DecodedFunctionCall;

/**
 * The type AttributesDepositedCall.
 *
 * @param number the number
 * @param timestamp the timestamp
 * @param baseFee the base fee
 * @param hash the hash
 * @param sequenceNumber the sequence number
 * @param batcherHash the batcher hash
 * @param feeOverhead the fee overhead
 * @param feeScalar the fee scalar
 * @author grapebaba
 * @since 0.1.0
 */
public record AttributesDepositedCall(
        BigInteger number,
        BigInteger timestamp,
        BigInteger baseFee,
        String hash,
        BigInteger sequenceNumber,
        String batcherHash,
        BigInteger feeOverhead,
        BigInteger feeScalar) {

    private static final AbiDecoder l1BlockAbi;

    static {
        try {
            Path path = Paths.get(Objects.requireNonNull(Epoch.class.getResource("/abi/L1Block.json"))
                    .toURI());
            l1BlockAbi = new AbiDecoder(path.toString());
        } catch (URISyntaxException | IOException e) {
            throw new AbiFileLoadException(e);
        }
    }

    /**
     * From attributes deposited call.
     *
     * @param callData the call data
     * @return the attributes deposited call
     */
    public static AttributesDepositedCall from(String callData) {
        DecodedFunctionCall decodedFunctionCall = l1BlockAbi.decodeFunctionCall(callData);
        BigInteger number = (BigInteger) decodedFunctionCall.getParam("_number").getValue();
        BigInteger timestamp =
                (BigInteger) decodedFunctionCall.getParam("_timestamp").getValue();
        BigInteger baseFee =
                (BigInteger) decodedFunctionCall.getParam("_basefee").getValue();
        String hash = (String) decodedFunctionCall.getParam("_hash").getValue();
        BigInteger sequenceNumber =
                (BigInteger) decodedFunctionCall.getParam("_sequencenumber").getValue();
        String batcherHash =
                (String) decodedFunctionCall.getParam("_batcherhash").getValue();
        BigInteger feeOverhead =
                (BigInteger) decodedFunctionCall.getParam("_l1feeoverhead").getValue();
        BigInteger feeScalar =
                (BigInteger) decodedFunctionCall.getParam("_l1feescalar").getValue();

        return new AttributesDepositedCall(
                number, timestamp, baseFee, hash, sequenceNumber, batcherHash, feeOverhead, feeScalar);
    }
}
