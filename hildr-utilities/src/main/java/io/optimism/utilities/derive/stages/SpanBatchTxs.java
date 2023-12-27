package io.optimism.utilities.derive.stages;

import static org.web3j.crypto.transaction.type.TransactionType.EIP1559;
import static org.web3j.crypto.transaction.type.TransactionType.EIP2930;
import static org.web3j.crypto.transaction.type.TransactionType.LEGACY;

import io.libp2p.etc.types.ByteBufExtKt;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.apache.tuweni.bytes.Bytes;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.crypto.SignedRawTransaction;
import org.web3j.crypto.TransactionDecoder;
import org.web3j.crypto.transaction.type.ITransaction;
import org.web3j.crypto.transaction.type.Transaction1559;
import org.web3j.crypto.transaction.type.Transaction2930;
import org.web3j.crypto.transaction.type.TransactionType;
import org.web3j.utils.Numeric;

public class SpanBatchTxs {

    private Long totalBlockTxCount;
    private BigInteger contractCreationBits;
    private BigInteger yParityBits;
    private List<SpanBatchSignature> txSigs;
    private List<BigInteger> txNonces;
    private List<BigInteger> txGases;
    private List<String> txTos;
    private List<String> txDatas;
    private List<Integer> txTypes;

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
            List<Integer> txTypes) {
        this.totalBlockTxCount = totalBlockTxCount;
        this.contractCreationBits = contractCreationBits;
        this.yParityBits = yParityBits;
        this.txSigs = txSigs;
        this.txNonces = txNonces;
        this.txGases = txGases;
        this.txTos = txTos;
        this.txDatas = txDatas;
        this.txTypes = txTypes;
    }

    public byte[] encodeContractCreationBits() {
        Long contractCreationBitBufferLen = this.totalBlockTxCount / 8;
        if (this.totalBlockTxCount % 8 != 0) {
            contractCreationBitBufferLen = contractCreationBitBufferLen + 1;
        }
        byte[] contractCreationBitBuffer = new byte[contractCreationBitBufferLen.intValue()];
        for (int i = 0; i < this.totalBlockTxCount; i += 8) {
            int end = i + 8;
            if (end < this.totalBlockTxCount.intValue()) {
                end = this.totalBlockTxCount.intValue();
            }
            int bits = 0;
            for (int j = i; j < end; j++) {
                if (this.contractCreationBits.testBit(j)) {
                    bits |= 1 << (j - i);
                }
            }
            contractCreationBitBuffer[i / 8] = (byte) bits;
        }
        return contractCreationBitBuffer;
    }

    /**
     * contractCreationBits is bitlist right-padded to a multiple of 8 bits.
     *
     * @param contractCreationBit encoded contract creation bits.
     */
    public void decodeContractCreationBits(byte[] contractCreationBit) {
        Long contractCreationBitBufferLen = this.totalBlockTxCount / 8;
        if (this.totalBlockTxCount % 8 != 0) {
            contractCreationBitBufferLen = contractCreationBitBufferLen + 1;
        }
        if (contractCreationBitBufferLen > 10000000) {
            throw new RuntimeException("span batch size limit reached");
        }
        byte[] contractCreationBitBuffer = new byte[contractCreationBitBufferLen.intValue()];
        System.arraycopy(contractCreationBit, 0, contractCreationBitBuffer, 0, contractCreationBit.length);
        BigInteger contractCreationbits = BigInteger.ZERO;
        for (int i = 0; i < this.totalBlockTxCount; i += 8) {
            int end = i + 8;
            if (end < this.totalBlockTxCount.intValue()) {
                end = this.totalBlockTxCount.intValue();
            }
            byte bits = contractCreationBitBuffer[i / 8];
            for (int j = i; j < end; j++) {
                int bit = (bits >> (j - i)) & 1;
                if (bit != 0) {
                    contractCreationbits = contractCreationbits.setBit(j);
                }
            }
        }
        this.contractCreationBits = contractCreationbits;
    }

    /**
     * Counts the number of contract creations in the batch.
     *
     * @return the number of contract creations in the batch.
     */
    public Long contractCreationCount() {
        if (contractCreationBits == null) {
            throw new RuntimeException("dev error: contract creation bits not set");
        }
        Long result = 0L;
        for (int i = 0; i < this.totalBlockTxCount; i++) {
            if (contractCreationBits.testBit(i)) {
                result++;
            }
        }
        return result;
    }

    public byte[] encodeYParityBits() {
        Long yParityBitBufferLen = this.totalBlockTxCount / 8;
        if (this.totalBlockTxCount % 8 != 0) {
            yParityBitBufferLen++;
        }
        byte[] yParityBitBuffer = new byte[yParityBitBufferLen.intValue()];
        for (int i = 0; i < this.totalBlockTxCount; i += 8) {
            int end = i + 8;
            if (end < this.totalBlockTxCount.intValue()) {
                end = this.totalBlockTxCount.intValue();
            }
            int bits = 0;
            for (int j = i; j < end; j++) {
                if (yParityBits.testBit(j)) {
                    bits |= 1 << (j - i);
                }
            }
            yParityBitBuffer[i / 8] = (byte) bits;
        }
        return yParityBitBuffer;
    }

    public void decodeYParityBits(byte[] yParityBit) {
        Long yParityBitBufferLen = this.totalBlockTxCount / 8;
        if (this.totalBlockTxCount % 8 != 0) {
            yParityBitBufferLen++;
        }
        if (yParityBitBufferLen > 10000000L) {
            throw new RuntimeException("span batch size limit reached");
        }

        BigInteger yParityBits = BigInteger.ZERO;
        for (int i = 0; i < this.totalBlockTxCount; i += 8) {
            int end = i + 8;
            if (end < this.totalBlockTxCount.intValue()) {
                end = this.totalBlockTxCount.intValue();
            }
            byte bits = yParityBit[i / 8];
            for (int j = i; j < end; j++) {
                int bit = (bits >> (j - i)) & 1;
                if (bit != 0) {
                    yParityBits = yParityBits.setBit(j);
                }
            }
        }
        this.yParityBits = yParityBits;
    }

    public byte[] encodeTxSigsRS() {
        ByteBuffer result = ByteBuffer.allocate(64 * this.totalBlockTxCount.intValue());
        for (SpanBatchSignature signature : txSigs) {
            byte[] rBytes = Numeric.toBytesPadded(signature.r(), 32);
            result.put(rBytes);
            byte[] sBytes = Numeric.toBytesPadded(signature.s(), 32);
            result.put(sBytes);
        }
        return result.array();
    }

    public void decodeTxSigsRS(byte[] txSigsBuffer) {
        List<SpanBatchSignature> txSigs = new ArrayList<>();
        for (int i = 0; i < this.totalBlockTxCount.intValue(); i++) {
            byte[] r = new byte[32];
            System.arraycopy(txSigsBuffer, i * 64, r, 0, 32);
            byte[] s = new byte[32];
            System.arraycopy(txSigsBuffer, 32 + i * 64, s, 0, 32);
            BigInteger rInt = Numeric.toBigInt(r);
            BigInteger sInt = Numeric.toBigInt(s);
            txSigs.add(new SpanBatchSignature(BigInteger.ZERO, rInt, sInt));
        }
        this.txSigs = txSigs;
    }

    public Bytes encodeTxNonces() {
        ByteBuf buffer = Unpooled.buffer(10);
        for (BigInteger txNonce : txNonces) {
            ByteBufExtKt.writeUvarint(buffer, txNonce.longValue());
        }
        return Bytes.wrap(ByteBufUtil.getBytes(buffer));
    }

    public void decodeTxNonces(Bytes bytes) {
        List<BigInteger> txNonces = new ArrayList<>();
        ByteBuf buffer = Unpooled.wrappedBuffer(bytes.toArrayUnsafe());
        for (int i = 0; i < this.totalBlockTxCount.intValue(); i++) {
            BigInteger txNonce = BigInteger.valueOf(ByteBufExtKt.readUvarint(buffer));
            txNonces.add(txNonce);
        }
        this.txNonces = txNonces;
    }

    public Bytes encodeTxGases() {
        ByteBuf buffer = Unpooled.buffer(10);
        for (BigInteger txGas : txGases) {
            ByteBufExtKt.writeUvarint(buffer, txGas.longValue());
        }
        return Bytes.wrap(ByteBufUtil.getBytes(buffer));
    }

    public void decodeTxGases(Bytes bytes) {
        List<BigInteger> txGases = new ArrayList<>();
        ByteBuf buffer = Unpooled.wrappedBuffer(bytes.toArrayUnsafe());
        for (int i = 0; i < this.totalBlockTxCount.intValue(); i++) {
            BigInteger txNonce = BigInteger.valueOf(ByteBufExtKt.readUvarint(buffer));
            txGases.add(txNonce);
        }
        this.txGases = txGases;
    }

    public byte[] encodeTxTos() {
        ByteBuffer result = ByteBuffer.allocate(20 * this.totalBlockTxCount.intValue());
        for (String txTo : txTos) {
            byte[] addressBytes = Numeric.hexStringToByteArray(txTo.substring(2));
            result.put(addressBytes);
        }
        return result.array();
    }

    public void decodeTxTos(byte[] txTosBuffer) {
        List<String> txTos = new ArrayList<>();
        for (int i = 0; i < this.totalBlockTxCount.intValue(); i++) {
            byte[] addBytes = new byte[20];
            System.arraycopy(txTosBuffer, i * 20, addBytes, 0, 20);
            String address = Keys.toChecksumAddress(Numeric.toHexStringNoPrefix(addBytes))
                    .toLowerCase();
            txTos.add(address);
        }
        this.txTos = txTos;
    }

    // No Test
    public void recoverV(BigInteger chainId) {
        if (this.txTypes.size() != this.txSigs.size()) {
            throw new RuntimeException("tx type length and tx sigs length mismatch");
        }
        for (int i = 0; i < this.txTypes.size(); i++) {
            BigInteger bit = this.yParityBits.testBit(i) ? BigInteger.ONE : BigInteger.ZERO;
            BigInteger v;
            int type = this.txTypes.get(i);
            switch (type) {
                case 0:
                    v = chainId.multiply(new BigInteger("2"))
                            .add(BigInteger.valueOf(35))
                            .add(bit);
                    break;
                case 1, 2:
                    v = bit;
                    break;
                default:
                    throw new RuntimeException("invalid tx type:" + this.txTypes.get(i));
            }
            SpanBatchSignature old = this.txSigs.get(i);
            SpanBatchSignature newTxSig = new SpanBatchSignature(v, old.r(), old.s());
            this.txSigs.set(i, newTxSig);
        }
    }

    public static SpanBatchTxs newSpanBatchTxs(List<String> txs, int chainId) {
        Long totalBlockTxCount = Long.valueOf(txs.size());
        BigInteger contractCreationBits = BigInteger.ZERO;
        BigInteger yParityBits = BigInteger.ZERO;
        List<SpanBatchSignature> txSigs = new ArrayList<>();
        List<String> txTos = new ArrayList<>();
        List<BigInteger> txNonces = new ArrayList<>();
        List<BigInteger> txGases = new ArrayList<>();
        List<String> txDatas = new ArrayList<>();
        List<Integer> txTypes = new ArrayList<>();
        for (int idx = 0; idx < totalBlockTxCount.intValue(); idx++) {
            String tx = txs.get(idx);
            SignedRawTransaction rawTransaction = (SignedRawTransaction) TransactionDecoder.decode(tx);
            ITransaction transaction = rawTransaction.getTransaction();
            if (EIP1559.equals(rawTransaction.getType())) {
                Transaction1559 transaction1559 = (Transaction1559) transaction;
                if (transaction1559.getChainId() != chainId) {
                    throw new RuntimeException("chainId mismatch. tx has chain ID:" + transaction1559.getChainId()
                            + ", expected:" + chainId + ", but expected chain ID:" + chainId);
                }
            }
            if (EIP2930.equals(rawTransaction.getType())) {
                Transaction2930 transaction2930 = (Transaction2930) transaction;
                if (transaction2930.getChainId() != chainId) {
                    throw new RuntimeException("chainId mismatch. tx has chain ID:" + transaction2930.getChainId()
                            + ", expected:" + chainId + ", but expected chain ID:" + chainId);
                }
            }

            Sign.SignatureData signatureData = rawTransaction.getSignatureData();
            BigInteger v = new BigInteger(1, signatureData.getV());
            BigInteger r = new BigInteger(1, signatureData.getR());
            BigInteger s = new BigInteger(1, signatureData.getS());

            SpanBatchSignature txSig = new SpanBatchSignature(v, r, s);
            txSigs.add(txSig);
            BigInteger contractCreationBit = BigInteger.ONE;
            if (transaction.getTo() != null) {
                txTos.add(transaction.getTo());
            } else {
                contractCreationBit = contractCreationBit.setBit(idx);
            }
            BigInteger yParityBit = convertVToYParity(txSig.v(), transaction.getType());
            if (yParityBit.testBit(idx)) {
                yParityBit = yParityBit.setBit(idx);
            }
            txNonces.add(transaction.getNonce());
            txGases.add(transaction.getGasLimit());
            SpanBatchTx stx = SpanBatchTx.newSpanBatchTx(transaction);
            byte[] stxByte = stx.marshalBinary();
            txDatas.add(Numeric.toHexString(stxByte));
            if (transaction.getType() == null || transaction.getType().getRlpType() == null) {
                txTypes.add(0);
            } else {
                String hex = Integer.toHexString(transaction.getType().getRlpType() & 0xFF);
                txTypes.add(Integer.parseInt(hex, 10));
            }
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
                txTypes);
    }

    public static BigInteger convertVToYParity(BigInteger v, TransactionType txType) {
        if (EIP1559 == txType || EIP2930 == txType) {
            return v;
        }
        if (LEGACY == txType) {
            int res = (v.intValue() - 35) & 1;
            return BigInteger.valueOf(res);
        }
        return null;
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

    public List<Integer> getTxTypes() {
        return txTypes;
    }

    public void setTxTypes(List<Integer> txTypes) {
        this.txTypes = txTypes;
    }
}
