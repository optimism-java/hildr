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
import static org.web3j.protocol.core.DefaultBlockParameterName.FINALIZED;

import io.optimism.config.Config;
import io.optimism.config.Config.ChainConfig;
import io.optimism.config.Config.CliConfig;
import io.optimism.engine.EngineApi;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import org.junit.jupiter.api.Test;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.http.HttpService;

class DriverTest {

  @Test
  void testNewDriverFromFinalizedHead()
      throws IOException, ExecutionException, InterruptedException {
    if (System.getenv("L2_TEST_RPC_URL") == null || System.getenv("L1_TEST_RPC_URL") == null) {
      return;
    }
    String l1rpc = System.getenv("L1_TEST_RPC_URL");
    String l2rpc = System.getenv("L2_TEST_RPC_URL");
    CliConfig cliConfig =
        new CliConfig(
            l1rpc,
            l2rpc,
            null,
            "d195a64e08587a3f1560686448867220c2727550ce3e0c95c7200d0ade0f9167",
            l2rpc,
            null);

    Config config = Config.create(null, cliConfig, ChainConfig.optimismGoerli());
    Web3j provider = Web3j.build(new HttpService(config.l2RpcUrl()));
    EthBlock finalizedBlock = provider.ethGetBlockByNumber(FINALIZED, true).send();
    Driver<EngineApi> driver = Driver.from(config);

    assertEquals(
        driver.getEngineDriver().getFinalizedHead().number(),
        finalizedBlock.getBlock().getNumber());
  }
}
