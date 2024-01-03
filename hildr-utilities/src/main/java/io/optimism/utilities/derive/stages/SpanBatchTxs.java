package io.optimism.utilities.derive.stages;

import static org.hyperledger.besu.ethereum.core.Transaction.REPLAY_PROTECTED_V_BASE;
import static org.hyperledger.besu.ethereum.core.Transaction.REPLAY_UNPROTECTED_V_BASE;

import com.google.common.primitives.Longs;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import org.apache.tuweni.bytes.Bytes;
import org.hyperledger.besu.datatypes.TransactionType;
import org.hyperledger.besu.ethereum.core.Transaction;
import org.hyperledger.besu.ethereum.core.encoding.EncodingContext;
import org.hyperledger.besu.ethereum.core.encoding.TransactionDecoder;
import org.jetbrains.annotations.NotNull;
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
    private List<String> txDatas;
    private List<Byte> txTypes;

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
            List<String> txDatas,
            List<Byte> txTypes,
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
    public void decodeContractCreationBits(byte[] contractCreationBit) {
        this.contractCreationBits =
                decodeSpanBatchBits(Unpooled.wrappedBuffer(contractCreationBit), (int) this.totalBlockTxCount);
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
    public void decodeProtectedBits(byte[] protectedBit) {
        this.protectedBits = decodeSpanBatchBits(Unpooled.wrappedBuffer(protectedBit), (int) this.totalLegacyTxCount);
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
    public void decodeYParityBits(byte[] yParityBit) {
        this.yParityBits = decodeSpanBatchBits(Unpooled.wrappedBuffer(yParityBit), (int) this.totalBlockTxCount);
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

    public void decodeTxSigsRS(byte[] txSigsBuffer) {
        this.txSigs = decodeSpanBatchTxSigsRS(txSigsBuffer);
    }

    public static List<SpanBatchSignature> decodeSpanBatchTxSigsRS(byte[] txSigsBuffer) {
        List<SpanBatchSignature> txSigs = new ArrayList<>();
        ByteBuf buffer = Unpooled.wrappedBuffer(txSigsBuffer);
        while (buffer.readableBytes() > 0) {
            var r = ByteBufUtil.getBytes(buffer.readBytes(32));
            var s = ByteBufUtil.getBytes(buffer.readBytes(32));
            BigInteger rInt = Numeric.toBigInt(r);
            BigInteger sInt = Numeric.toBigInt(s);
            txSigs.add(new SpanBatchSignature(BigInteger.ZERO, rInt, sInt));
        }
        return txSigs;
    }

    public byte[] encodeTxNonces() {
        ByteBuf buffer = Unpooled.buffer((int) (10 * totalBlockTxCount));
        for (BigInteger txNonce : txNonces) {
            putVarLong(txNonce.longValue(), buffer);
        }
        return ByteBufUtil.getBytes(buffer);
    }

    public void decodeTxNonces(byte[] txNoncesBuffer) {
        this.txNonces = decodeSpanBatchTxNonces(txNoncesBuffer);
    }

    public static List<BigInteger> decodeSpanBatchTxNonces(byte[] nonces) {
        return getBigIntegers(nonces);
    }

    public byte[] encodeTxGases() {
        ByteBuf buffer = Unpooled.buffer(10 * txGases.size());
        for (BigInteger txGas : txGases) {
            putVarLong(txGas.longValue(), buffer);
        }
        return ByteBufUtil.getBytes(buffer);
    }

    public void decodeTxGases(Bytes bytes) {
        this.txGases = decodeSpanBatchTxGases(bytes.toArray());
    }

    public static List<BigInteger> decodeSpanBatchTxGases(byte[] gases) {
        return getBigIntegers(gases);
    }

    public byte[] encodeTxTos() {
        ByteBuf result = PooledByteBufAllocator.DEFAULT.buffer(20 * txTos.size());
        for (String txTo : txTos) {
            byte[] addressBytes = Numeric.hexStringToByteArray(txTo);
            result.writeBytes(addressBytes);
        }
        return ByteBufUtil.getBytes(result);
    }

    public void decodeTxTos(byte[] txTosBuffer) {
        this.txTos = decodeSpanBatchTxTos(txTosBuffer);
    }

    public static List<String> decodeSpanBatchTxTos(byte[] txTosBuffer) {
        List<String> txTos = new ArrayList<>();
        ByteBuf buffer = Unpooled.wrappedBuffer(txTosBuffer);
        while (buffer.readableBytes() > 0) {
            ByteBuf addressBuf = buffer.readBytes(20);
            txTos.add(Numeric.toHexString(ByteBufUtil.getBytes(addressBuf)));
        }
        return txTos;
    }

    public static List<String> decodeSpanBatchTxDatas(byte[] txDatasBuffer) {
        TransactionDecoder.decodeOpaqueBytes(Bytes.wrap(txDatasBuffer), EncodingContext.BLOCK_BODY);
        List<String> txTos = new ArrayList<>();
        ByteBuf buffer = Unpooled.wrappedBuffer(txDatasBuffer);
        while (buffer.readableBytes() > 0) {
            ByteBuf addressBuf = buffer.readBytes(20);
            txTos.add(Numeric.toHexString(ByteBufUtil.getBytes(addressBuf)));
        }
        return txTos;
    }

    // No Test
    public void recoverV(BigInteger chainId) {
        if (this.txTypes.size() != this.txSigs.size()) {
            throw new RuntimeException("tx type length and tx sigs length mismatch");
        }
        for (int i = 0; i < this.txTypes.size(); i++) {
            BigInteger bit = this.yParityBits.testBit(i) ? BigInteger.ONE : BigInteger.ZERO;
            SpanBatchSignature newTxSig = getSpanBatchSignature(chainId, i, bit);
            this.txSigs.set(i, newTxSig);
        }
    }

    @NotNull private SpanBatchSignature getSpanBatchSignature(BigInteger chainId, int i, BigInteger bit) {
        BigInteger v;
        int type = this.txTypes.get(i);
        v = switch (type) {
            case 0 -> chainId.multiply(new BigInteger("2"))
                    .add(BigInteger.valueOf(35))
                    .add(bit);
            case 1, 2 -> bit;
            default -> throw new RuntimeException("invalid tx type:%d".formatted(this.txTypes.get(i)));};
        SpanBatchSignature old = this.txSigs.get(i);
        return new SpanBatchSignature(v, old.r(), old.s());
    }

    public static SpanBatchTxs newSpanBatchTxs(List<String> txs, BigInteger chainId) {
        long totalBlockTxCount = txs.size();
        BigInteger contractCreationBits = BigInteger.ZERO;
        BigInteger yParityBits = BigInteger.ZERO;
        BigInteger protectedBits = BigInteger.ZERO;
        List<SpanBatchSignature> txSigs = new ArrayList<>();
        List<String> txTos = new ArrayList<>();
        List<BigInteger> txNonces = new ArrayList<>();
        List<BigInteger> txGases = new ArrayList<>();
        List<String> txDatas = new ArrayList<>();
        List<Byte> txTypes = new ArrayList<>();
        long totalLegacyTxCount = 0;
        for (int idx = 0; idx < totalBlockTxCount; idx++) {
            String tx = txs.get(idx);
            Bytes txnBytes = Bytes.fromHexString(tx);
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
            txDatas.add(Numeric.toHexString(stxByte));
            txTypes.add(rawTransaction.getType().getEthSerializedType());
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

    public Long getTotalBlockTxCount() {
        return totalBlockTxCount;
    }

    public void setTotalBlockTxCount(Long totalBlockTxCount) {
        this.totalBlockTxCount = totalBlockTxCount;
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

    public List<String> getTxDatas() {
        return txDatas;
    }

    public void setTxDatas(List<String> txDatas) {
        this.txDatas = txDatas;
    }

    public List<Byte> getTxTypes() {
        return txTypes;
    }

    public void setTxTypes(List<Byte> txTypes) {
        this.txTypes = txTypes;
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

    private static List<BigInteger> getBigIntegers(byte[] gases) {
        List<BigInteger> txNonces = new ArrayList<>();
        ByteBuf buffer = Unpooled.wrappedBuffer(gases);
        while (buffer.readableBytes() > 0) {
            txNonces.add(Numeric.toBigInt(Longs.toByteArray(getVarLong(buffer))));
        }
        return txNonces;
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
