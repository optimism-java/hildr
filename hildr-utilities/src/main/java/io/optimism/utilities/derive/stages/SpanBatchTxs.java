package io.optimism.utilities.derive.stages;

import static org.hyperledger.besu.ethereum.core.Transaction.REPLAY_PROTECTED_V_BASE;
import static org.hyperledger.besu.ethereum.core.Transaction.REPLAY_UNPROTECTED_V_BASE;

import com.google.common.primitives.Longs;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.PooledByteBufAllocator;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.MutableBytes;
import org.hyperledger.besu.datatypes.TransactionType;
import org.hyperledger.besu.ethereum.core.Transaction;
import org.hyperledger.besu.ethereum.core.encoding.EncodingContext;
import org.hyperledger.besu.ethereum.core.encoding.TransactionDecoder;
import org.hyperledger.besu.ethereum.core.encoding.TransactionEncoder;
import org.hyperledger.besu.ethereum.rlp.BytesValueRLPInput;
import org.hyperledger.besu.ethereum.rlp.RLPInput;
import org.web3j.utils.Numeric;

public class SpanBatchTxs {

    private static final long MaxSpanBatchSize = 10000000L;
    private long totalBlockTxCount;
    private BigInteger contractCreationBits;
    private BigInteger yParityBits;
    private List<SpanBatchSignature> txSigs;
    private List<BigInteger> txNonces;
    private List<BigInteger> txGases;
    private List<String> txTos;
    private List<Bytes> txDatas;
    private List<TransactionType> txTypes;

    private long totalLegacyTxCount;

    private BigInteger protectedBits;

    public SpanBatchTxs() {}

    public SpanBatchTxs(
            Long totalBlockTxCount,
            BigInteger contractCreationBits,
            BigInteger yParityBits,
            List<SpanBatchSignature> txSigs,
            List<BigInteger> txNonces,
            List<BigInteger> txGases,
            List<String> txTos,
            List<Bytes> txDatas,
            List<TransactionType> txTypes,
            long totalLegacyTxCount,
            BigInteger protectedBits) {
        this.totalBlockTxCount = totalBlockTxCount;
        this.contractCreationBits = contractCreationBits;
        this.yParityBits = yParityBits;
        this.txSigs = txSigs;
        this.txNonces = txNonces;
        this.txGases = txGases;
        this.txTos = txTos;
        this.txDatas = txDatas;
        this.txTypes = txTypes;
        this.totalLegacyTxCount = totalLegacyTxCount;
        this.protectedBits = protectedBits;
    }

    /**
     * contractCreationBits is bitlist right-padded to a multiple of 8 bits.
     *
     * @return encoded contract creation bits.
     */
    public byte[] encodeContractCreationBits() {
        return encodeSpanBatchBits((int) this.totalBlockTxCount, this.contractCreationBits);
    }

    /**
     * contractCreationBits is bitlist right-padded to a multiple of 8 bits.
     *
     * @param contractCreationBit encoded contract creation bits.
     */
    public void decodeContractCreationBits(ByteBuf contractCreationBit) {
        this.contractCreationBits = decodeSpanBatchBits(contractCreationBit, (int) this.totalBlockTxCount);
    }

    /**
     * protectedBits is bitlist right-padded to a multiple of 8 bits.
     *
     * @return encoded protected bits.
     */
    public byte[] encodeProtectedBits() {
        return encodeSpanBatchBits((int) this.totalLegacyTxCount, this.protectedBits);
    }

    /**
     * protectedBits is bitlist right-padded to a multiple of 8 bits.
     *
     * @param protectedBit encoded protected bits.
     */
    public void decodeProtectedBits(ByteBuf protectedBit) {
        this.protectedBits = decodeSpanBatchBits(protectedBit, (int) this.totalLegacyTxCount);
    }

    /**
     * Counts the number of contract creations in the batch.
     *
     * @return the number of contract creations in the batch.
     */
    public long contractCreationCount() {
        if (contractCreationBits == null) {
            throw new RuntimeException("dev error: contract creation bits not set");
        }
        long result = 0L;
        for (int i = 0; i < this.totalBlockTxCount; i++) {
            if (contractCreationBits.testBit(i)) {
                result++;
            }
        }
        return result;
    }

