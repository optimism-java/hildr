package io.optimism.utilities.encoding;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import io.optimism.rpc.response.OpEthBlock;
import io.optimism.types.DepositTransaction;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.web3j.crypto.Hash;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.utils.Numeric;

class TxEncoderTest {

    private static String blockJson;

    private static ObjectMapper mapper;

    @BeforeAll
    static void setUp() throws IOException {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        URL url = Resources.getResource("op_block_json.txt");
        blockJson = Resources.toString(url, Charsets.UTF_8);
    }

    @Test
    void mapperOpEthBlock() throws IOException {
        var opBlock = mapper.readValue(blockJson, OpEthBlock.class);
        assertNotNull(opBlock);
        assertNotNull(opBlock.getBlock().getTransactions());
        assertFalse(opBlock.getBlock().getTransactions().isEmpty());
        var txObj = (OpEthBlock.TransactionObject)
                opBlock.getBlock().getTransactions().get(0);
        assertEquals("0xb526f7d7fbabb79a10077d83d78d036fbb0e066f363b6a0434d009809c4dbe7a", txObj.getSourceHash());
        assertEquals("0x0", txObj.getMint());
        assertEquals("0x1", txObj.getDepositReceiptVersion());
    }

    @Test
    void encodeFromEthBlock() throws JsonProcessingException {
        var block = mapper.readValue(blockJson, EthBlock.class);
        assertNotNull(block.getBlock());
        block.getBlock().getTransactions().removeFirst();
        assertEquals(2, block.getBlock().getTransactions().size());
        var txHashes = block.getBlock().getTransactions().stream()
                .map(tx -> {
                    var txObj = (EthBlock.TransactionObject) tx;
                    return Numeric.toHexString(Hash.sha3(TxEncoder.encode(txObj)));
                })
                .toList();
        assertNotNull(txHashes);
        assertEquals(2, txHashes.size());
        assertEquals(
                List.of(
                        "0x3ae14753cfc7dc0a7f03612c69c2d7586e2b1609b78823e44cf4c6c0286e6cfa",
                        "0x0401086b456ea2b20b1d1d2eec67e68be5bc91133e5fbc703ce9e9f61d0908d2"),
                txHashes);
    }

    @Test
    void encodeFromOpEthBlock() throws JsonProcessingException {
        var opBlock = mapper.readValue(blockJson, OpEthBlock.class);
        assertNotNull(opBlock.getBlock());
        opBlock.getBlock().getTransactions().removeFirst();
        assertEquals(2, opBlock.getBlock().getTransactions().size());
        var txHashes = opBlock.getBlock().getTransactions().stream()
                .map(tx -> {
                    var txObj = (OpEthBlock.TransactionObject) tx;
                    return Numeric.toHexString(Hash.sha3(TxEncoder.encode(txObj.toWeb3j())));
                })
                .toList();
        assertNotNull(txHashes);
        assertEquals(2, txHashes.size());
        assertEquals(
                List.of(
                        "0x3ae14753cfc7dc0a7f03612c69c2d7586e2b1609b78823e44cf4c6c0286e6cfa",
                        "0x0401086b456ea2b20b1d1d2eec67e68be5bc91133e5fbc703ce9e9f61d0908d2"),
                txHashes);
    }

    @Test
    void encodeDepositTx() throws JsonProcessingException {
        var opBlock = mapper.readValue(blockJson, OpEthBlock.class);
        assertNotNull(opBlock.getBlock());
        var txObj = (OpEthBlock.TransactionObject)
                opBlock.getBlock().getTransactions().removeFirst();
        DepositTransaction depositTx1 = TxEncoder.toDepositTx(txObj, false);
        var txEncoded = TxEncoder.encodeDepositTx(txObj, false);
        DepositTransaction depositTx2 = TxDecoder.decodeToDeposit(Numeric.toHexString(txEncoded));
        var txHash = Numeric.toHexString(Hash.sha3(txEncoded));
        assertEquals("0xc7e14eba295f2a5278d49ff9039b7faf7ac9dbccad0da8054d7f69985048b782", txHash);
        assertEquals(depositTx1, depositTx2);
    }
}
