package io.optimism.types;

import io.optimism.types.enums.BlockVersion;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.ssz.SSZ;
import org.apache.tuweni.units.bigints.UInt256;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.utils.Numeric;

/**
 * The type ExecutionPayloadSSZ.
 *
 * @param parentHash    the parent hash
 * @param feeRecipient  the fee recipient
 * @param stateRoot     the state root
 * @param receiptsRoot  the receipts root
 * @param logsBloom     the logs bloom
 * @param prevRandao    the prev randao
 * @param blockNumber   the block number
 * @param gasLimit      the gas limit
 * @param gasUsed       the gas used
 * @param timestamp     the timestamp
 * @param extraData     the extra data
 * @param baseFeePerGas the base fee per gas
 * @param blockHash     the block hash
 * @param transactions  the transactions
 * @param withdrawals   the withdrawals
 * @param blobGasUsed   the blob gas used
 * @param excessBlobGas the excess blob gas
 * @author grapebaba
 * @since 0.2.0
 */
public record ExecutionPayloadSSZ(
        Bytes parentHash,
        Bytes feeRecipient,
        Bytes stateRoot,
        Bytes receiptsRoot,
        Bytes logsBloom,
        Bytes prevRandao,
        long blockNumber,
        long gasLimit,
        long gasUsed,
        long timestamp,
        Bytes extraData,
        UInt256 baseFeePerGas,
        Bytes blockHash,
        List<Bytes> transactions,
        List<EthBlock.Withdrawal> withdrawals,
        Long blobGasUsed,
        Long excessBlobGas) {
    /**
     * The constant EXECUTION_PAYLOAD_FIXED_PART.
     */
    // All fields (4s are offsets to dynamic data)
    private static final int EXECUTION_PAYLOAD_FIXED_PART_V1 =
            32 + 20 + 32 + 32 + 256 + 32 + 8 + 8 + 8 + 8 + 4 + 32 + 32 + 4;

    // V1 + Withdrawals offset
    private static final int EXECUTION_PAYLOAD_FIXED_PART_V2 = EXECUTION_PAYLOAD_FIXED_PART_V1 + 4;

    // V2 + BlobGasUed + ExcessBlobGas
    private static final int EXECUTION_PAYLOAD_FIXED_PART_V3 = EXECUTION_PAYLOAD_FIXED_PART_V2 + 8 + 8;
    private static final int WITHDRAWAL_SIZE = 8 + 8 + 20 + 8;

    private static final int MAX_WITHDRAWALS_PER_PAYLOAD = 1 << 4;
    /**
     * The constant MAX_TRANSACTIONS_PER_PAYLOAD.
     */
    // MAX_TRANSACTIONS_PER_PAYLOAD in consensus spec
    private static final int MAX_TRANSACTIONS_PER_PAYLOAD = 1 << 20;

    /**
     * From execution payload ssz.
     *
     * @param data    the data
     * @param version the version
     * @return the execution payload ssz
     */
    public static ExecutionPayloadSSZ from(Bytes data, BlockVersion version) {
        final int dataSize = data.size();
        final int fixedPart = executionPayloadFixedPart(version);
        if (dataSize < fixedPart) {
            throw new IllegalArgumentException(
                    String.format("scope too small to decode execution payload: %d", data.size()));
        }

        return SSZ.decode(data, sszReader -> {
            int offset = 0;
            Bytes parentHash = sszReader.readHash(32);
            offset += 32;
            Bytes feeRecipient = sszReader.readAddress();
            offset += 20;
            Bytes stateRoot = sszReader.readFixedBytes(32);
            offset += 32;
            Bytes receiptsRoot = sszReader.readFixedBytes(32);
            offset += 32;
            Bytes logsBloom = sszReader.readFixedBytes(256);
            offset += 256;
            Bytes prevRandao = sszReader.readFixedBytes(32);
            offset += 32;
            long blockNumber = sszReader.readUInt64();
            offset += 8;
            long gasLimit = sszReader.readUInt64();
            offset += 8;
            long gasUsed = sszReader.readUInt64();
            offset += 8;
            long timestamp = sszReader.readUInt64();
            offset += 8;
            long extraDataOffset = sszReader.readUInt32();
            if (extraDataOffset != fixedPart) {
                throw new IllegalArgumentException(
                        String.format("unexpected extra data offset: %d <> %d", extraDataOffset, fixedPart));
            }
            offset += 4;
            UInt256 baseFeePerGas = sszReader.readUInt256();
            offset += 32;
            Bytes blockHash = sszReader.readHash(32);
            offset += 32;
            long transactionsOffset = sszReader.readUInt32();
            if (transactionsOffset < extraDataOffset) {
                throw new IllegalArgumentException(
                        String.format("unexpected transactions offset: %d < %d", transactionsOffset, extraDataOffset));
            }
            offset += 4;

            if (version == BlockVersion.V1 && offset != fixedPart) {
                throw new IllegalArgumentException(String.format(
                        "unexpected offset: %d <> %d, version: %d", offset, fixedPart, version.getVersion()));
            }

            long withdrawalsOffset = dataSize;
            if (version.hasWithdrawals()) {
                withdrawalsOffset = sszReader.readUInt32();
                offset += 4;
                if (withdrawalsOffset < transactionsOffset) {
                    throw new IllegalArgumentException(String.format(
                            "unexpected withdrawals offset: %d < %d", withdrawalsOffset, transactionsOffset));
                }
            }

            Long blobGasUsed = null;
            Long ExcessBlobGas = null;
            if (version == BlockVersion.V3) {
                blobGasUsed = sszReader.readUInt64();
                offset += 8;
                ExcessBlobGas = sszReader.readUInt64();
                offset += 8;
            }

            if (version == BlockVersion.V2 && offset != fixedPart) {
                throw new IllegalArgumentException(String.format(
                        "unexpected offset: %d <> %d, version: %d", offset, fixedPart, version.getVersion()));
            }

            // var _ = offset; // for future extensions: we keep the offset accurate for extensions

            if (transactionsOffset > extraDataOffset + 32 || transactionsOffset > dataSize) {
                throw new IllegalArgumentException(
                        String.format("extra-data is too large: %d", transactionsOffset - extraDataOffset));
            }

            Bytes extraData = Bytes.EMPTY;
            if (transactionsOffset != extraDataOffset) {
                extraData = sszReader.readFixedBytes((int) (transactionsOffset - extraDataOffset));
            }

            List<Bytes> transactions;
            if (sszReader.isComplete()) {
                transactions = List.of();
            } else {
                Bytes transactionsData = sszReader.readFixedBytes((int) (withdrawalsOffset - transactionsOffset));
                transactions = unmarshalTransactions(transactionsData);
            }

            List<EthBlock.Withdrawal> withdrawals = null;
            if (version.hasWithdrawals()) {
                if (withdrawalsOffset > dataSize) {
                    throw new IllegalArgumentException(
                            String.format("withdrawals is too large: %d", withdrawalsOffset - dataSize));
                }

                if (sszReader.isComplete()) {
                    withdrawals = List.of();
                } else {
                    Bytes withdrawalsData = sszReader.readFixedBytes((int) (dataSize - withdrawalsOffset));
                    withdrawals = unmarshalWithdrawals(withdrawalsData);
                }
            }

            return new ExecutionPayloadSSZ(
                    parentHash,
                    feeRecipient,
                    stateRoot,
                    receiptsRoot,
                    logsBloom,
                    prevRandao,
                    blockNumber,
                    gasLimit,
                    gasUsed,
                    timestamp,
                    extraData,
                    baseFeePerGas,
                    blockHash,
                    transactions,
                    withdrawals,
                    blobGasUsed,
                    ExcessBlobGas);
        });
    }

    private static List<EthBlock.Withdrawal> unmarshalWithdrawals(Bytes withdrawalsData) {
        List<EthBlock.Withdrawal> result = new ArrayList<>();

        if (withdrawalsData.size() % WITHDRAWAL_SIZE != 0) {
            throw new IllegalArgumentException(
                    String.format("invalid withdrawals data size: %d", withdrawalsData.size()));
        }

        int withdrawalCount = withdrawalsData.size() / WITHDRAWAL_SIZE;

        if (withdrawalCount > MAX_WITHDRAWALS_PER_PAYLOAD) {
            throw new IllegalArgumentException(
                    String.format("too many withdrawals: %d > %d", withdrawalCount, MAX_WITHDRAWALS_PER_PAYLOAD));
        }

        AtomicInteger offset = new AtomicInteger();
        for (int i = 0; i < withdrawalCount; i++) {
            Bytes withdrawalData = withdrawalsData.slice(offset.get(), WITHDRAWAL_SIZE);
            EthBlock.Withdrawal withdrawal = SSZ.decode(withdrawalData, sszReader -> {
                String index = Numeric.encodeQuantity(BigInteger.valueOf(sszReader.readUInt64()));
                offset.addAndGet(8);
                String validatorIndex = Numeric.encodeQuantity(BigInteger.valueOf(sszReader.readUInt64()));
                offset.addAndGet(8);
                String address = sszReader.readAddress().toString();
                offset.addAndGet(20);
                String amount = Numeric.encodeQuantity(BigInteger.valueOf(sszReader.readUInt64()));
                offset.addAndGet(8);

                return new EthBlock.Withdrawal(index, validatorIndex, address, amount);
            });

            result.add(withdrawal);
        }

        return result;
    }

    private static List<Bytes> unmarshalTransactions(Bytes data) {
        if (data.isEmpty()) {
            return List.of();
        }

        return SSZ.decode(data, txsSSZReader -> {
            int transactionsBytesSize = data.size();
            if (transactionsBytesSize < 4) {
                throw new IllegalArgumentException(
                        String.format("not enough scope to read first tx offset: %d", transactionsBytesSize));
            }

            long firstTxOffset = txsSSZReader.readUInt32();
            if (firstTxOffset % 4 != 0) {
                throw new IllegalArgumentException(
                        String.format("invalid first tx offset: %d, not a multiple of offset size", firstTxOffset));
            }
            if (firstTxOffset > transactionsBytesSize) {
                throw new IllegalArgumentException(String.format(
                        "invalid first tx offset: %d, out of scope %d", firstTxOffset, transactionsBytesSize));
            }
            int txCount = (int) firstTxOffset / 4;
            if (txCount > MAX_TRANSACTIONS_PER_PAYLOAD) {
                throw new IllegalArgumentException(
                        String.format("too many transactions: %d > %d", txCount, MAX_TRANSACTIONS_PER_PAYLOAD));
            }
            List<Long> nextOffsets = new ArrayList<>(txCount);
            long currentTxOffset = firstTxOffset;
            for (int i = 0; i < txCount; i++) {
                long nextTxOffset = transactionsBytesSize;
                if (i + 1 < txCount) {
                    nextTxOffset = txsSSZReader.readUInt32();
                }
                if (nextTxOffset < currentTxOffset || nextTxOffset > transactionsBytesSize) {
                    throw new IllegalArgumentException(String.format(
                            "tx %d has bad next offset: %d, current is %d, scope is %d",
                            i, nextTxOffset, currentTxOffset, transactionsBytesSize));
                }
                nextOffsets.add(nextTxOffset);
                currentTxOffset = nextTxOffset;
            }
            List<Bytes> txs = new ArrayList<>(txCount);
            long currentTxOffset1 = firstTxOffset;
            for (int i = 0; i < txCount; i++) {
                long nextTxOffset = nextOffsets.get(i);
                int currentTxSize = (int) (nextTxOffset - currentTxOffset1);
                Bytes transaction =
                        txsSSZReader.isComplete() ? Bytes.EMPTY : txsSSZReader.readFixedBytes(currentTxSize);
                txs.add(transaction);
                currentTxOffset1 = nextTxOffset;
            }

            if (!txsSSZReader.isComplete()) {
                throw new IllegalArgumentException("txsSSZReader is not complete");
            }
            return txs;
        });
    }

    @Override
    public String toString() {
        return "ExecutionPayloadSSZ{parentHash=%s, feeRecipient=%s, stateRoot=%s, receiptsRoot=%s, logsBloom=%s, prevRandao=%s, blockNumber=%d, gasLimit=%d, gasUsed=%d, timestamp=%d, extraData=%s, baseFeePerGas=%s, blockHash=%s, withdrawals=%s, transactions=%s}"
                .formatted(
                        parentHash,
                        feeRecipient,
                        stateRoot,
                        receiptsRoot,
                        logsBloom,
                        prevRandao,
                        blockNumber,
                        gasLimit,
                        gasUsed,
                        timestamp,
                        extraData,
                        baseFeePerGas,
                        blockHash,
                        withdrawals,
                        transactions);
    }

    private static int executionPayloadFixedPart(BlockVersion version) {
        if (version == BlockVersion.V3) {
            return EXECUTION_PAYLOAD_FIXED_PART_V3;
        } else if (version == BlockVersion.V2) {
            return EXECUTION_PAYLOAD_FIXED_PART_V2;
        } else {
            return EXECUTION_PAYLOAD_FIXED_PART_V1;
        }
    }
}
