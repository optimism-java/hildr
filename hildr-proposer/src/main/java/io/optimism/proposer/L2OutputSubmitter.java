package io.optimism.proposer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import io.optimism.proposer.config.Config;
import io.optimism.proposer.exception.OutputSubmitterExecution;
import io.optimism.utilities.rpc.Web3jProvider;
import io.optimism.utilities.rpc.response.OutputRootResult;
import io.optimism.utilities.rpc.response.SyncStatusResult;
import io.optimism.utilities.telemetry.TracerTaskWrapper;
import io.optimism.utilities.web3j.Web3jUtil;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.RawTransaction;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.methods.response.Web3Sha3;
import org.web3j.utils.Numeric;

/**
 * L2OutputSubmitter submits L2 output.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public class L2OutputSubmitter extends AbstractExecutionThreadService {

    private static final Logger LOGGER = LoggerFactory.getLogger(L2OutputSubmitter.class);

    private final Config config;
    private final Web3j l1Client;
    private final Web3j l2Client;
    private final Credentials l2From;
    private final Web3jService rollUpClient;
    private final ObjectMapper mapper;

    private BigInteger nonce;
    private volatile boolean isShutdownTriggered;

    private void tryOutputSubmit() {
        var syncStatus = this.syncStatus();
        var currentBlockNum = Boolean.TRUE.equals(config.allowNonFinalized())
                ? syncStatus.safeL2().number()
                : syncStatus.finalizedL2().number();
        var output = this.fetchOutputInfo(currentBlockNum);
        if (output != null) {
            this.sendOutputTx(currentBlockNum, output, syncStatus);
        }
    }

    private OutputRootResult fetchOutputInfo(BigInteger currentBlockNum) {
        BigInteger nextCheckPointBlockNum = this.nextCheckPointBlock();
        if (currentBlockNum.compareTo(nextCheckPointBlockNum) < 0) {
            return null;
        }
        return this.outputAtBlock(nextCheckPointBlockNum);
    }

    private Object sendOutputTx(
            final BigInteger curBlock, final OutputRootResult output, final SyncStatusResult status) {
        this.waitL1Head(status.headL1().number().add(BigInteger.ONE));
        Function proposeL2OutputFn = null;
        try {
            proposeL2OutputFn = FunctionEncoder.makeFunction(
                    "proposeL2Output",
                    List.of("bytes32", "uint256", "bytes32", "uint256"),
                    List.of(
                            Numeric.hexStringToByteArray(output.outputRoot()),
                            curBlock,
                            status.currentL1().hash(),
                            status.currentL1().number()),
                    List.of());
        } catch (ReflectiveOperationException e) {
            throw new OutputSubmitterExecution(e);
        }

        String fnData = FunctionEncoder.encode(proposeL2OutputFn);

        // todo parameter
        RawTransaction tx = RawTransaction.createTransaction(
                this.config.l2ChainId(),
                this.getNonce(),
                BigInteger.ZERO,
                this.config.l2OutputOracleAddr(),
                BigInteger.ZERO,
                fnData,
                BigInteger.ZERO,
                BigInteger.ZERO);
        var receipt = Web3jUtil.executeContractReturnReceipt(l2Client, tx, this.config.l2ChainId(), this.l2From);
        Optional<TransactionReceipt> txOption = receipt.getTransactionReceipt();
        if (txOption.isPresent() && txOption.get().isStatusOK()) {
            LOGGER.info(
                    "proposer tx successfully published: tx_hash = {}, l1BlockNum = {}, l1BlockHash = {}",
                    txOption.get().getTransactionHash(),
                    status.currentL1().number(),
                    status.currentL1().hash());
        } else {
            LOGGER.error(
                    "proposer tx successfully published but reverted: tx_hash = {}",
                    txOption.map(TransactionReceipt::getTransactionHash).orElse(null));
        }
        return null;
    }

    private BigInteger getNonce() {
        if (nonce == null) {
            nonce = Web3jUtil.getTxCount(this.l2Client, this.l2From.getAddress());
        } else {
            nonce = nonce.add(BigInteger.ONE);
        }
        return nonce;
    }

    private void waitL1Head(final BigInteger headNum) {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            scope.fork(() -> {
                BigInteger l1Head = BigInteger.ZERO;
                do {
                    if (l1Head.compareTo(BigInteger.ZERO) == 0) {
                        Thread.sleep(this.config.pollInterval());
                    }
                    var blockNumResp = l1Client.ethBlockNumber().sendAsync().get();
                    if (blockNumResp != null) {
                        l1Head = blockNumResp.getBlockNumber();
                    }
                } while (l1Head.compareTo(headNum) <= 0);
                return null;
            });
            scope.join();
        } catch (InterruptedException e) {
            throw new OutputSubmitterExecution(e);
        }
    }

    private BigInteger nextCheckPointBlock() {
        // l2 oracle contract fetches next block number
        Function nextBlockNumberFn = null;
        try {
            nextBlockNumberFn =
                    FunctionEncoder.makeFunction("nextBlockNumber", List.of(), List.of(), List.of("uint256"));
        } catch (ReflectiveOperationException e) {
            throw new OutputSubmitterExecution(e);
        }
        List<Type> resp = Web3jUtil.executeContract(
                this.l2Client, this.l2From.getAddress(), this.config.l2OutputOracleAddr(), nextBlockNumberFn);
        return resp.isEmpty() ? null : ((Uint256) resp.get(0)).getValue();
    }

    private OutputRootResult outputAtBlock(BigInteger blockNumber) {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            var outputInfo = scope.fork(TracerTaskWrapper.wrap(() -> {
                return new Request<>(
                                "optimism_outputAtBlock",
                                Collections.singletonList(blockNumber),
                                this.rollUpClient,
                                Web3Sha3.class)
                        .send()
                        .getResult();
            }));
            scope.join();
            scope.throwIfFailed();
            return mapper.readValue(outputInfo.get(), OutputRootResult.class);
        } catch (InterruptedException | ExecutionException | IOException e) {
            throw new OutputSubmitterExecution(e);
        }
    }

    private SyncStatusResult syncStatus() {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            var outputInfo = scope.fork(TracerTaskWrapper.wrap(() -> {
                return new Request<>("optimism_syncStatus", List.of(), this.rollUpClient, Web3Sha3.class)
                        .send()
                        .getResult();
            }));
            scope.join();
            scope.throwIfFailed();
            return mapper.readValue(outputInfo.get(), SyncStatusResult.class);
        } catch (InterruptedException | ExecutionException | IOException e) {
            throw new OutputSubmitterExecution(e);
        }
    }

    /**
     * The L2OutputSubmitter constructor.
     * @param config The proposer config.
     */
    public L2OutputSubmitter(Config config) {
        this.config = config;
        this.l1Client = Web3jProvider.createClient(config.l1RpcUrl());
        this.l2Client = Web3jProvider.createClient(config.l2RpcUrl());
        this.l2From = Credentials.create(config.l2Signer());
        var tuple = Web3jProvider.create(config.rollupRpc());
        this.rollUpClient = tuple.component2();
        this.mapper = new ObjectMapper();
    }

    @Override
    protected void run() throws Exception {
        try {
            while (isRunning() && !this.isShutdownTriggered) {
                this.tryOutputSubmit();
                Thread.sleep(config.pollInterval());
            }
        } catch (InterruptedException e) {
            throw new OutputSubmitterExecution("");
        }
    }

    @Override
    protected void shutDown() throws Exception {
        super.shutDown();
    }

    @Override
    protected void triggerShutdown() {
        this.isShutdownTriggered = true;
    }
}
