package io.optimism.rpc.methods;

import io.optimism.exceptions.HildrServiceExecutionException;
import io.optimism.rpc.RpcMethod;
import io.optimism.rpc.Web3jProvider;
import io.optimism.rpc.internal.JsonRpcRequestContext;
import io.optimism.rpc.internal.response.JsonRpcResponse;
import io.optimism.rpc.internal.response.JsonRpcSuccessResponse;
import io.optimism.rpc.internal.result.EthGetProof;
import io.optimism.rpc.internal.result.OutputRootResult;
import io.optimism.telemetry.TracerTaskWrapper;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
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

    /**
     * Instantiates a new Output at block.
     *
     * @param l2RpcUrl the l 2 rpc url
     * @param l2ToL1MessagePasser the l 2 to l 1 message passer
     */
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
        if (context.getRequest().getParamLength() == 0) {
            throw new HildrServiceExecutionException("Invalid params");
        }
        String number = context.getParameter(0, String.class);
        if (StringUtils.isEmpty(number)) {
            throw new HildrServiceExecutionException("failed to get block by number: " + number);
        }
        final BigInteger blockNumber = Numeric.toBigInt(number);
        try {
            EthBlock.Block block = this.getBlock(blockNumber);

            final String blockHash = block.getHash();
            EthGetProof.Proof stateProof = this.getProof(blockHash);
            if (stateProof == null) {
                throw new HildrServiceExecutionException("failed to get state proof: " + blockHash);
            }
            String stateRoot = block.getStateRoot();
            String withdrawalStorageRoot = stateProof.getStorageHash();
            var outputRoot = computeL2OutputRoot(block, withdrawalStorageRoot);
            var version = new byte[32];
            var result =
                    new OutputRootResult(outputRoot, Numeric.toHexString(version), stateRoot, withdrawalStorageRoot);
            return new JsonRpcSuccessResponse(context.getRequest().getId(), result);
        } catch (ExecutionException e) {
            throw new HildrServiceExecutionException(e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new HildrServiceExecutionException(e);
        }
    }

    static String computeL2OutputRoot(EthBlock.Block block, String storageRoot) {
        var version = new byte[32];

        byte[] digestBytes = new byte[0];
        digestBytes = ArrayUtils.addAll(digestBytes, version);
        digestBytes = ArrayUtils.addAll(digestBytes, Numeric.hexStringToByteArray(block.getStateRoot()));
        digestBytes = ArrayUtils.addAll(digestBytes, Numeric.hexStringToByteArray(storageRoot));
        digestBytes = ArrayUtils.addAll(digestBytes, Numeric.hexStringToByteArray(block.getHash()));

        var digest = new Keccak.Digest256();
        byte[] hash = digest.digest(digestBytes);
        return Numeric.toHexString(hash);
    }

    private EthBlock.Block getBlock(final BigInteger blockNumber) throws InterruptedException, ExecutionException {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            StructuredTaskScope.Subtask<EthBlock> ethBlockFuture = scope.fork(TracerTaskWrapper.wrap(
                    () -> client.ethGetBlockByNumber(DefaultBlockParameter.valueOf(blockNumber), true)
                            .send()));
            scope.join();
            scope.throwIfFailed();
            return ethBlockFuture.get().getBlock();
        }
    }

    private EthGetProof.Proof getProof(String blockHash) throws InterruptedException, ExecutionException {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            StructuredTaskScope.Subtask<EthGetProof> ehtGetProofFuture =
                    scope.fork(TracerTaskWrapper.wrap(() -> new Request<>(
                                    ETH_GET_PROOF,
                                    Arrays.asList(this.l2ToL1MessagePasser, Collections.<String>emptyList(), blockHash),
                                    this.service,
                                    EthGetProof.class)
                            .send()));
            scope.join();
            scope.throwIfFailed();
            return ehtGetProofFuture.get().getProof();
        }
    }
}