    /**
     * yParityBits is bitlist right-padded to a multiple of 8 bits.
     *
     * @return encoded y parity bits.
     */
    public byte[] encodeYParityBits() {
        return encodeSpanBatchBits((int) this.totalBlockTxCount, this.yParityBits);
    }

    /**
     * yParityBits is bitlist right-padded to a multiple of 8 bits.
     *
     * @param yParityBit encoded y parity bits.
     */
    public void decodeYParityBits(ByteBuf yParityBit) {
        this.yParityBits = decodeSpanBatchBits(yParityBit, (int) this.totalBlockTxCount);
    }

    public byte[] encodeTxSigsRS() {
        ByteBuf result = PooledByteBufAllocator.DEFAULT.buffer(64 * txSigs.size());
        for (SpanBatchSignature txSig : txSigs) {
            byte[] rBytes = Numeric.toBytesPadded(txSig.r(), 32);
            byte[] sBytes = Numeric.toBytesPadded(txSig.s(), 32);
            result.writeBytes(rBytes);
            result.writeBytes(sBytes);
        }
        return ByteBufUtil.getBytes(result);
    }

    public void decodeTxSigsRS(ByteBuf txSigsBuffer) {
        List<SpanBatchSignature> txSigs = new ArrayList<>();
        for (int i = 0; i < totalBlockTxCount; i++) {
            var r = ByteBufUtil.getBytes(txSigsBuffer.readBytes(32));
            var s = ByteBufUtil.getBytes(txSigsBuffer.readBytes(32));
            BigInteger rInt = Numeric.toBigInt(r);
            BigInteger sInt = Numeric.toBigInt(s);
            txSigs.add(new SpanBatchSignature(BigInteger.ZERO, rInt, sInt));
        }

        this.txSigs = txSigs;
    }

    public byte[] encodeTxNonces() {
        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer(10 * txNonces.size());
        for (BigInteger txNonce : txNonces) {
            putVarLong(txNonce.longValue(), buffer);
        }
        return ByteBufUtil.getBytes(buffer);
    }

    public void decodeTxNonces(ByteBuf txNoncesBuffer) {
        List<BigInteger> txNonces = new ArrayList<>();

        for (int i = 0; i < totalBlockTxCount; i++) {
            txNonces.add(Numeric.toBigInt(Longs.toByteArray(getVarLong(txNoncesBuffer))));
        }

        this.txNonces = txNonces;
    }

    public byte[] encodeTxGases() {
        ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer(10 * txGases.size());
        for (BigInteger txGas : txGases) {
            putVarLong(txGas.longValue(), buffer);
        }
        return ByteBufUtil.getBytes(buffer);
    }

    public void decodeTxGases(ByteBuf gases) {
        List<BigInteger> txNonces = new ArrayList<>();

        for (int i = 0; i < totalBlockTxCount; i++) {
            txNonces.add(Numeric.toBigInt(Longs.toByteArray(getVarLong(gases))));
        }

        this.txGases = txNonces;
    }

    public byte[] encodeTxTos() {
        ByteBuf result = PooledByteBufAllocator.DEFAULT.buffer(20 * txTos.size());
        for (String txTo : txTos) {
            byte[] addressBytes = Numeric.hexStringToByteArray(txTo);
            result.writeBytes(addressBytes);
        }
        return ByteBufUtil.getBytes(result);
    }

    public void decodeTxTos(ByteBuf txTosBuffer) {
        List<String> txTos = new ArrayList<>();
        var contractCreationCount = contractCreationCount();
        for (int i = 0; i < totalBlockTxCount - contractCreationCount; i++) {
            ByteBuf addressBuf = txTosBuffer.readBytes(20);
            txTos.add(Numeric.toHexString(ByteBufUtil.getBytes(addressBuf)));
        }

        this.txTos = txTos;
    }

    public byte[] encodeTxDatas() {
        ByteBuf result = PooledByteBufAllocator.DEFAULT.buffer();
        for (Bytes txData : txDatas) {
            result.writeBytes(txData.toArrayUnsafe());
        }
        return ByteBufUtil.getBytes(result);
    }

