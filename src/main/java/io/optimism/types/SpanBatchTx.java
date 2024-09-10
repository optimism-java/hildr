package io.optimism.types;

import static org.hyperledger.besu.ethereum.core.Transaction.REPLAY_PROTECTED_V_BASE;
import static org.hyperledger.besu.ethereum.core.Transaction.REPLAY_PROTECTED_V_MIN;
import static org.hyperledger.besu.ethereum.core.Transaction.REPLAY_UNPROTECTED_V_BASE;
import static org.hyperledger.besu.ethereum.core.Transaction.REPLAY_UNPROTECTED_V_BASE_PLUS_1;
import static org.hyperledger.besu.ethereum.core.Transaction.TWO;

import com.google.common.base.Suppliers;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.function.Supplier;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.crypto.SECPSignature;
import org.hyperledger.besu.crypto.SignatureAlgorithm;
import org.hyperledger.besu.crypto.SignatureAlgorithmFactory;
import org.hyperledger.besu.datatypes.Address;
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

    /**
     * The constant SIGNATURE_ALGORITHM.
     */
    public static final Supplier<SignatureAlgorithm> SIGNATURE_ALGORITHM =
            Suppliers.memoize(SignatureAlgorithmFactory::getInstance);

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
        switch (txType()) {
            case FRONTIER -> {
                SpanBatchLegacyTxData spanBatchLegacyTxData = (SpanBatchLegacyTxData) this.spanBatchTxData;
                return spanBatchLegacyTxData.encode();
            }
            case EIP1559 -> {
                SpanBatchDynamicFeeTxData spanBatchDynamicFeeTxData = (SpanBatchDynamicFeeTxData) this.spanBatchTxData;
                return spanBatchDynamicFeeTxData.encode();
            }
            case ACCESS_LIST -> {
                SpanBatchAccessListTxData spanBatchAccessListTxData = (SpanBatchAccessListTxData) this.spanBatchTxData;
                return spanBatchAccessListTxData.encode();
            }
            case BLOB -> throw new RuntimeException("blob tx not supported");
            default -> throw new IllegalStateException("unexpected value: %s".formatted(txType()));
        }
    }

    /**
     * Unmarshal binary span batch tx data.
     *
     * @param b the b
     * @return the span batch tx data
     */
    public static SpanBatchTx unmarshalBinary(byte[] b) {
        if (b.length <= 1) {
            throw new RuntimeException("typed transaction too short");
        }
        if ((b[0] & 0xFF) > 0x7F) {
            RLPInput input = new BytesValueRLPInput(Bytes.wrap(b), false);
            return new SpanBatchTx(SpanBatchLegacyTxData.decode(input));
        }

        byte[] spanBatchData = ArrayUtils.subarray(b, 1, b.length);
        RLPInput input = new BytesValueRLPInput(Bytes.wrap(spanBatchData), false);
        if (TransactionType.ACCESS_LIST.getEthSerializedType() == b[0]) {
            return new SpanBatchTx(SpanBatchAccessListTxData.decode(input));
        }
        if (TransactionType.EIP1559.getEthSerializedType() == b[0]) {
            return new SpanBatchTx(SpanBatchDynamicFeeTxData.decode(input));
        }

        throw new RuntimeException("invalid typed transaction type");
    }

    /**
     * Gets span batch tx data.
     *
     * @return the span batch tx data
     */
    public SpanBatchTxData getSpanBatchTxData() {
        return spanBatchTxData;
    }

    /**
     * Convert to full tx transaction.
     *
     * @param nonce   the nonce
     * @param gas     the gas
     * @param to      the to
     * @param chainId the chain id
     * @param v       the v
     * @param r       the r
     * @param s       the s
     * @return the transaction
     */
    public Transaction convertToFullTx(
            BigInteger nonce, BigInteger gas, String to, BigInteger chainId, BigInteger v, BigInteger r, BigInteger s) {

        switch (txType()) {
            case FRONTIER -> {
                SpanBatchLegacyTxData spanBatchLegacyTxData = (SpanBatchLegacyTxData) this.spanBatchTxData;

                var recIdAndProtectedTx = getRecId(chainId, v);
                byte recId = recIdAndProtectedTx.getLeft();
                boolean protectedTx = recIdAndProtectedTx.getRight();
                final SECPSignature signature = SIGNATURE_ALGORITHM.get().createSignature(r, s, recId);

                var builder = Transaction.builder()
                        .type(TransactionType.FRONTIER)
                        .nonce(nonce.longValue())
                        .gasPrice(spanBatchLegacyTxData.gasPrice())
                        .gasLimit(gas.longValue())
                        .to(to == null ? null : Address.fromHexString(to))
                        .value(spanBatchLegacyTxData.value())
                        .payload(spanBatchLegacyTxData.data())
                        .signature(signature);

                if (protectedTx) {
                    builder.chainId(chainId);
                }

                return builder.build();
            }
            case EIP1559 -> {
                SpanBatchDynamicFeeTxData spanBatchDynamicFeeTxData = (SpanBatchDynamicFeeTxData) this.spanBatchTxData;
                final SECPSignature signature = SIGNATURE_ALGORITHM.get().createSignature(r, s, v.byteValueExact());

                return Transaction.builder()
                        .type(TransactionType.EIP1559)
                        .nonce(nonce.longValue())
                        .maxPriorityFeePerGas(spanBatchDynamicFeeTxData.gasTipCap())
                        .maxFeePerGas(spanBatchDynamicFeeTxData.gasFeeCap())
                        .gasLimit(gas.longValue())
                        .to(to == null ? null : Address.fromHexString(to))
                        .value(spanBatchDynamicFeeTxData.value())
                        .payload(spanBatchDynamicFeeTxData.data())
                        .accessList(spanBatchDynamicFeeTxData.accessList())
                        .chainId(chainId)
                        .signature(signature)
                        .build();
            }
            case ACCESS_LIST -> {
                SpanBatchAccessListTxData spanBatchAccessListTxData = (SpanBatchAccessListTxData) this.spanBatchTxData;
                final SECPSignature signature = SIGNATURE_ALGORITHM.get().createSignature(r, s, v.byteValueExact());

                return Transaction.builder()
                        .type(TransactionType.ACCESS_LIST)
                        .nonce(nonce.longValue())
                        .gasPrice(spanBatchAccessListTxData.gasPrice())
                        .gasLimit(gas.longValue())
                        .to(to == null ? null : Address.fromHexString(to))
                        .value(spanBatchAccessListTxData.value())
                        .payload(spanBatchAccessListTxData.data())
                        .accessList(spanBatchAccessListTxData.accessList())
                        .chainId(chainId)
                        .signature(signature)
                        .build();
            }
            case BLOB -> throw new RuntimeException("blob tx not supported");
            default -> throw new IllegalStateException("unexpected value: %s".formatted(txType()));
        }
    }

    private static Pair<Byte, Boolean> getRecId(BigInteger chainId, BigInteger v) {
        byte recId;
        boolean protectedTx = true;
        if (v.equals(REPLAY_UNPROTECTED_V_BASE) || v.equals(REPLAY_UNPROTECTED_V_BASE_PLUS_1)) {
            recId = v.subtract(REPLAY_UNPROTECTED_V_BASE).byteValueExact();
            protectedTx = false;
        } else if (v.compareTo(REPLAY_PROTECTED_V_MIN) > 0) {
            recId = v.subtract(TWO.multiply(chainId).add(REPLAY_PROTECTED_V_BASE))
                    .byteValueExact();
        } else {
            throw new RuntimeException(String.format("An unsupported encoded `v` value of %s was found", v));
        }
        return Pair.of(recId, protectedTx);
    }
}
