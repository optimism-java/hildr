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
 * The type Epoch.
 *
 * @param number L1 block number.
 * @param hash L1 block hash.
 * @param timestamp L1 block timestamp.
 * @author grapebaba
 * @since 0.1.0
 */
public record Epoch(BigInteger number, String hash, BigInteger timestamp) {

  private static final AbiDecoder l1BlockAbi;

  static {
    try {
      Path path =
          Paths.get(Objects.requireNonNull(Epoch.class.getResource("/abi/L1Block.json")).toURI());
      l1BlockAbi = new AbiDecoder(path.toString());
    } catch (URISyntaxException | IOException e) {
      throw new AbiFileLoadException(e);
    }
  }

  /**
   * From epoch.
   *
   * @param hexCallData the hex call data
   * @return the epoch
   */
  public static Epoch from(String hexCallData) {
    DecodedFunctionCall decodedFunctionCall = l1BlockAbi.decodeFunctionCall(hexCallData);
    BigInteger number = (BigInteger) decodedFunctionCall.getParam("_number").getValue();
    BigInteger timestamp = (BigInteger) decodedFunctionCall.getParam("_timestamp").getValue();
    String hash = (String) decodedFunctionCall.getParam("_hash").getValue();

    return new Epoch(number, hash, timestamp);
  }
}