    public void decodeTxDatas(ByteBuf txDatasBuffer) {
        List<Bytes> txDatas = new ArrayList<>();
        List<TransactionType> txTypes = new ArrayList<>();

        System.out.println(txDatasBuffer.readerIndex());
        for (int i = 0; i < totalBlockTxCount; i++) {
            Bytes remaining =
                    Bytes.wrapByteBuf(txDatasBuffer, txDatasBuffer.readerIndex(), txDatasBuffer.readableBytes());
            System.out.println(i);
            if (i == 1886) {
                System.out.println("here");
            }
            RLPInput input = new BytesValueRLPInput(remaining, false, false);
            var tx = readTxData(input, i == totalBlockTxCount - 1);
            Bytes txData = tx.getLeft();
            System.out.println(txData.toHexString());
            TransactionType txType = tx.getRight();
            txDatas.add(txData);
            txTypes.add(txType);
            if (txType == TransactionType.FRONTIER) {
                totalLegacyTxCount++;
            }
            txDatasBuffer.readerIndex(txDatasBuffer.readerIndex() + txData.size());
            System.out.println(txDatasBuffer.readerIndex());
        }

        System.out.println(txDatasBuffer.readerIndex());
        this.txDatas = txDatas;
        this.txTypes = txTypes;
    }

    public void recoverV(BigInteger chainId) {
        if (this.txTypes.size() != this.txSigs.size()) {
            throw new RuntimeException("tx type length and tx sigs length mismatch");
        }

        if (this.protectedBits == null) {
            throw new RuntimeException("protected bits not set");
        }

        for (int i = 0; i < this.txTypes.size(); i++) {
            BigInteger bit = this.yParityBits.testBit(i) ? BigInteger.ONE : BigInteger.ZERO;
            BigInteger v;
            switch (this.txTypes.get(i)) {
                case FRONTIER:
                    if (this.protectedBits.testBit(i)) {
                        v = chainId.multiply(BigInteger.TWO)
                                .add(REPLAY_PROTECTED_V_BASE)
                                .add(bit);
                    } else {
                        v = bit.add(REPLAY_UNPROTECTED_V_BASE);
                    }
                    break;
                case EIP1559:
                case ACCESS_LIST:
                    v = bit;
                    break;
                default:
                    throw new RuntimeException("invalid tx type:%s".formatted(this.txTypes.get(i)));
            }
            this.txSigs.get(i).setV(v);
        }
    }

    public byte[] encode() {
        ByteBuf result = PooledByteBufAllocator.DEFAULT.buffer();
        result.writeBytes(encodeContractCreationBits());
        result.writeBytes(encodeYParityBits());
        result.writeBytes(encodeTxSigsRS());
        result.writeBytes(encodeTxTos());
        result.writeBytes(encodeTxDatas());
        result.writeBytes(encodeTxNonces());
        result.writeBytes(encodeTxGases());
        result.writeBytes(encodeProtectedBits());
        return ByteBufUtil.getBytes(result);
    }

    public void decode(ByteBuf buffer) {
        decodeContractCreationBits(buffer);
        decodeYParityBits(buffer);
        decodeTxSigsRS(buffer);
        decodeTxTos(buffer);
        decodeTxDatas(buffer);
        decodeTxNonces(buffer);
        decodeTxGases(buffer);
        decodeProtectedBits(buffer);
    }

    public List<byte[]> fullTxs(BigInteger chainId) {
        List<byte[]> fullTxs = new ArrayList<>();
        int toIdx = 0;
        for (int i = 0; i < this.totalBlockTxCount; i++) {
            SpanBatchTx spanBatchTx =
                    SpanBatchTx.unmarshalBinary(this.txDatas.get(i).toArrayUnsafe());
            BigInteger nonce = this.txNonces.get(i);
            BigInteger gas = this.txGases.get(i);
            String to = null;
            if (!this.contractCreationBits.testBit(i)) {
                if (this.txTos.size() <= toIdx) {
                    throw new RuntimeException("tx to not enough");
                }
                to = this.txTos.get(toIdx);
                toIdx++;
            }
            BigInteger v = this.txSigs.get(i).v();
            BigInteger r = this.txSigs.get(i).r();
            BigInteger s = this.txSigs.get(i).s();
            Transaction tx = spanBatchTx.convertToFullTx(nonce, gas, to, chainId, v, r, s);
            Bytes txBytes = TransactionEncoder.encodeOpaqueBytes(tx, EncodingContext.BLOCK_BODY);
            fullTxs.add(txBytes.toArrayUnsafe());
        }
        return fullTxs;
    }

