package io.optimism.derive.stages;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.optimism.types.SpanBatchAccessListTxData;
import io.optimism.types.SpanBatchDynamicFeeTxData;
import io.optimism.types.SpanBatchLegacyTxData;
import io.optimism.types.SpanBatchTx;
import io.optimism.types.SpanBatchTxData;
import java.math.BigInteger;
import java.util.List;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.hyperledger.besu.datatypes.AccessListEntry;
import org.hyperledger.besu.datatypes.Address;
import org.hyperledger.besu.datatypes.Hash;
import org.hyperledger.besu.datatypes.TransactionType;
import org.hyperledger.besu.datatypes.Wei;
import org.hyperledger.besu.ethereum.core.Transaction;
import org.junit.jupiter.api.Test;
import org.web3j.utils.Numeric;

/**
 * The type SpanBatchTxTest.
 *
 * @author grapebaba
 * @since 0.2.4
 */
class SpanBatchTxTest {

    /**
     * Marshal binary.
     */
    @Test
    void marshalBinaryInterOp() {
        Transaction.Builder builder = Transaction.builder();
        Transaction tx = builder.type(TransactionType.FRONTIER)
                .nonce(10000000000L)
                .gasPrice(Wei.of(new BigInteger("10000000000")))
                .gasLimit(311245L)
                .to(Address.fromHexString("0xf1d2f39c58427bE48c5ECDa1E0cC7a930Ae1Ca50"))
                .value(Wei.of(new BigInteger("6000000000000000000")))
                .payload(Bytes.fromHexString("0xba457aba24bbd63f5670"))
                .build();

        SpanBatchTx spanBatchTx = SpanBatchTx.newSpanBatchTx(tx);
        byte[] bytes = spanBatchTx.marshalBinary();
        assertArrayEquals(
                bytes, Numeric.hexStringToByteArray("0xda8853444835ec5800008502540be4008aba457aba24bbd63f5670"));

        Transaction accTx = Transaction.builder()
                .type(TransactionType.ACCESS_LIST)
                .gasPrice(Wei.of(new BigInteger("10000000000")))
                .value(Wei.of(new BigInteger("6000000000000000000")))
                .payload(Bytes.fromHexString("0xba457aba24bbd63f5670"))
                .chainId(BigInteger.valueOf(1))
                .accessList(List.of(new AccessListEntry(
                        Address.fromHexString("0xf1d2f39c58427bE48c5ECDa1E0cC7a930Ae1Ca50"),
                        List.of(Hash.wrap(Bytes32.fromHexString("0x01"))))))
                .build();

        SpanBatchTx spanBatchTx2 = SpanBatchTx.newSpanBatchTx(accTx);
        byte[] bytes2 = spanBatchTx2.marshalBinary();
        assertArrayEquals(
                bytes2,
                Numeric.hexStringToByteArray(
                        "0x01f8548853444835ec5800008502540be4008aba457aba24bbd63f5670f838f794f1d2f39c58427be48c5ecda1e0cc7a930ae1ca50e1a00000000000000000000000000000000000000000000000000000000000000001"));

        Transaction dynTx = Transaction.builder()
                .type(TransactionType.EIP1559)
                .maxPriorityFeePerGas(Wei.of(new BigInteger("10000000000")))
                .maxFeePerGas(Wei.of(new BigInteger("10000000000")))
                .value(Wei.of(new BigInteger("6000000000000000000")))
                .payload(Bytes.fromHexString("0xba457aba24bbd63f5670"))
                .accessList(List.of(new AccessListEntry(
                        Address.fromHexString("0xf1d2f39c58427bE48c5ECDa1E0cC7a930Ae1Ca50"),
                        List.of(Hash.wrap(Bytes32.fromHexString("0x01"))))))
                .chainId(BigInteger.valueOf(1))
                .build();

        SpanBatchTx spanBatchTx3 = SpanBatchTx.newSpanBatchTx(dynTx);
        byte[] bytes3 = spanBatchTx3.marshalBinary();
        assertArrayEquals(
                bytes3,
                Numeric.hexStringToByteArray(
                        "0x02f85a8853444835ec5800008502540be4008502540be4008aba457aba24bbd63f5670f838f794f1d2f39c58427be48c5ecda1e0cc7a930ae1ca50e1a00000000000000000000000000000000000000000000000000000000000000001"));
    }

