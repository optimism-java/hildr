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

package io.optimism.derive.stages;


import java.math.BigInteger;
import java.util.List;
import net.osslabz.evm.abi.definition.AbiDefinition.Entry.Param;
import net.osslabz.evm.abi.definition.AbiDefinition.Event;
import net.osslabz.evm.abi.definition.SolidityType;
import org.apache.commons.lang3.ArrayUtils;
import org.web3j.protocol.core.methods.response.EthLog.LogObject;
import org.web3j.utils.Numeric;

/**
 * The type UserDeposited.
 *
 * @param from the from address.
 * @param to the to address.
 * @param mint the mint amount.
 * @param value the value.
 * @param gas the gas.
 * @param isCreation the isCreation flag.
 * @param data the data.
 * @param l1BlockNum the L1 blockNum.
 * @param l1BlockHash the L1 blockHash.
 * @param logIndex the logIndex.
 * @author grapebaba
 * @since 0.1.0
 */
public record UserDeposited(
    String from,
    String to,
    BigInteger mint,
    BigInteger value,
    BigInteger gas,
    boolean isCreation,
    byte[] data,
    BigInteger l1BlockNum,
    String l1BlockHash,
    BigInteger logIndex) {

  private static final Event event;

  static {
    Param from = new Param();
    from.setIndexed(true);
    from.setName("from");
    from.setType(new SolidityType.AddressType());
    Param to = new Param();
    to.setIndexed(true);
    to.setName("to");
    to.setType(new SolidityType.AddressType());
    Param version = new Param();
    version.setIndexed(true);
    version.setName("version");
    version.setType(new SolidityType.UnsignedIntType("uint256"));
    Param opaqueData = new Param();
    opaqueData.setIndexed(false);
    opaqueData.setName("opaqueData");
    opaqueData.setType(new SolidityType.BytesType());
    event = new Event(false, "UserDeposited", List.of(from, to, version, opaqueData), List.of());
  }

  /**
   * Derive UserDeposited from log.
   *
   * @param log the log
   * @return the UserDeposited
   */
  public static UserDeposited fromLog(LogObject log) {
    byte[][] topics =
        log.getTopics().stream().map(Numeric::hexStringToByteArray).toArray(byte[][]::new);
    List<?> decodedEvent = event.decode(Numeric.hexStringToByteArray(log.getData()), topics);
    byte[] opaqueData = (byte[]) decodedEvent.get(3);

    String from = log.getTopics().get(1);
    String to = log.getTopics().get(2);
    BigInteger mint = new BigInteger(ArrayUtils.subarray(opaqueData, 0, 32));
    BigInteger value = new BigInteger(ArrayUtils.subarray(opaqueData, 32, 64));
    BigInteger gas = new BigInteger(ArrayUtils.subarray(opaqueData, 64, 72));
    boolean isCreation = opaqueData[72] != (byte) 0;
    byte[] data = ArrayUtils.subarray(opaqueData, 73, opaqueData.length);

    return new UserDeposited(
        from,
        to,
        mint,
        value,
        gas,
        isCreation,
        data,
        log.getBlockNumber(),
        log.getBlockHash(),
        log.getLogIndex());
  }
}