    public static SpanBatchTxs newSpanBatchTxs(List<byte[]> txs, BigInteger chainId) {
        long totalBlockTxCount = txs.size();
        BigInteger contractCreationBits = BigInteger.ZERO;
        BigInteger yParityBits = BigInteger.ZERO;
        BigInteger protectedBits = BigInteger.ZERO;
        List<SpanBatchSignature> txSigs = new ArrayList<>();
        List<String> txTos = new ArrayList<>();
        List<BigInteger> txNonces = new ArrayList<>();
        List<BigInteger> txGases = new ArrayList<>();
        List<Bytes> txDatas = new ArrayList<>();
        List<TransactionType> txTypes = new ArrayList<>();
        long totalLegacyTxCount = 0;
        for (int idx = 0; idx < totalBlockTxCount; idx++) {
            byte[] tx = txs.get(idx);
            Bytes txnBytes = Bytes.wrap(tx);
            Transaction rawTransaction = TransactionDecoder.decodeOpaqueBytes(txnBytes, EncodingContext.BLOCK_BODY);
            if (rawTransaction.getType() == TransactionType.FRONTIER) {
                if (rawTransaction.getChainId().isPresent()) {
                    protectedBits = protectedBits.setBit((int) totalLegacyTxCount);
                } else {
                    protectedBits = protectedBits.clearBit((int) totalLegacyTxCount);
                }
                totalLegacyTxCount++;
            }
            if (rawTransaction.getChainId().isPresent()
                    && rawTransaction.getChainId().get().compareTo(chainId) != 0) {
                throw new RuntimeException("chainId mismatch. tx has chain ID:%d, expected:%d, but expected chain ID:%d"
                        .formatted(rawTransaction.getChainId().get(), chainId, chainId));
            }

            SpanBatchSignature txSig = new SpanBatchSignature(
                    rawTransaction.getType() == TransactionType.FRONTIER
                            ? rawTransaction.getV()
                            : rawTransaction.getYParity(),
                    rawTransaction.getR(),
                    rawTransaction.getS());
            txSigs.add(txSig);
            if (rawTransaction.getTo().isPresent()) {
                txTos.add(rawTransaction.getTo().get().toHexString());
                contractCreationBits = contractCreationBits.clearBit(idx);
            } else {
                contractCreationBits = contractCreationBits.setBit(idx);
            }
            BigInteger yParityBit = convertVToYParity(rawTransaction);
            if (yParityBit.equals(BigInteger.ONE)) {
                yParityBits = yParityBits.setBit(idx);
            } else if (yParityBit.equals(BigInteger.ZERO)) {
                yParityBits = yParityBits.clearBit(idx);
            } else {
                throw new RuntimeException("invalid y parity bit:%d".formatted(yParityBit));
            }
            txNonces.add(Numeric.toBigInt(Longs.toByteArray(rawTransaction.getNonce())));
            txGases.add(Numeric.toBigInt(Longs.toByteArray(rawTransaction.getGasLimit())));
            SpanBatchTx stx = SpanBatchTx.newSpanBatchTx(rawTransaction);
            byte[] stxByte = stx.marshalBinary();
            txDatas.add(Bytes.wrap(stxByte));
            txTypes.add(rawTransaction.getType());
        }
        return new SpanBatchTxs(
                totalBlockTxCount,
                contractCreationBits,
                yParityBits,
                txSigs,
                txNonces,
                txGases,
                txTos,
                txDatas,
                txTypes,
                totalLegacyTxCount,
                protectedBits);
    }

    public BigInteger getContractCreationBits() {
        return contractCreationBits;
    }

    public void setContractCreationBits(BigInteger contractCreationBits) {
        this.contractCreationBits = contractCreationBits;
    }

    public BigInteger getyParityBits() {
        return yParityBits;
    }

