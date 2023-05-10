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

package io.optimism.l1;

import java.math.BigInteger;
import java.util.Arrays;
import org.bouncycastle.util.encoders.Hex;
import org.web3j.protocol.core.methods.response.EthLog;

/**
 * SystemConfigUpdate class.
 *
 * @author thinkAfCod
 * @since 0.1.0
 */
public class SystemConfigUpdate {

  private static final String ERROR_MSG = "invalid system config update";

  private String address;

  private BigInteger feeOverhead;

  private BigInteger feeScalar;

  private BigInteger gas;

  private final SystemConfigUpdateType type;

  /**
   * the SystemConfigUpdate constructor. type define to SystemConfigUpdateType.BatchSender
   *
   * @param address batch sender address
   */
  public SystemConfigUpdate(String address) {
    this.address = address;
    this.type = SystemConfigUpdateType.BatchSender;
  }

  /**
   * the SystemConfigUpdate constructor. type define to SystemConfigUpdateType.Fees
   *
   * @param feeOverhead overhead fee
   * @param feeScalar scalar fee
   */
  public SystemConfigUpdate(BigInteger feeOverhead, BigInteger feeScalar) {
    this.feeOverhead = feeOverhead;
    this.feeScalar = feeScalar;
    this.type = SystemConfigUpdateType.Fees;
  }

  /**
   * the SystemConfigUpdate constructor. type define to SystemConfigUpdateType.Gas
   *
   * @param gas gas value
   */
  public SystemConfigUpdate(BigInteger gas) {
    this.gas = gas;
    this.type = SystemConfigUpdateType.Gas;
  }

  /**
   * get batch sender address.
   *
   * @return batch sender address
   */
  public String getAddress() {
    return address;
  }

  /**
   * get overhead fee.
   *
   * @return overhead fee
   */
  public BigInteger getFeeOverhead() {
    return feeOverhead;
  }

  /**
   * get scalar fee.
   *
   * @return scalar fee
   */
  public BigInteger getFeeScalar() {
    return feeScalar;
  }

  /**
   * get gas.
   *
   * @return gas
   */
  public BigInteger getGas() {
    return gas;
  }

  /**
   * get system config update type.
   *
   * @return the enum of system config update type
   */
  public SystemConfigUpdateType getType() {
    return type;
  }

  /**
   * create systemConfigUpdate from EthLog.LogObject.
   *
   * @param log EthLog.LogObject
   * @return a SystemConfigUpdate instance
   */
  public static SystemConfigUpdate tryFrom(EthLog.LogObject log) {
    byte[] decode = Hex.decode(log.getTopics().get(1));
    BigInteger version = new BigInteger(decode);

    if (version.compareTo(BigInteger.ZERO) != 0) {
      throw new IllegalStateException(ERROR_MSG);
    }

    byte[] decodeUpdateType = Hex.decode(log.getTopics().get(2));
    BigInteger updateType = new BigInteger(decodeUpdateType);
    if (BigInteger.ZERO.compareTo(updateType) == 0) {
      byte[] data = Hex.decode(log.getData());
      byte[] addrBytes = Arrays.copyOfRange(data, 76, 96);
      return new SystemConfigUpdate(Hex.toHexString(addrBytes));
    } else if (BigInteger.ONE.compareTo(updateType) == 0) {
      byte[] data = Hex.decode(log.getData());
      byte[] feeOverheadBytes = Arrays.copyOfRange(data, 64, 96);
      byte[] feeScalarBytes = Arrays.copyOfRange(data, 96, 128);
      BigInteger feeOverhead = new BigInteger(feeOverheadBytes);
      BigInteger feeScalar = new BigInteger(feeScalarBytes);
      return new SystemConfigUpdate(feeOverhead, feeScalar);
    } else if (BigInteger.TWO.compareTo(updateType) == 0) {
      byte[] data = Hex.decode(log.getData());
      byte[] gasBytes = Arrays.copyOfRange(data, 64, 96);
      BigInteger gas = new BigInteger(gasBytes);
      return new SystemConfigUpdate(gas);
    } else {
      throw new IllegalStateException(ERROR_MSG);
    }
  }
}