    /**
     * Unmarshal binary.
     */
    @Test
    void unmarshalBinaryInterOp() {
        SpanBatchTxData txData = SpanBatchTx.unmarshalBinary(
                        Numeric.hexStringToByteArray("0xda8853444835ec5800008502540be4008aba457aba24bbd63f5670"))
                .getSpanBatchTxData();

        assertNotNull(txData);
        assertEquals(TransactionType.FRONTIER, txData.txType());
        assertEquals(Wei.of(new BigInteger("6000000000000000000")), ((SpanBatchLegacyTxData) txData).value());
        assertEquals(Wei.of(new BigInteger("10000000000")), ((SpanBatchLegacyTxData) txData).gasPrice());
        assertEquals(Bytes.fromHexString("0xba457aba24bbd63f5670"), ((SpanBatchLegacyTxData) txData).data());

        SpanBatchTxData txData1 = SpanBatchTx.unmarshalBinary(
                        Numeric.hexStringToByteArray(
                                "0x01f8548853444835ec5800008502540be4008aba457aba24bbd63f5670f838f794f1d2f39c58427be48c5ecda1e0cc7a930ae1ca50e1a00000000000000000000000000000000000000000000000000000000000000001"))
                .getSpanBatchTxData();
        assertNotNull(txData1);
        assertEquals(TransactionType.ACCESS_LIST, txData1.txType());
        assertEquals(Wei.of(new BigInteger("6000000000000000000")), ((SpanBatchAccessListTxData) txData1).value());
        assertEquals(Wei.of(new BigInteger("10000000000")), ((SpanBatchAccessListTxData) txData1).gasPrice());
        assertEquals(Bytes.fromHexString("0xba457aba24bbd63f5670"), ((SpanBatchAccessListTxData) txData1).data());
        assertEquals(
                List.of(new AccessListEntry(
                        Address.fromHexString("0xf1d2f39c58427bE48c5ECDa1E0cC7a930Ae1Ca50"),
                        List.of(Hash.wrap(Bytes32.fromHexString("0x01"))))),
                ((SpanBatchAccessListTxData) txData1).accessList());

        SpanBatchTxData txData2 = SpanBatchTx.unmarshalBinary(
                        Numeric.hexStringToByteArray(
                                "0x02f85a8853444835ec5800008502540be4008502540be4008aba457aba24bbd63f5670f838f794f1d2f39c58427be48c5ecda1e0cc7a930ae1ca50e1a00000000000000000000000000000000000000000000000000000000000000001"))
                .getSpanBatchTxData();
        assertNotNull(txData2);
        assertEquals(TransactionType.EIP1559, txData2.txType());
        assertEquals(Wei.of(new BigInteger("6000000000000000000")), ((SpanBatchDynamicFeeTxData) txData2).value());
        assertEquals(Wei.of(new BigInteger("10000000000")), ((SpanBatchDynamicFeeTxData) txData2).gasTipCap());
        assertEquals(Wei.of(new BigInteger("10000000000")), ((SpanBatchDynamicFeeTxData) txData2).gasFeeCap());
        assertEquals(Bytes.fromHexString("0xba457aba24bbd63f5670"), ((SpanBatchDynamicFeeTxData) txData2).data());
        assertEquals(
                List.of(new AccessListEntry(
                        Address.fromHexString("0xf1d2f39c58427bE48c5ECDa1E0cC7a930Ae1Ca50"),
                        List.of(Hash.wrap(Bytes32.fromHexString("0x01"))))),
                ((SpanBatchDynamicFeeTxData) txData2).accessList());
    }
}
