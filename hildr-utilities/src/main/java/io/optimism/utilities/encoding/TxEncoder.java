package io.optimism.utilities.encoding;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Suppliers;
import io.optimism.type.DepositTransaction;
import io.optimism.type.enums.TxType;
import io.optimism.utilities.rpc.response.OpEthBlock;
import java.math.BigInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.hyperledger.besu.crypto.SECPSignature;
import org.hyperledger.besu.crypto.SignatureAlgorithm;
import org.hyperledger.besu.crypto.SignatureAlgorithmFactory;
import org.hyperledger.besu.datatypes.AccessListEntry;
import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.datatypes.TransactionType;
import org.hyperledger.besu.datatypes.Wei;
import org.hyperledger.besu.ethereum.core.Transaction;
import org.hyperledger.besu.ethereum.core.encoding.EncodingContext;
import org.hyperledger.besu.ethereum.core.encoding.TransactionEncoder;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.utils.Numeric;

public class TxEncoder {

    private static final Supplier<SignatureAlgorithm> SIGNATURE_ALGORITHM =
            Suppliers.memoize(SignatureAlgorithmFactory::getInstance);

    public static final BigInteger REPLAY_UNPROTECTED_V_BASE = BigInteger.valueOf(27);
    public static final BigInteger REPLAY_UNPROTECTED_V_BASE_PLUS_1 = BigInteger.valueOf(28);

    public static final BigInteger REPLAY_PROTECTED_V_BASE = BigInteger.valueOf(35);

    // The v signature parameter starts at 36 because 1 is the first valid chainId so:
    // chainId > 1 implies that 2 * chainId + V_BASE > 36.
    public static final BigInteger REPLAY_PROTECTED_V_MIN = BigInteger.valueOf(36);

    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static byte[] encode(EthBlock.TransactionObject tx) {
        if (TxType.OPTIMISM_DEPOSIT.is(tx.getType())) {
            throw new IllegalArgumentException("this method not support deposit transaction");
        }
        return TransactionEncoder.encodeOpaqueBytes(web3jTxToBesuTx(tx), EncodingContext.BLOCK_BODY)
                .toArray();
    }

    public static DepositTransaction toDepositTx(OpEthBlock.TransactionObject tx, boolean isSystemTx) {
        return new DepositTransaction(
                tx.getSourceHash(),
                tx.getFrom(),
                tx.getTo(),
                Numeric.toBigInt(tx.getMint()),
                tx.getValue(),
                tx.getGas(),
                isSystemTx,
                tx.getInput());
    }

    public static byte[] encodeDepositTx(OpEthBlock.TransactionObject tx, boolean isSystemTx) {
        DepositTransaction depositTx = new DepositTransaction(
                tx.getSourceHash(),
                tx.getFrom(),
                tx.getTo(),
                Numeric.toBigInt(tx.getMint()),
                tx.getValue(),
                tx.getGas(),
                isSystemTx,
                tx.getInput());
        return depositTx.encode();
    }

    public static Transaction web3jTxToBesuTx(EthBlock.TransactionObject tx) {
        if (TxType.LEGACY.is(tx.getType())) {
            return toLegacyTx(tx);
        } else if (TxType.EIP1559.is(tx.getType())) {
            return toEIP1559Tx(tx);
        } else if (TxType.ACCESS_LIST.is(tx.getType())) {
            return toAccessListTx(tx);
        } else {
            throw new IllegalArgumentException("Unsupported transaction type: " + tx.getType());
        }
    }

    private static Transaction toLegacyTx(EthBlock.TransactionObject tx) {
        Transaction.Builder builder = Transaction.builder();
        builder.type(TxType.LEGACY.getBesuType())
                .chainId(BigInteger.valueOf(tx.getChainId()))
                .nonce(tx.getNonce().longValue())
                .gasPrice(Wei.of(tx.getGasPrice()))
                .gasLimit(tx.getGas().longValue())
                .to(Address.fromHexString(tx.getTo()))
                .value(Wei.of(tx.getValue()))
                .payload(Bytes.wrap(Numeric.hexStringToByteArray(tx.getInput())));
        byte recId = getRecIdFromLegacyTx(tx);
        final BigInteger r = Numeric.toBigInt(tx.getR());
        final BigInteger s = Numeric.toBigInt(tx.getS());
        final SECPSignature signature = SIGNATURE_ALGORITHM.get().createSignature(r, s, recId);
        builder.signature(signature);
        return builder.build();
    }

