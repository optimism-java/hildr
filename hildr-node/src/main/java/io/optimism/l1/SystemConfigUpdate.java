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
public abstract class SystemConfigUpdate {

  private static final String ERROR_MSG = "invalid system config update";

  /** not public constructor. */
  private SystemConfigUpdate() {}

  /** update batch sender. */
  public static final class BatchSender extends SystemConfigUpdate {

    private final String address;

    /**
     * the BatchSender constructor.
     *
     * @param address batch sender address
     */
    public BatchSender(String address) {
      this.address = address;
    }

    /**
     * get batch sender.
     *
     * @return batch sender
     */
    public String getAddress() {
      return address;
    }
  }

  /** update fee. */
  public static final class Fees extends SystemConfigUpdate {

    private final BigInteger feeOverhead;

    private final BigInteger feeScalar;

    /**
     * Fees constructor.
     *
     * @param feeOverhead overhead fee
     * @param feeScalar scalar fee
     */
    public Fees(BigInteger feeOverhead, BigInteger feeScalar) {
      this.feeOverhead = feeOverhead;
      this.feeScalar = feeScalar;
    }

    /**
     * get fee of overhead.
     *
     * @return fee of overhead
     */
    public BigInteger getFeeOverhead() {
      return feeOverhead;
    }

    /**
     * get fee of scalar.
     *
     * @return fee of scalar
     */
    public BigInteger getFeeScalar() {
      return feeScalar;
    }
  }

  /** update gas. */
  public static final class Gas extends SystemConfigUpdate {

    private final BigInteger gas;

    /**
     * the Gas constructor.
     *
     * @param gas gas value
     */
    public Gas(BigInteger gas) {
      this.gas = gas;
    }

    /**
     * get fee of gas.
     *
     * @return fee of gas
     */
    public BigInteger getGas() {
      return gas;
    }
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
      return new BatchSender(Hex.toHexString(addrBytes));
    } else if (BigInteger.ONE.compareTo(updateType) == 0) {
      byte[] data = Hex.decode(log.getData());
      byte[] feeOverheadBytes = Arrays.copyOfRange(data, 64, 96);
      byte[] feeScalarBytes = Arrays.copyOfRange(data, 96, 128);
      BigInteger feeOverhead = new BigInteger(feeOverheadBytes);
      BigInteger feeScalar = new BigInteger(feeScalarBytes);
      return new Fees(feeOverhead, feeScalar);
    } else if (BigInteger.TWO.compareTo(updateType) == 0) {
      byte[] data = Hex.decode(log.getData());
      byte[] gasBytes = Arrays.copyOfRange(data, 64, 96);
      BigInteger gas = new BigInteger(gasBytes);
      return new Gas(gas);
    } else {
      throw new IllegalStateException(ERROR_MSG);
    }
  }
}
