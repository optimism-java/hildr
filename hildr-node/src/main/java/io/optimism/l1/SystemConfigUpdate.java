package io.optimism.l1;

import java.math.BigInteger;
import java.util.Arrays;
import org.bouncycastle.util.encoders.Hex;
import org.web3j.protocol.core.methods.response.EthLog;

/**
 * SystemConfigUpdate
 *
 * @author thinkAfCod
 * @since 2023.05
 */
public class SystemConfigUpdate {

  private static final String ERROR_MSG = "invalid system config update";

  private String address;

  private BigInteger feeOverhead;

  private BigInteger feeScalar;

  private BigInteger gas;

  private final SystemConfigUpdateType type;

  public SystemConfigUpdate(String address) {
    this.address = address;
    this.type = SystemConfigUpdateType.BatchSender;
  }

  public SystemConfigUpdate(
      BigInteger feeOverhead, BigInteger feeScalar) {
    this.feeOverhead = feeOverhead;
    this.feeScalar = feeScalar;
    this.type = SystemConfigUpdateType.Fees;
  }

  public SystemConfigUpdate(BigInteger gas) {
    this.gas = gas;
    this.type = SystemConfigUpdateType.Gas;
  }

  public String getAddress() {
    return address;
  }

  public BigInteger getFeeOverhead() {
    return feeOverhead;
  }

  public BigInteger getFeeScalar() {
    return feeScalar;
  }

  public BigInteger getGas() {
    return gas;
  }

  public SystemConfigUpdateType getType() {
    return type;
  }

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
