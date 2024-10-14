package io.optimism.v2.derive.types;

import com.google.common.primitives.Bytes;
import io.optimism.config.Config;
import io.optimism.exceptions.InvalidSystemConfigUpdateException;
import io.optimism.rpc.response.OpEthBlock;
import io.optimism.v2.derive.types.enums.TxType;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.utils.Numeric;

/**
 * The system config.
 *
 * @param batcherAddr batch sender address.
 * @param gasLimit gas limit
 * @param overhead Pre-Ecotone this is passed as-is to engine.                          Post-Ecotone this is always zero, and not passed into the engine.
 * @param scalar scalar Pre-Ecotone this is passed as-is to the engine.                          Post-Ecotone this encodes multiple pieces of scalar data.
 * @param unsafeBlockSigner unsafe block signer address.
 * @author thinkAfCod
 * @since 0.1.1
 */
public record SystemConfig(
        String batcherAddr,
        BigInteger gasLimit,
        BigInteger overhead,
        BigInteger scalar,
        BigInteger baseFeeScalar,
        BigInteger blobBaseFeeScalar,
        BigInteger eip1559denominator,
        BigInteger eip1559Elasticity,
        String unsafeBlockSigner) {

    private static final String SUCCESS_STATUS = "0x1";

    private static final String TOPIC_0 = "0x1d2b0bda21d56b8bd12d4f94ebacffdfb35f5e226f84b461103bb8beab6353be";
    private static final String TOPIC_1 = "0x0000000000000000000000000000000000000000000000000000000000000000";

    private static final int UPDATE_BATCH_SENDER = 0;
    private static final int UPDATE_FEES_CONFIG = 1;
    private static final int UPDATE_GAS_LIMIT = 2;
    private static final int UPDATE_UNSAFE_BLOCK_SIGNER = 3;
    private static final int UPDATE_EIP1559 = 4;

    /**
     * Get base fee scalar.
     *
     * @return tuple contains blobBaseFeeScalar and baseFeeScalar
     */
    public Tuple2<BigInteger, BigInteger> ecotoneScalars() {
        var scalars = Numeric.toBytesPadded(scalar, 32);
        var versionByte = scalars[0];
        if (versionByte == 0) {
            // Bedrock version L1 base fee scalar
            var blobBaseFeeScalar = BigInteger.ZERO;
            var baseFeeScalar = Numeric.toBigInt(Arrays.copyOfRange(scalars, 28, scalars.length));
            return new Tuple2<>(blobBaseFeeScalar, baseFeeScalar);
        } else if (versionByte == 1) {
            // Ecotone version L1 base fee scalar
            var blobBaseFeeScalar = Numeric.toBigInt(Arrays.copyOfRange(scalars, 24, 28));
            var baseFeeScalar = Numeric.toBigInt(Arrays.copyOfRange(scalars, 28, scalars.length));
            return new Tuple2<>(blobBaseFeeScalar, baseFeeScalar);
        }
        throw new IllegalStateException("invalid l1FeeScalar");
    }

    public static SystemConfig fromOpBlock(OpEthBlock.Block opBlock, Config.ChainConfig chainConfig) {
        if (chainConfig.l2Genesis().number().equals(opBlock.getNumber())) {
            if (!chainConfig.l2Genesis().hash().equals(opBlock.getHash())) {
                // todo throw OpBlockConversionException InvalidGenesisHash
            }
            // todo ChainConfig SystemConfig
        }
        if (opBlock == null || CollectionUtils.isEmpty(opBlock.getTransactions())) {
            // todo throw OpBlockConversionException EmptyTransactions
        }
        EthBlock.TransactionObject depositTx =
                (EthBlock.TransactionObject) opBlock.getTransactions().getFirst();
        if (!depositTx.getType().equalsIgnoreCase(TxType.OPTIMISM_DEPOSIT.getString())) {
            // todo throw OpBlockConversionException InvalidTxType
        }

        L1BlockInfoTx l1Info = L1BlockInfoTx.decodeFrom(depositTx.getInput());
        BigInteger l1FeeScalar;
        if (l1Info.isBedrock()) {
            l1FeeScalar = l1Info.l1FeeScalar();
        } else {
            byte[] scalar = new byte[32];
            scalar[0] = L1BlockInfoTx.L1_SCALAR_ECOTONE;
            System.arraycopy(Numeric.toBytesPadded(l1Info.baseFeeScalar(), 4), 0, scalar, 24, 4);
            System.arraycopy(Numeric.toBytesPadded(l1Info.blobBaseFeeScalar(), 4), 0, scalar, 28, 4);
            l1FeeScalar = Numeric.toBigInt(scalar);
        }

        BigInteger eip1559Denominator = BigInteger.ZERO;
        BigInteger eip1559Elasticity = BigInteger.ZERO;
        if (chainConfig.isHolocene(opBlock.getTimestamp())) {
            var eip1559Params = opBlock.getNonce().toByteArray();
            var paramsLen = eip1559Params.length;
            eip1559Denominator = Numeric.toBigInt(eip1559Params, paramsLen - 8, 4);
            eip1559Elasticity = Numeric.toBigInt(eip1559Params, paramsLen - 4, 4);
        }

        return new SystemConfig(
                depositTx.getFrom(),
                opBlock.getGasLimit(),
                BigInteger.ZERO,
                l1FeeScalar,
                BigInteger.ZERO,
                BigInteger.ZERO,
                eip1559Denominator,
                eip1559Elasticity,
                null);
    }

    public SystemConfig updateByLogs(
            final List<EthLog.LogObject> logs, final String sysConfigAddr, final boolean ecotoneActive) {
        SystemConfig config = this;
        for (EthLog.LogObject logObj : logs) {
            Log log = logObj.get();
            List<String> topics = log.getTopics();
            if (!CollectionUtils.isEmpty(topics)) {
                config = processConfigUpdateLog(config, log, ecotoneActive);
            }
        }
        return config;
    }

    public SystemConfig updateByReceipts(
            final List<TransactionReceipt> txReceipts, final String sysConfigAddr, final boolean ecotoneActive) {
        SystemConfig config = this;
        for (TransactionReceipt receipt : txReceipts) {
            if (!SUCCESS_STATUS.equals(receipt.getStatus())) {
                continue;
            }
            for (Log log : receipt.getLogs()) {
                List<String> topics = log.getTopics();
                if (sysConfigAddr.equals(log.getAddress()) && !CollectionUtils.isEmpty(topics)) {
                    config = processConfigUpdateLog(config, log, ecotoneActive);
                }
            }
        }
        return config;
    }

    public static SystemConfig processConfigUpdateLog(SystemConfig config, Log log, boolean ecotoneActive) {
        SystemConfig update = config;
        if (log.getTopics().size() < 3) {
            // todo throw LogProcessingException invalid topic size
            return null;
        }
        if (TOPIC_0.equals(log.getTopics().getFirst())) {
            // todo throw LogProcessingException invalid topic
            return null;
        }
        if (log.getTopics().get(1) == null || !TOPIC_1.equals(log.getTopics().get(1))) {
            // todo throw LogProcessingException unsuported version
            return null;
        }

        byte[] decodeUpdateType = Numeric.hexStringToByteArray(log.getTopics().get(2));
        final int updateType = Numeric.toBigInt(decodeUpdateType).intValue();

        switch (updateType) {
            case UPDATE_BATCH_SENDER:

            case UPDATE_FEES_CONFIG:
                break;
            case UPDATE_GAS_LIMIT:
                break;
            case UPDATE_UNSAFE_BLOCK_SIGNER:
                break;
        }
        return update;
    }

    private static SystemConfig updateBatcherSender(SystemConfig config, String logData) {
        byte[] data = Numeric.hexStringToByteArray(logData);
        if (data.length != 96) {
            // todo throw SystemConfigUpdateException BatcherUpdateException invalid data len
            return null;
        }
        byte[] addrBytes = Arrays.copyOfRange(data, 76, 96);
        if (addrBytes.length != 20) {
            throw new InvalidSystemConfigUpdateException();
        }
        return new SystemConfig(
                Numeric.toHexString(addrBytes),
                config.gasLimit(),
                config.overhead(),
                config.scalar(),
                config.baseFeeScalar(),
                config.blobBaseFeeScalar(),
                config.eip1559denominator(),
                config.eip1559Elasticity(),
                config.unsafeBlockSigner());
    }

    private static SystemConfig updateGasConfig(SystemConfig config, String logData, boolean ecotoneActive) {
        byte[] data = Numeric.hexStringToByteArray(logData);
        if (data.length != 128) {
            // todo throw SystemConfigUpdateException GasConfigUpdateException invalid data len
            return null;
        }

        byte[] feeOverheadBytes = Arrays.copyOfRange(data, 64, 96);
        if (feeOverheadBytes.length != 32) {
            throw new InvalidSystemConfigUpdateException();
        }
        byte[] feeScalarBytes = Arrays.copyOfRange(data, 96, 128);
        if (feeScalarBytes.length != 32) {
            throw new InvalidSystemConfigUpdateException();
        }
        BigInteger feeOverhead = ecotoneActive ? BigInteger.ZERO : Numeric.toBigInt(feeOverheadBytes);
        BigInteger feeScalar = Numeric.toBigInt(feeScalarBytes);

        if (ecotoneActive && !isEcotoneScalar(feeScalarBytes)) {
            return config;
        }
        return new SystemConfig(
                config.batcherAddr(),
                config.gasLimit(),
                feeOverhead,
                feeScalar,
                config.baseFeeScalar(),
                config.blobBaseFeeScalar(),
                config.eip1559denominator(),
                config.eip1559Elasticity(),
                config.unsafeBlockSigner());
    }

    private static SystemConfig updateGasLimit(SystemConfig config, String logData) {
        byte[] data = Numeric.hexStringToByteArray(logData);
        if (data.length != 96) {
            // todo throw SystemConfigUpdateException GasLimitUpdateException invalid data len
            return null;
        }

        byte[] gasBytes = Arrays.copyOfRange(data, 64, 96);
        if (gasBytes.length != 32) {
            throw new InvalidSystemConfigUpdateException();
        }
        BigInteger gasLimit = Numeric.toBigInt(gasBytes);
        return new SystemConfig(
                config.batcherAddr(),
                gasLimit,
                config.overhead(),
                config.scalar(),
                config.baseFeeScalar(),
                config.blobBaseFeeScalar(),
                config.eip1559denominator(),
                config.eip1559Elasticity(),
                config.unsafeBlockSigner());
    }

    private static SystemConfig updateEip1559Params(SystemConfig config, String logData) {
        byte[] data = Numeric.hexStringToByteArray(logData);
        if (data.length != 96) {
            // todo throw SystemConfigUpdateException BatcherUpdateException invalid data len
            return null;
        }

        byte[] eip1559Denominator = Arrays.copyOfRange(data, 88, 92);
        byte[] eip1559Elasticity = Arrays.copyOfRange(data, 92, 96);
        return new SystemConfig(
                config.batcherAddr(),
                config.gasLimit(),
                config.overhead(),
                config.scalar(),
                config.baseFeeScalar(),
                config.blobBaseFeeScalar(),
                Numeric.toBigInt(eip1559Denominator),
                Numeric.toBigInt(eip1559Elasticity),
                config.unsafeBlockSigner());
    }

    private static boolean isEcotoneScalar(byte[] scalar) {
        var versionByte = (int) scalar[0];
        if (versionByte == 0) {
            byte[] bedrockScalar = Arrays.copyOfRange(scalar, 1, 28);
            return isAllZeros(bedrockScalar);
        } else if (versionByte == 1) {
            byte[] ecotoneScalar = Arrays.copyOfRange(scalar, 1, 24);
            return isAllZeros(ecotoneScalar);
        }
        return false;
    }

    public static boolean isAllZeros(byte[] array) {
        return array != null && Bytes.indexOf(array, (byte) 0) == -1;
    }
}
