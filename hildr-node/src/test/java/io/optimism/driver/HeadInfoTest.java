/*
 * Copyright 2023 281165273grape@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package io.optimism.driver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigInteger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.web3j.protocol.core.methods.response.EthBlock;

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
  void shouldFailConversionFromBlockToHeadInfoIfMissingL1DepositedTx()
      throws JsonProcessingException {
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

    assertThrowsExactly(
        L1AttributesDepositedTxNotFoundException.class,
        () -> {
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
  void shouldConvertFromBlockToHeadInfo() throws JsonProcessingException {
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
                            "transactions": [
                            {
                                "hash": "0x661df2908a63c9701ef4f9bc1d62432f08cbdc8c6fe6012af49405c00de5f69d",
                                "nonce": "0x41ed06",
                                "blockHash": "0x2e4f4aff36bb7951be9742ad349fb1db84643c6bbac5014f3d196fd88fe333eb",
                                "blockNumber": "0x7fe52f",
                                "transactionIndex": "0x0",
                                "from": "0xdeaddeaddeaddeaddeaddeaddeaddeaddead0001",
                                "to": "0x4200000000000000000000000000000000000015",
                                "value": "0x0",
                                "gasPrice": "0x0",
                                "gas": "0xf4240",
                                "input": "0x015d8eb900000000000000000000000000000000000000000000000000000000008768240000000000000000000000000000000000000000000000000000000064443450000000000000000000000000000000000000000000000000000000000000000e0444c991c5fe1d7291ff34b3f5c3b44ee861f021396d33ba3255b83df30e357d00000000000000000000000000000000000000000000000000000000000000050000000000000000000000007431310e026b69bfc676c0013e12a1a11411eec9000000000000000000000000000000000000000000000000000000000000083400000000000000000000000000000000000000000000000000000000000f4240",
                                "v": "0x0",
                                "r": "0x0",
                                "s": "0x0",
                                "type": "0x7e",
                                "mint": "0x0",
                                "sourceHash": "0x34ad504eea583add76d3b9d249965356ef6ca344d6766644c929357331bb0dc9"
                            }
                            ],
                            "size": "0x365",
                            "mixHash": "0x7aeec5550a9b0616701e49ab835af5f10eadba2a0582016f0e256c9cace0c046",
                            "nonce": "0x0000000000000000",
                            "baseFeePerGas": "0x32"
                        }""";

    EthBlock.Block block = objectMapper.readValue(rawBlock, EthBlock.Block.class);

    HeadInfo headInfo = HeadInfo.from(block);

    assertEquals(
        "0x2e4f4aff36bb7951be9742ad349fb1db84643c6bbac5014f3d196fd88fe333eb",
        headInfo.l2BlockInfo().hash());
    assertEquals(BigInteger.valueOf(8381743L), headInfo.l2BlockInfo().number());
    assertEquals(BigInteger.valueOf(1682191554L), headInfo.l2BlockInfo().timestamp());

    assertEquals(
        "0x0444c991c5fe1d7291ff34b3f5c3b44ee861f021396d33ba3255b83df30e357d",
        headInfo.l1Epoch().hash());
    assertEquals(BigInteger.valueOf(8874020L), headInfo.l1Epoch().number());
    assertEquals(BigInteger.valueOf(1682191440L), headInfo.l1Epoch().timestamp());
  }
}