    private static Transaction toEIP1559Tx(EthBlock.TransactionObject tx) {
        Transaction.Builder builder = Transaction.builder();
        builder.type(TransactionType.EIP1559)
                .chainId(BigInteger.valueOf(tx.getChainId()))
                .nonce(tx.getNonce().longValue())
                .maxPriorityFeePerGas(Wei.of(tx.getMaxPriorityFeePerGas()))
                .maxFeePerGas(Wei.of(tx.getMaxFeePerGas()))
                .gasLimit(tx.getGas().longValue())
                .to(Address.fromHexString(tx.getTo()))
                .value(Wei.of(tx.getValue()))
                .payload(Bytes.wrap(Numeric.hexStringToByteArray(tx.getInput())))
                .accessList(tx.getAccessList().stream()
                        .map(accessListObj -> new AccessListEntry(
                                Address.fromHexString(accessListObj.getAddress()),
                                accessListObj.getStorageKeys().stream()
                                        .map(storageKey -> Bytes32.wrap(Numeric.hexStringToByteArray(storageKey)))
                                        .collect(Collectors.toList())))
                        .toList());
        final byte recId = BigInteger.valueOf(tx.getV()).byteValue();
        final BigInteger r = Numeric.toBigInt(tx.getR());
        final BigInteger s = Numeric.toBigInt(tx.getS());
        final SECPSignature signature = SIGNATURE_ALGORITHM.get().createSignature(r, s, recId);
        builder.signature(signature);
        return builder.build();
    }

    private static Transaction toAccessListTx(EthBlock.TransactionObject tx) {
        Transaction.Builder builder = Transaction.builder();
        builder.type(TransactionType.EIP1559)
                .chainId(BigInteger.valueOf(tx.getChainId()))
                .nonce(tx.getNonce().longValue())
                .gasPrice(Wei.of(tx.getGasPrice()))
                .gasLimit(tx.getGas().longValue())
                .to(Address.fromHexString(tx.getTo()))
                .value(Wei.of(tx.getValue()))
                .payload(Bytes.wrap(Numeric.hexStringToByteArray(tx.getInput())))
                .accessList(tx.getAccessList().stream()
                        .map(accessListObj -> new AccessListEntry(
                                Address.fromHexString(accessListObj.getAddress()),
                                accessListObj.getStorageKeys().stream()
                                        .map(storageKey -> Bytes32.wrap(Numeric.hexStringToByteArray(storageKey)))
                                        .collect(Collectors.toList())))
                        .toList());
        final byte recId = BigInteger.valueOf(tx.getV()).byteValue();
        final BigInteger r = Numeric.toBigInt(tx.getR());
        final BigInteger s = Numeric.toBigInt(tx.getS());
        final SECPSignature signature = SIGNATURE_ALGORITHM.get().createSignature(r, s, recId);
        builder.signature(signature);
        return builder.build();
    }

    private static byte getRecIdFromLegacyTx(EthBlock.TransactionObject tx) {
        BigInteger v = BigInteger.valueOf(tx.getV());
        byte recId;
        if (v.equals(REPLAY_UNPROTECTED_V_BASE) || v.equals(REPLAY_UNPROTECTED_V_BASE_PLUS_1)) {
            recId = v.subtract(REPLAY_UNPROTECTED_V_BASE).byteValueExact();
        } else if (v.compareTo(REPLAY_PROTECTED_V_MIN) > 0) {
            BigInteger chainId = v.subtract(REPLAY_PROTECTED_V_BASE).divide(BigInteger.TWO);
            recId = v.subtract(BigInteger.TWO.multiply(chainId).add(REPLAY_PROTECTED_V_BASE))
                    .byteValueExact();
        } else {
            throw new RuntimeException(String.format("An unsupported encoded `v` value of %s was found", v));
        }
        return recId;
    }
}
