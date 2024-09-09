package io.optimism.driver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.http.HttpService;

/**
 * The type HeadInfoTest.
 *
 * @author grapebaba
 * @since 0.1.0
 */
class HeadInfoTest {

    /**
     * Should fail conversion from block to head info if missing l 1 deposited tx.
     *
     * @throws JsonProcessingException the json processing exception
     */
    @Test
    @DisplayName("should fail conversion from a block to head info if missing L1 deposited tx")
    @SuppressWarnings("checkstyle:LineLength")
    void shouldFailConversionFromBlockToHeadInfoIfMissingL1DepositedTx() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();

        String rawBlock =
                """
            {
                            "hash": "0x2e4f4aff36bb7951be9742ad349fb1db84643c6bbac5014f3d196fd88fe333eb",
                            "parentHash": "0xeccf4c06ad0d27be1cadee5720a509d31a9de0462b52f2cf6045d9a73c9aa504",
                            "sha3Uncles": "0x1dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d49347",
                            "miner": "0x4200000000000000000000000000000000000011",
                            "stateRoot": "0x5905b2423f299a29db41e377d7ceadf4baa49eed04e1b72957e8c0985e04e730",
                            "transactionsRoot": "0x030e481411042a769edde83d790d583ed69f9d3098d4a78d00e008f749fcfd97",
                            "receiptsRoot": "0x29079b696c12a19999f3bb303fddb6fc12fb701f427678cca24954b91080ada3",
                            "number": "0x7fe52f",
                            "gasUsed": "0xb711",
                            "gasLimit": "0x17d7840",
                            "extraData": "0x",
                            "logsBloom": "0x00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000",
                            "timestamp": "0x644434c2",
                            "difficulty": "0x0",
                            "totalDifficulty": "0x0",
                            "sealFields": [],
                            "uncles": [],
                            "transactions": [],
                            "size": "0x365",
                            "mixHash": "0x7aeec5550a9b0616701e49ab835af5f10eadba2a0582016f0e256c9cace0c046",
                            "nonce": "0x0000000000000000",
                            "baseFeePerGas": "0x32"
                        }""";
        EthBlock.Block block = objectMapper.readValue(rawBlock, EthBlock.Block.class);

        assertThrowsExactly(L1AttributesDepositedTxNotFoundException.class, () -> {
            HeadInfo ignored = HeadInfo.from(block);
        });
    }

    /**
     * Should convert from block to head info.
     *
     * @throws JsonProcessingException the json processing exception
     */
    @Test
    @DisplayName("should convert from a block to head info")
    @SuppressWarnings("checkstyle:LineLength")
    void shouldConvertFromBlockToHeadInfo() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        List<String> rawBlockFiles = List.of("bedrock_raw_block.txt", "ectone_raw_block.txt");
        for (String path : rawBlockFiles) {
            URL url = Resources.getResource(path);
            String testData = Resources.toString(url, Charsets.UTF_8);
            Map map = objectMapper.readValue(testData, Map.class);

            EthBlock.Block block = objectMapper.convertValue(map.get("test_block_data"), EthBlock.Block.class);

            HeadInfo headInfo = HeadInfo.from(block);
            assertEquals(map.get("assert_l2_block_hash"), headInfo.l2BlockInfo().hash());
            assertEquals(
                    new BigInteger((String) map.get("assert_l2_block_number")),
                    headInfo.l2BlockInfo().number());
            assertEquals(
                    new BigInteger((String) map.get("assert_l2_block_timestamp")),
                    headInfo.l2BlockInfo().timestamp());

            assertEquals(map.get("assert_l1_block_hash"), headInfo.l1Epoch().hash());
            assertEquals(
                    new BigInteger((String) map.get("assert_l1_block_number")),
                    headInfo.l1Epoch().number());
            assertEquals(
                    new BigInteger((String) map.get("assert_l1_block_timestamp")),
                    headInfo.l1Epoch().timestamp());
        }
    }

    @Test
    void testHeadInfoFromL2BlockHash() throws IOException {
        if (System.getenv("L2_TEST_RPC_URL") == null) {
            return;
        }
        String l2RpcUrl = System.getenv("L2_TEST_RPC_URL");
        Web3j l2Provider = Web3j.build(new HttpService(l2RpcUrl));
        String l2BlockHash = "0x75d4a658d7b6430c874c5518752a8d90fb1503eccd6ae4cfc97fd4aedeebb939";
        EthBlock ethBlock = l2Provider.ethGetBlockByHash(l2BlockHash, true).send();
        HeadInfo headInfo = HeadInfo.from(ethBlock.getBlock());

        assertEquals(BigInteger.valueOf(8428108L), headInfo.l2BlockInfo().number());
        assertEquals(BigInteger.valueOf(1682284284L), headInfo.l2BlockInfo().timestamp());
        assertEquals(
                "0x76ab90dc2afea158bbe14a99f22d5f867b51719378aa37d1a3aa3833ace67cad",
                headInfo.l1Epoch().hash());
        assertEquals(BigInteger.valueOf(8879997L), headInfo.l1Epoch().number());
        assertEquals(BigInteger.valueOf(1682284164L), headInfo.l1Epoch().timestamp());
        assertEquals(BigInteger.valueOf(4L), headInfo.sequenceNumber());
    }
}
