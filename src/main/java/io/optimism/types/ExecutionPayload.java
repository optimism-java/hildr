package io.optimism.types;

import io.optimism.config.Config;
import io.optimism.rpc.response.OpEthBlock;
import io.optimism.types.enums.TxType;
import io.optimism.utilities.encoding.TxDecoder;
import io.optimism.utilities.encoding.TxEncoder;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.utils.Numeric;

/**
 * The type ExecutionPayload.
 *
 * @param parentHash            A 32 byte hash of the parent payload.
 * @param feeRecipient          A 20 byte hash (aka Address) for the feeRecipient field of the new payload.
 * @param stateRoot             A 32 byte state root hash.
 * @param receiptsRoot          A 32 byte receipt root hash.
 * @param logsBloom             A 32 byte logs bloom filter.
 * @param prevRandao            A 32 byte beacon chain randomness value.
 * @param blockNumber           A 64-bit number for the current block index.
 * @param gasLimit              A 64-bit value for the gas limit.
 * @param gasUsed               A 64-bit value for the gas used.
 * @param timestamp             A 64-bit value for the timestamp field of the new payload.
 * @param extraData             0 to 32 byte value for extra data.
 * @param baseFeePerGas         256 bits for the base fee per gas.
 * @param blockHash             The 32 byte block hash.
 * @param transactions          An array of transaction objects where each object is a byte list.
 * @param withdrawals           An array of withdrawal objects where each object is a byte list.
 * @param blobGasUsed           The gas used by the blob.
 * @param excessBlobGas         The excess gas used by the blob.
 * @param parentBeaconBlockRoot The parent beacon block root.
 * @author grapebaba
 * @since 0.1.0
 */