    public void setyParityBits(BigInteger yParityBits) {
        this.yParityBits = yParityBits;
    }

    public List<SpanBatchSignature> getTxSigs() {
        return txSigs;
    }

    public void setTxSigs(List<SpanBatchSignature> txSigs) {
        this.txSigs = txSigs;
    }

    public List<BigInteger> getTxNonces() {
        return txNonces;
    }

    public void setTxNonces(List<BigInteger> txNonces) {
        this.txNonces = txNonces;
    }

    public List<BigInteger> getTxGases() {
        return txGases;
    }

    public void setTxGases(List<BigInteger> txGases) {
        this.txGases = txGases;
    }

    public List<String> getTxTos() {
        return txTos;
    }

    public void setTxTos(List<String> txTos) {
        this.txTos = txTos;
    }

    public List<Bytes> getTxDatas() {
        return txDatas;
    }

    public void setTxDatas(List<Bytes> txDatas) {
        this.txDatas = txDatas;
    }

    public List<TransactionType> getTxTypes() {
        return txTypes;
    }

    public void setTxTypes(List<TransactionType> txTypes) {
        this.txTypes = txTypes;
    }

    public void setTotalBlockTxCount(long totalBlockTxCount) {
        this.totalBlockTxCount = totalBlockTxCount;
    }

    public long getTotalBlockTxCount() {
        return totalBlockTxCount;
    }

    public BigInteger getProtectedBits() {
        return protectedBits;
    }

    public long getTotalLegacyTxCount() {
        return totalLegacyTxCount;
    }

    public void setTotalLegacyTxCount(long totalLegacyTxCount) {
        this.totalLegacyTxCount = totalLegacyTxCount;
    }

    public static BigInteger convertVToYParity(Transaction transaction) {
        switch (transaction.getType()) {
            case FRONTIER -> {
                if (transaction.getChainId().isPresent()) {
                    return transaction.getV().subtract(REPLAY_PROTECTED_V_BASE).and(BigInteger.ONE);
                } else {
                    return transaction.getV().subtract(REPLAY_UNPROTECTED_V_BASE);
                }
            }
            case EIP1559, ACCESS_LIST -> {
                return transaction.getYParity();
            }
            default -> throw new RuntimeException("invalid tx type:%s".formatted(transaction.getType()));
        }
    }

    public static BigInteger decodeSpanBatchBits(ByteBuf source, int bitLength) {
        var bufLen = bitLength / 8;
        if (bitLength % 8 != 0) {
            bufLen++;
        }
        if (bufLen > MaxSpanBatchSize) {
            throw new RuntimeException("span batch size limit reached");
        }
        byte[] buf = new byte[(int) bufLen];
        try {
            source.readBytes(buf);
        } catch (Exception e) {
            throw new RuntimeException("read error");
        }
        var res = Numeric.toBigInt(buf);
        if (res.bitLength() > bitLength) {
            throw new RuntimeException("invalid bit length");
        }
        return res;
    }

    public static byte[] encodeSpanBatchBits(int bitLength, BigInteger bits) {
        if (bits.bitLength() > bitLength) {
            throw new RuntimeException(
                    "bitfield is larger than bitLength: %d > %d".formatted(bits.bitLength(), bitLength));
        }

        var bufLen = bitLength / 8;
        if (bitLength % 8 != 0) {
            bufLen++;
        }
        if (bufLen > MaxSpanBatchSize) {
            throw new RuntimeException("span batch size limit reached");
        }
        return Numeric.toBytesPadded(bits, bufLen);
    }

    public static Pair<Bytes, TransactionType> readTxData(RLPInput input, boolean isLast) {
        if (isTypedTransaction(input)) {
            final Bytes typedTransactionBytes = input.readBytes();
            Bytes txBytes = isLast ? lastCurrentListAsBytes(input) : input.currentListAsBytes();
            var transactionType = getTransactionType(typedTransactionBytes).orElseThrow();
            return Pair.of(Bytes.concatenate(typedTransactionBytes, txBytes), transactionType);
        } else {
            Bytes bytes = isLast ? lastCurrentListAsBytes(input) : input.currentListAsBytes();
            return Pair.of(bytes, TransactionType.FRONTIER);
        }
    }

