package io.optimism.rpc;

import io.optimism.exceptions.Web3jCallException;
import io.optimism.telemetry.TracerTaskWrapper;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Hash;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Numeric;

/**
 * Web3jUtil provides some utility methods for web3j.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public class Web3jUtil {

    private Web3jUtil() {}

    /**
     * Get the transaction count of the given address.
     *
     * @param client the web3j client
     * @param fromAddr the address
     * @return the transaction count
     */
    public static BigInteger getTxCount(final Web3j client, final String fromAddr) {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            var countFuture = scope.fork(TracerTaskWrapper.wrap(() -> {
                var countReq = client.ethGetTransactionCount(fromAddr, DefaultBlockParameterName.LATEST);
                return countReq.send().getTransactionCount();
            }));
            scope.join();
            scope.throwIfFailed();
            return countFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new Web3jCallException("get tx count failed", e);
        }
    }

    /**
     * Get the transaction receipt of the given transaction hash.
     * @param client the web3j client
     * @param txHash the transaction hash
     * @return the transaction receipt
     */
    public static EthGetTransactionReceipt getTxReceipt(final Web3j client, final String txHash) {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            var receiptFuture =
                    scope.fork(() -> client.ethGetTransactionReceipt(txHash).send());
            scope.join();
            scope.throwIfFailed();
            return receiptFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new Web3jCallException("failed to get TxReceipt", e);
        }
    }

    /**
     * Poll the block by the given parameter.
     * @param client the web3j client
     * @param parameter the block parameter
     * @param returnFullTransactionObjects whether to return full transaction objects
     * @return the block
     */
    public static EthBlock pollBlock(
            final Web3j client, final DefaultBlockParameter parameter, final boolean returnFullTransactionObjects) {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            var receiptFuture = scope.fork(() -> client.ethGetBlockByNumber(parameter, returnFullTransactionObjects)
                    .send());
            scope.join();
            scope.throwIfFailed();
            return receiptFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new Web3jCallException("failed to get block by number", e);
        }
    }

    /**
     * Execute the contract.
     * @param client the web3j client
     * @param fromAddr the from address
     * @param contractAddr the contract address
     * @param function the function
     * @return the list of type
     */
    public static List<Type> executeContract(
            final Web3j client, String fromAddr, String contractAddr, final Function function) {
        String fnData = FunctionEncoder.encode(function);
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            var fut = scope.fork(() -> {
                return client.ethCall(
                                Transaction.createEthCallTransaction(fromAddr, contractAddr, fnData),
                                DefaultBlockParameterName.LATEST)
                        .send()
                        .getValue();
            });
            scope.join();
            scope.throwIfFailed();
            return FunctionReturnDecoder.decode(fut.get(), function.getOutputParameters());
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new Web3jCallException(e);
        }
    }

    /**
     * Execute the contract and return the transaction receipt.
     * @param client the web3j client
     * @param tx the raw transaction
     * @param chainId the chain ID
     * @param credentials the credentials
     * @return the transaction receipt
     */
    public static EthGetTransactionReceipt executeContractReturnReceipt(
            final Web3j client, final RawTransaction tx, final long chainId, final Credentials credentials) {

        byte[] sign = TransactionEncoder.signMessage(tx, chainId, credentials);
        var signTxHexValue = Numeric.toHexString(sign);
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            var fork = scope.fork(() -> {
                EthSendTransaction txResp =
                        client.ethSendRawTransaction(signTxHexValue).send();
                if (txResp == null) {
                    throw new Web3jCallException("call contract tx response is null");
                }
                if (txResp.hasError()) {
                    throw new Web3jCallException(String.format(
                            "call contract tx has error: code = %d, msg = %s, data = %s",
                            txResp.getError().getCode(),
                            txResp.getError().getMessage(),
                            txResp.getError().getData()));
                }
                String txHashLocal = Hash.sha3(signTxHexValue);
                String txHashRemote = txResp.getTransactionHash();
                if (txHashLocal.equals(txHashRemote)) {
                    throw new Web3jCallException(String.format(
                            "tx has mismatch: txHashLocal = %s, txHashRemote = %s", txHashLocal, txHashRemote));
                }
                return Web3jUtil.getTxReceipt(client, txHashLocal);
            });
            scope.join();
            scope.throwIfFailed();
            return fork.get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new Web3jCallException(e);
        }
    }
}