public record ExecutionPayload(
        String parentHash,
        String feeRecipient,
        String stateRoot,
        String receiptsRoot,
        String logsBloom,
        String prevRandao,
        BigInteger blockNumber,
        BigInteger gasLimit,
        BigInteger gasUsed,
        BigInteger timestamp,
        String extraData,
        BigInteger baseFeePerGas,
        String blockHash,
        List<String> transactions,
        List<EthBlock.Withdrawal> withdrawals,
        BigInteger blobGasUsed,
        BigInteger excessBlobGas,
        String parentBeaconBlockRoot) {

    /**
     * Converts the ExecutionPayload to an L2BlockRef.
     * @param config the chain config
     * @return the L2BlockRef
     */
    public L2BlockRef toL2BlockInfo(Config.ChainConfig config) {
        final Epoch l1GenesisEpoch = config.l1StartEpoch();
        final BlockInfo l2GenesisInfo = config.l2Genesis();
        BigInteger seqNumber;
        Epoch l1Origin;
        if (this.blockNumber.compareTo(l2GenesisInfo.number()) == 0) {
            if (!l2GenesisInfo.hash().equals(this.blockHash)) {
                throw new IllegalArgumentException("expected L2 genesis hash to match L2 block at genesis block number "
                        + l2GenesisInfo.number()
                        + ": "
                        + this.blockHash
                        + " <> "
                        + l2GenesisInfo.hash());
            }
            l1Origin = l1GenesisEpoch;
            seqNumber = BigInteger.ZERO;
        } else {
            if (transactions == null || transactions.isEmpty()) {
                throw new IllegalArgumentException(
                        "l2 block is missing L1 info deposit tx, block hash: " + this.blockHash);
            }
            String txData = transactions.getFirst();
            DepositTransaction depositTx = TxDecoder.decodeToDeposit(txData);
            L1BlockInfo l1Info = L1BlockInfo.from(Numeric.hexStringToByteArray(depositTx.getData()));
            l1Origin = l1Info.toEpoch();
            seqNumber = l1Info.sequenceNumber();
        }
        return new L2BlockRef(this.blockHash, this.blockNumber, this.parentHash, this.timestamp, l1Origin, seqNumber);
    }

    /**
     * The type Execution payload res.
     *
     * @param parentHash    A 32 byte hash of the parent payload.
     * @param feeRecipient  A 20 byte hash (aka Address) for the feeRecipient field of the new payload.
     * @param stateRoot     A 32 byte state root hash.
     * @param receiptsRoot  A 32 byte receipt root hash.
     * @param logsBloom     A 32 byte logs bloom filter.
     * @param prevRandao    A 32 byte beacon chain randomness value.
     * @param blockNumber   A 64-bit number for the current block index.
     * @param gasLimit      A 64-bit value for the gas limit.
     * @param gasUsed       A 64-bit value for the gas used.
     * @param timestamp     A 64-bit value for the timestamp field of the new payload.
     * @param extraData     0 to 32 byte value for extra data.
     * @param baseFeePerGas 256 bits for the base fee per gas.
     * @param blockHash     The 32 byte block hash.
     * @param withdrawals   An array of withdrawal objects where each object is a byte list.
     * @param transactions  An array of transaction objects where each object is a byte list.
     * @param blobGasUsed   The gas used by the blob.
     * @param excessBlobGas The excess gas used by the blob.
     */
    public record ExecutionPayloadRes(
            String parentHash,
            String feeRecipient,
            String stateRoot,
            String receiptsRoot,
            String logsBloom,
            String prevRandao,
            String blockNumber,
            String gasLimit,
            String gasUsed,
            String timestamp,
            String extraData,
            String baseFeePerGas,
            String blockHash,
            List<String> transactions,
            List<EthBlock.Withdrawal> withdrawals,
            String blobGasUsed,
            String excessBlobGas) {

        /**
         * To execution payload execution payload.
         *
         * @param parentBeaconBlockRoot the parent beacon block root
         * @return the execution payload
         */
        public ExecutionPayload toExecutionPayload(String parentBeaconBlockRoot) {
            return new ExecutionPayload(
                    parentHash,
                    feeRecipient,
                    stateRoot,
                    receiptsRoot,
                    logsBloom,
                    prevRandao,
                    Numeric.decodeQuantity(blockNumber),
                    Numeric.decodeQuantity(gasLimit),
                    Numeric.decodeQuantity(gasUsed),
                    Numeric.decodeQuantity(timestamp),
                    extraData,
                    Numeric.decodeQuantity(baseFeePerGas),
                    blockHash,
                    transactions,
                    withdrawals,
                    StringUtils.isEmpty(blobGasUsed) ? null : Numeric.decodeQuantity(blobGasUsed),
                    StringUtils.isEmpty(excessBlobGas) ? null : Numeric.decodeQuantity(excessBlobGas),
                    parentBeaconBlockRoot);
        }
    }

    /**
     * From execution payload.
     *
     * @param block the L2 block
     * @param config the chain config
     * @return the execution payload
     */
    public static ExecutionPayload fromL2Block(OpEthBlock.Block block, Config.ChainConfig config) {
        boolean isSystemTx = block.getTimestamp().compareTo(config.regolithTime()) < 0;
        List<String> encodedTxs = block.getTransactions().stream()
                .map(tx -> {
                    var txObj = ((OpEthBlock.TransactionObject) tx);
                    if (TxType.OPTIMISM_DEPOSIT.is(txObj.getType())) {
                        return Numeric.toHexString(TxEncoder.encodeDepositTx(txObj, isSystemTx));
                    } else {
                        return Numeric.toHexString(TxEncoder.encode(txObj.toWeb3j()));
                    }
                })
                .collect(Collectors.toList());

        return new ExecutionPayload(
                block.getParentHash(),
                StringUtils.isNotEmpty(block.getAuthor()) ? block.getAuthor() : block.getMiner(),
                block.getStateRoot(),
                block.getReceiptsRoot(),
                block.getLogsBloom(),
                block.getMixHash(),
                block.getNumber(),
                block.getGasLimit(),
                block.getGasUsed(),
                block.getTimestamp(),
                block.getExtraData(),
                block.getBaseFeePerGas(),
                block.getHash(),
                encodedTxs,
                block.getWithdrawals(),
                block.getBlobGasUsed(),
                block.getExcessBlobGas(),
                block.getParentBeaconBlockRoot());
    }

    /**
     * From ExecutionPayloadSSZ to ExecutionPayload.
     *
     * @param payload the ExecutionPayloadSSZ
     * @return the ExecutionPayload
     */
    public static ExecutionPayload from(ExecutionPayloadSSZ payload) {
        return new ExecutionPayload(
                Numeric.toHexString(payload.parentHash().toArray()),
                Numeric.toHexString(payload.feeRecipient().toArray()),
                Numeric.toHexString(payload.stateRoot().toArray()),
                Numeric.toHexString(payload.receiptsRoot().toArray()),
                Numeric.toHexString(payload.logsBloom().toArray()),
                Numeric.toHexString(payload.prevRandao().toArray()),
                BigInteger.valueOf(payload.blockNumber()),
                BigInteger.valueOf(payload.gasLimit()),
                BigInteger.valueOf(payload.gasUsed()),
                BigInteger.valueOf(payload.timestamp()),
                Numeric.toHexString(payload.extraData().toArray()),
                payload.baseFeePerGas().toBigInteger(),
                Numeric.toHexString(payload.blockHash().toArray()),
                payload.transactions().stream()
                        .map(bytes -> Numeric.toHexString(bytes.toArray()))
                        .collect(Collectors.toList()),
                payload.withdrawals(),
                payload.blobGasUsed() == null ? null : BigInteger.valueOf(payload.blobGasUsed()),
                payload.excessBlobGas() == null ? null : BigInteger.valueOf(payload.excessBlobGas()),
                null);
    }

    /**
     * From ExecutionPayloadSSZ to ExecutionPayload.
     *
     * @param payload the ExecutionPayloadSSZ
     * @param parentBeaconBlockRoot the l1 parent beacon block root
     * @return the ExecutionPayload
     */
    public static ExecutionPayload from(ExecutionPayloadSSZ payload, String parentBeaconBlockRoot) {
        return new ExecutionPayload(
                Numeric.toHexString(payload.parentHash().toArray()),
                Numeric.toHexString(payload.feeRecipient().toArray()),
                Numeric.toHexString(payload.stateRoot().toArray()),
                Numeric.toHexString(payload.receiptsRoot().toArray()),
                Numeric.toHexString(payload.logsBloom().toArray()),
                Numeric.toHexString(payload.prevRandao().toArray()),
                BigInteger.valueOf(payload.blockNumber()),
                BigInteger.valueOf(payload.gasLimit()),
                BigInteger.valueOf(payload.gasUsed()),
                BigInteger.valueOf(payload.timestamp()),
                Numeric.toHexString(payload.extraData().toArray()),
                payload.baseFeePerGas().toBigInteger(),
                Numeric.toHexString(payload.blockHash().toArray()),
                payload.transactions().stream()
                        .map(bytes -> Numeric.toHexString(bytes.toArray()))
                        .collect(Collectors.toList()),
                payload.withdrawals(),
                payload.blobGasUsed() == null ? null : BigInteger.valueOf(payload.blobGasUsed()),
                payload.excessBlobGas() == null ? null : BigInteger.valueOf(payload.excessBlobGas()),
                parentBeaconBlockRoot);
    }

    /**
     * The type Execution payload req.
     *
     * @param parentHash    A 32 byte hash of the parent payload.
     * @param feeRecipient  A 20 byte hash (aka Address) for the feeRecipient field of the new payload.
     * @param stateRoot     A 32 byte state root hash.
     * @param receiptsRoot  A 32 byte receipt root hash.
     * @param logsBloom     A 32 byte logs bloom filter.
     * @param prevRandao    A 32 byte beacon chain randomness value.
     * @param blockNumber   A 64-bit number for the current block index.
     * @param gasLimit      A 64-bit value for the gas limit.
     * @param gasUsed       A 64-bit value for the gas used.
     * @param timestamp     A 64-bit value for the timestamp field of the new payload.
     * @param extraData     0 to 32 byte value for extra data.
     * @param baseFeePerGas 256 bits for the base fee per gas.
     * @param blockHash     The 32 byte block hash.
     * @param withdrawals   The withdrawals list.
     * @param transactions  An array of transaction objects where each object is a byte list.
     * @param blobGasUsed   The gas used by the blob.
     * @param excessBlobGas The excess gas used by the blob.
     */
    public record ExecutionPayloadReq(
            String parentHash,
            String feeRecipient,
            String stateRoot,
            String receiptsRoot,
            String logsBloom,
            String prevRandao,
            String blockNumber,
            String gasLimit,
            String gasUsed,
            String timestamp,
            String extraData,
            String baseFeePerGas,
            String blockHash,
            List<String> transactions,
            List<EthBlock.Withdrawal> withdrawals,
            String blobGasUsed,
            String excessBlobGas) {}

    /**
     * To req execution payload req.
     *
     * @return the execution payload req
     */
    public ExecutionPayloadReq toReq() {
        return new ExecutionPayloadReq(
                parentHash,
                feeRecipient,
                stateRoot,
                receiptsRoot,
                logsBloom,
                prevRandao,
                Numeric.toHexStringWithPrefix(blockNumber),
                Numeric.toHexStringWithPrefix(gasLimit),
                Numeric.toHexStringWithPrefix(gasUsed),
                Numeric.toHexStringWithPrefix(timestamp),
                extraData,
                Numeric.toHexStringWithPrefix(baseFeePerGas),
                blockHash,
                transactions,
                withdrawals,
                blobGasUsed == null ? null : Numeric.toHexStringWithPrefix(blobGasUsed),
                excessBlobGas == null ? null : Numeric.toHexStringWithPrefix(excessBlobGas));
    }

    /**
     * The type PayloadAttributes.
     *
     * <p>L2 extended payload attributes for Optimism. For more details, visit the [Optimism specs](<a
     * href="https://github.com/ethereum-optimism/optimism/blob/develop/specs/exec-engine.md#extended-payloadattributesv1">...</a>).
     *
     * @param timestamp             64 bit value for the timestamp field of the new payload.
     * @param prevRandao            32 byte value for the prevRandao field of the new payload.
     * @param suggestedFeeRecipient 20 bytes suggested value for the feeRecipient field of the new
     *                              payload.
     * @param transactions          List of transactions to be included in the new payload.
     * @param withdrawals           List of withdrawals to be included in the new payload.
     * @param noTxPool              Boolean value indicating whether the payload should be built without including
     *                              transactions from the txpool.
     * @param gasLimit              64 bit value for the gasLimit field of the new payload.The gasLimit is optional
     *                              w.r.t. compatibility with L1, but required when used as rollup.This field overrides the gas
     *                              limit used during block-building.If not specified as rollup, a STATUS_INVALID is returned.
     * @param epoch                 The batch epoch number from derivation. This value is not expected by the engine
     *                              is skipped during serialization and deserialization.
     * @param l1InclusionBlock      The L1 block number when this batch was first fully derived. This value
     *                              is not expected by the engine and is skipped during serialization and deserialization.
     * @param seqNumber             The L2 sequence number of the block. This value is not expected by the engine
     *                              and is skipped during serialization and deserialization.
     * @param parentBeaconBlockRoot The parent beacon block root.
     * @author zhouop0
     * @since 0.1.0
     */
    public record PayloadAttributes(
            BigInteger timestamp,
            String prevRandao,
            String suggestedFeeRecipient,
            List<String> transactions,
            List<EthBlock.Withdrawal> withdrawals,
            boolean noTxPool,
            BigInteger gasLimit,
            Epoch epoch,
            BigInteger l1InclusionBlock,
            BigInteger seqNumber,
            String parentBeaconBlockRoot) {

        /**
         * The type Epoch req.
         *
         * @param number    the number
         * @param hash      the hash
         * @param timestamp the timestamp
         */
        public record EpochReq(String number, String hash, String timestamp) {}

        /**
         * The type Payload attributes req.
         *
         * @param timestamp             the timestamp
         * @param prevRandao            the prev randao
         * @param suggestedFeeRecipient the suggested fee recipient
         * @param transactions          the transactions
         * @param withdrawals           the withdrawals
         * @param noTxPool              the no tx pool
         * @param gasLimit              the gas limit
         * @param parentBeaconBlockRoot the parent beacon block root
         */
        public record PayloadAttributesReq(
                String timestamp,
                String prevRandao,
                String suggestedFeeRecipient,
                List<String> transactions,
                List<EthBlock.Withdrawal> withdrawals,
                boolean noTxPool,
                String gasLimit,
                String parentBeaconBlockRoot) {}

        /**
         * To req payload attributes req.
         *
         * @return the payload attributes req
         */
        public PayloadAttributesReq toReq() {
            return new PayloadAttributesReq(
                    Numeric.encodeQuantity(timestamp),
                    prevRandao,
                    suggestedFeeRecipient,
                    transactions,
                    withdrawals,
                    noTxPool,
                    Numeric.encodeQuantity(gasLimit),
                    parentBeaconBlockRoot);
        }
    }

    /**
     * The type Status.
     *
     * @author zhouop0
     * @since 0.1.0
     */
    public enum Status {
        /**
         * Valid status.
         */
        VALID,
        /**
         * Invalid status.
         */
        INVALID,
        /**
         * Syncing status.
         */
        SYNCING,
        /**
         * Accepted status.
         */
        ACCEPTED,
        /**
         * Invalid block hash status.
         */
        INVALID_BLOCK_HASH,
    }

    /**
     * The type PayloadStatus.
     *
     * <p>status The status of the payload. latestValidHash 32 Bytes - the hash of the most recent
     * valid block in the branch defined by payload and its ancestors. validationError A message
     * providing additional details on the validation error if the payload is classified as INVALID or
     * INVALID_BLOCK_HASH.
     *
     * @author zhouop0
     * @since 0.1.0
     */
    public static class PayloadStatus {

        private Status status;
        private String latestValidHash;
        private String validationError;

        /**
         * PayloadStatus constructor.
         */
        public PayloadStatus() {}

        /**
         * status get method.
         *
         * @return status. status
         */
        public Status getStatus() {
            return status;
        }

        /**
         * status set method.
         *
         * @param status status.
         */
        public void setStatus(Status status) {
            this.status = status;
        }

        /**
         * latestValidHash get method.
         *
         * @return latestValidHash. latest valid hash
         */
        public String getLatestValidHash() {
            return latestValidHash;
        }

        /**
         * latestValidHash set method.
         *
         * @param latestValidHash latestValidHash.
         */
        public void setLatestValidHash(String latestValidHash) {
            this.latestValidHash = latestValidHash;
        }

        /**
         * validationError get method.
         *
         * @return validationError. validation error
         */
        public String getValidationError() {
            return validationError;
        }

        /**
         * validationError set method.
         *
         * @param validationError validationError.
         */
        public void setValidationError(String validationError) {
            this.validationError = validationError;
        }
    }
}
