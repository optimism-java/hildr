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

package io.optimism.rpc.methods;

import io.optimism.rpc.RpcMethod;
import io.optimism.rpc.internal.JsonRpcRequestContext;
import io.optimism.rpc.internal.response.JsonRpcResponse;
import io.optimism.rpc.internal.response.JsonRpcSuccessResponse;
import io.optimism.rpc.internal.result.EthGetProof;
import io.optimism.rpc.internal.result.OutputRootResult;
import io.optimism.rpc.provider.Web3jProvider;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import jdk.incubator.concurrent.StructuredTaskScope;
import org.apache.commons.lang3.ArrayUtils;
import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.utils.Numeric;

/**
 * jsonRpc api that get output at block.
 *
 * @author thinkAfCod
 * @since 2023.06
 */
public class OutputAtBlock implements JsonRpcMethod {

  private static final String ETH_GET_PROOF = "eth_getProof";

  private final String l2ToL1MessagePasser;

  private Web3j client;

  private Web3jService service;

  public OutputAtBlock(final String l2RpcUrl, final String l2ToL1MessagePasser) {
    Tuple2<Web3j, Web3jService> tuple = Web3jProvider.create(l2RpcUrl);
    this.client = tuple.component1();
    this.service = tuple.component2();
    this.l2ToL1MessagePasser = l2ToL1MessagePasser;
  }

  @Override
  public String getName() {
    return RpcMethod.OP_OUTPUT_AT_BLOCK.getRpcMethodName();
  }

  @Override
  public JsonRpcResponse response(JsonRpcRequestContext context) {
    final BigInteger blockNumber = new BigInteger(context.getParameter(0, String.class), 10);

    try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
      Future<EthBlock> ethBlockFuture =
          scope.fork(
              () ->
                  client
                      .ethGetBlockByNumber(DefaultBlockParameter.valueOf(blockNumber), true)
                      .send());
      Future<EthGetProof> ehtGetProofFuture =
          scope.fork(
              () -> {
                return new Request<>(
                        ETH_GET_PROOF,
                        Arrays.asList(
                            this.l2ToL1MessagePasser,
                            Collections.<String>emptyList(),
                            DefaultBlockParameter.valueOf(blockNumber)),
                        this.service,
                        EthGetProof.class)
                    .send();
              });
      scope.join();
      scope.throwIfFailed();
      EthBlock.Block block = ethBlockFuture.resultNow().getBlock();
      String stateRoot = block.getStateRoot();

      EthGetProof.Proof stateProof = ehtGetProofFuture.resultNow().getProof();
      String withdrawalStorageRoot = stateProof.getStorageHash();
      var outputRoot = computeL2OutputRoot(block, withdrawalStorageRoot);
      var version = new byte[32];
      var result =
          new OutputRootResult(
              outputRoot, Numeric.toHexString(version), stateRoot, withdrawalStorageRoot);
      return new JsonRpcSuccessResponse(context.getRequest().getId(), result);
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private String computeL2OutputRoot(EthBlock.Block block, String storageRoot) {
    var version = new byte[32];

    var digest = new Keccak.Digest256();
    byte[] digestBytes = null;
    digestBytes = ArrayUtils.addAll(digestBytes, version);
    digestBytes =
        ArrayUtils.addAll(digestBytes, Numeric.hexStringToByteArray(block.getStateRoot()));
    digestBytes = ArrayUtils.addAll(digestBytes, Numeric.hexStringToByteArray(storageRoot));
    digestBytes = ArrayUtils.addAll(digestBytes, Numeric.hexStringToByteArray(block.getHash()));

    byte[] hash = digest.digest(digestBytes);
    return Numeric.toHexString(hash);
  }
}