    public static Bytes lastCurrentListAsBytes(RLPInput input) {
        if (!input.nextIsList()) {
            throw new RuntimeException("Cannot read list, current item is not a list list");
        }

        var currentPayloadSize = input.nextSize();
        var currentOffset = input.nextOffset();
        final MutableBytes scratch = MutableBytes.create(currentPayloadSize + 10);
        final int headerSize = RLPEncodingHelpers.writeListHeader(currentPayloadSize, scratch, 0);
        input.raw().slice(currentOffset, currentPayloadSize).copyTo(scratch, headerSize);

        return scratch.slice(0, currentPayloadSize + headerSize);
    }

    /**
     * Checks if the given RLP input is a typed transaction.
     *
     * <p>See EIP-2718
     *
     * <p>If it starts with a value in the range [0, 0x7f] then it is a new transaction type
     *
     * <p>if it starts with a value in the range [0xc0, 0xfe] then it is a legacy transaction type
     *
     * @param rlpInput the RLP input
     * @return true if the RLP input is a typed transaction, false otherwise
     */
    private static boolean isTypedTransaction(final RLPInput rlpInput) {
        return !rlpInput.nextIsList();
    }

    /**
     * Retrieves the transaction type from the provided bytes. The method attempts to extract the
     * first byte from the input bytes and interpret it as a transaction type. If the byte does not
     * correspond to a valid transaction type, the method returns an empty Optional.
     *
     * @param opaqueBytes the bytes from which to extract the transaction type
     * @return an Optional containing the TransactionType if the first byte of the input corresponds
     * to a valid transaction type, or an empty Optional if it does not
     */
    private static Optional<TransactionType> getTransactionType(final Bytes opaqueBytes) {
        try {
            byte transactionTypeByte = opaqueBytes.get(0);
            return Optional.of(TransactionType.of(transactionTypeByte));
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    /**
     * Reads an up to 64 bit long varint from the current position of the
     * given ByteBuffer and returns the decoded value as long.
     *
     * <p>The position of the buffer is advanced to the first byte after the
     * decoded varint.
     *
     * @param src the ByteBuffer to get the var int from
     * @return The integer value of the decoded long varint
     */
    public static long getVarLong(ByteBuf src) {
        long tmp;
        if ((tmp = src.readByte()) >= 0) {
            return tmp;
        }
        long result = tmp & 0x7f;
        if ((tmp = src.readByte()) >= 0) {
            result |= tmp << 7;
        } else {
            result |= (tmp & 0x7f) << 7;
            if ((tmp = src.readByte()) >= 0) {
                result |= tmp << 14;
            } else {
                result |= (tmp & 0x7f) << 14;
                if ((tmp = src.readByte()) >= 0) {
                    result |= tmp << 21;
                } else {
                    result |= (tmp & 0x7f) << 21;
                    if ((tmp = src.readByte()) >= 0) {
                        result |= tmp << 28;
                    } else {
                        result |= (tmp & 0x7f) << 28;
                        if ((tmp = src.readByte()) >= 0) {
                            result |= tmp << 35;
                        } else {
                            result |= (tmp & 0x7f) << 35;
                            if ((tmp = src.readByte()) >= 0) {
                                result |= tmp << 42;
                            } else {
                                result |= (tmp & 0x7f) << 42;
                                if ((tmp = src.readByte()) >= 0) {
                                    result |= tmp << 49;
                                } else {
                                    result |= (tmp & 0x7f) << 49;
                                    if ((tmp = src.readByte()) >= 0) {
                                        result |= tmp << 56;
                                    } else {
                                        result |= (tmp & 0x7f) << 56;
                                        result |= ((long) src.readByte()) << 63;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * Encodes a long integer in a variable-length encoding, 7 bits per byte, to a
     * ByteBuffer sink.
     *
     * @param v    the value to encode
     * @param sink the ByteBuffer to add the encoded value
     */
    public static void putVarLong(long v, ByteBuf sink) {
        while (true) {
            int bits = ((int) v) & 0x7f;
            v >>>= 7;
            if (v == 0) {
                sink.writeByte((byte) bits);
                return;
            }
            sink.writeByte((byte) (bits | 0x80));
        }
    }
}
