package io.optimism.types;

import static org.web3j.protocol.core.methods.response.EthBlock.Block;
import static org.web3j.protocol.core.methods.response.EthBlock.TransactionObject;

import io.optimism.config.Config.SystemConfig;
import io.optimism.derive.stages.Attributes.UserDeposited;
import io.optimism.exceptions.BlockIsPreLondonException;
import io.optimism.exceptions.BlockNotIncludedException;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The type L1Info.
 *
 * @param blockInfo L1 block info.
 * @param systemConfig system config.
 * @param userDeposits user deposits.
 * @param batcherTransactions batcher transactions.
 * @param parentBeaconRoot L1 beacon parent root hash.
 * @param finalized finalized.
 * @author grapebaba
 * @since 0.1.0
 */
public record L1Info(
        L1BlockInfo blockInfo,
        SystemConfig systemConfig,
        List<UserDeposited> userDeposits,
        List<String> batcherTransactions,
        String parentBeaconRoot,
        boolean finalized) {

    /**
     * Create L1Info.
     *
     * @param block the l1 block
     * @param userDeposits the user deposits
     * @param batchInbox the batch inbox
     * @param finalized the finalized
     * @param systemConfig the system config
     * @return the L1Info
     */
    public static L1Info create(
            Block block,
            List<UserDeposited> userDeposits,
            String batchInbox,
            boolean finalized,
            SystemConfig systemConfig) {
        BigInteger blockNumber = block.getNumber();
        if (blockNumber == null) {
            throw new BlockNotIncludedException();
        }
        String blockHash = block.getHash();
        if (blockHash == null) {
            throw new BlockNotIncludedException();
        }
        String mixHash = block.getMixHash();
        if (mixHash == null) {
            throw new BlockNotIncludedException();
        }
        BigInteger baseFeePerGas = block.getBaseFeePerGas();
        if (baseFeePerGas == null) {
            throw new BlockIsPreLondonException();
        }
        L1BlockInfo l1BlockInfo =
                L1BlockInfo.create(blockNumber, blockHash, block.getTimestamp(), baseFeePerGas, mixHash);
        List<String> batcherTransactions = createBatcherTransactions(block, systemConfig.batchSender(), batchInbox);

        return new L1Info(l1BlockInfo, systemConfig, userDeposits, batcherTransactions, null, finalized);
    }

    /**
     * Create L1Info.
     *
     * @param block the l1 block
     * @param userDeposits the user deposits
     * @param finalized if block is finalized
     * @param systemConfig the system config
     * @param batcherTransactions the batcher transactions
     * @param parentBeaconRoot the l1 parent beacon root hash
     * @return the L1Info instance
     */
    public static L1Info create(
            Block block,
            List<UserDeposited> userDeposits,
            boolean finalized,
            SystemConfig systemConfig,
            List<String> batcherTransactions,
            String parentBeaconRoot) {
        checkBlock(block);
        BigInteger blockNumber = block.getNumber();
        String blockHash = block.getHash();
        String mixHash = block.getMixHash();
        BigInteger baseFeePerGas = block.getBaseFeePerGas();
        BigInteger excessBlobGas = block.getExcessBlobGas();

        L1BlockInfo l1BlockInfo =
                L1BlockInfo.create(blockNumber, blockHash, block.getTimestamp(), baseFeePerGas, excessBlobGas, mixHash);
        return new L1Info(l1BlockInfo, systemConfig, userDeposits, batcherTransactions, parentBeaconRoot, finalized);
    }

    private static void checkBlock(Block block) {
        if (block.getNumber() == null) {
            throw new BlockNotIncludedException();
        }
        if (block.getHash() == null) {
            throw new BlockNotIncludedException();
        }
        if (block.getMixHash() == null) {
            throw new BlockNotIncludedException();
        }
        if (block.getBaseFeePerGas() == null) {
            throw new BlockIsPreLondonException();
        }
    }

    /**
     * Create batcher transactions
     *
     * @param block the l1 block
     * @param batchSender the batch sender address
     * @param batchInbox the batch inbox contract address
     * @return the list of batcher transactions
     */
    public static List<String> createBatcherTransactions(Block block, String batchSender, String batchInbox) {
        return block.getTransactions().stream()
                .filter(transactionResult ->
                        isValidBatcherTx((TransactionObject) transactionResult, batchSender, batchInbox))
                .map(transactionResult -> ((TransactionObject) transactionResult).getInput())
                .collect(Collectors.toList());
    }

    /**
     * Check if the transaction is a valid batcher transaction.
     *
     * @param tx the transaction
     * @param batchSender the batch sender address
     * @param batchInbox the batch inbox contract address
     * @return true if the transaction is a valid batcher transaction
     */
    public static boolean isValidBatcherTx(TransactionObject tx, String batchSender, String batchInbox) {
        return batchSender.equalsIgnoreCase(tx.getFrom()) && batchInbox.equalsIgnoreCase(tx.getTo());
    }

    /**
     * The type L1BlockInfo.
     *
     * @param number L1 block number
     * @param hash L1 block hash
     * @param timestamp L1 block timestamp
     * @param baseFee L1 base fee per gas
     * @param excessBlobGas L1 excess blob gas
     * @param mixHash L1 mix hash (prevrandao)
     * @author grapebaba
     * @since 0.1.0
     */
    public record L1BlockInfo(
            BigInteger number,
            String hash,
            BigInteger timestamp,
            BigInteger baseFee,
            BigInteger excessBlobGas,
            String mixHash) {

        /**
         * Create L1BlockInfo.
         *
         * @param number the number
         * @param hash the hash
         * @param timestamp the timestamp
         * @param baseFee the base fee
         * @param mixHash the mix hash
         * @return the l 1 block info
         */
        public static L1BlockInfo create(
                BigInteger number, String hash, BigInteger timestamp, BigInteger baseFee, String mixHash) {
            return new L1BlockInfo(number, hash, timestamp, baseFee, BigInteger.ZERO, mixHash);
        }

        /**
         * Create L1BlockInfo.
         *
         * @param number the number
         * @param hash the hash
         * @param timestamp the timestamp
         * @param baseFee the base fee
         * @param excessBlobGas the excess blob gas
         * @param mixHash the mix hash
         * @return the l 1 block info
         */
        public static L1BlockInfo create(
                BigInteger number,
                String hash,
                BigInteger timestamp,
                BigInteger baseFee,
                BigInteger excessBlobGas,
                String mixHash) {
            return new L1BlockInfo(number, hash, timestamp, baseFee, excessBlobGas, mixHash);
        }
    }
}
