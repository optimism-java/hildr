package io.optimism.rpc.response;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.core.Response;
import org.web3j.protocol.core.methods.response.AccessListObject;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.utils.Numeric;

/**
 * Block object returned by:
 *
 * <ul>
 *   <li>eth_getBlockByHash
 *   <li>eth_getBlockByNumber
 *   <li>eth_getUncleByBlockHashAndIndex
 *   <li>eth_getUncleByBlockNumberAndIndex
 * </ul>
 *
 * <p>See <a href="https://github.com/ethereum/wiki/wiki/JSON-RPC#eth_gettransactionbyhash">docs</a>
 * for further details.
 *
 * <p>See the following <a href="https://github.com/ethcore/parity/issues/2401">issue</a> for
 * details on additional Parity fields present in EthBlock.
 */
public class OpEthBlock extends Response<OpEthBlock.Block> {

    /**
     * Instantiates a new Op eth block.
     */
    public OpEthBlock() {}

    @Override
    @JsonDeserialize(using = ResponseDeserialiser.class)
    public void setResult(Block result) {
        super.setResult(result);
    }

    /**
     * Gets block.
     *
     * @return the block
     */
    public Block getBlock() {
        return getResult();
    }

    /**
     * The type Block.
     */
    public static class Block {
        private String number;
        private String hash;
        private String parentHash;
        private String parentBeaconBlockRoot;
        private String nonce;
        private String sha3Uncles;
        private String logsBloom;
        private String transactionsRoot;
        private String stateRoot;
        private String receiptsRoot;
        private String author;
        private String miner;
        private String mixHash;
        private String difficulty;
        private String totalDifficulty;
        private String extraData;
        private String size;
        private String gasLimit;
        private String gasUsed;
        private String timestamp;
        private List<TransactionResult> transactions;
        private List<String> uncles;
        private List<String> sealFields;
        private String baseFeePerGas;
        private String withdrawalsRoot;
        private List<EthBlock.Withdrawal> withdrawals;
        private String blobGasUsed;
        private String excessBlobGas;

        /**
         * Instantiates a new Block.
         */
        public Block() {}

        /**
         * Instantiates a new Block.
         *
         * @param number                the number
         * @param hash                  the hash
         * @param parentHash            the parent hash
         * @param parentBeaconBlockRoot the parent beacon block root
         * @param nonce                 the nonce
         * @param sha3Uncles            the sha 3 uncles
         * @param logsBloom             the logs bloom
         * @param transactionsRoot      the transactions root
         * @param stateRoot             the state root
         * @param receiptsRoot          the receipts root
         * @param author                the author
         * @param miner                 the miner
         * @param mixHash               the mix hash
         * @param difficulty            the difficulty
         * @param totalDifficulty       the total difficulty
         * @param extraData             the extra data
         * @param size                  the size
         * @param gasLimit              the gas limit
         * @param gasUsed               the gas used
         * @param timestamp             the timestamp
         * @param transactions          the transactions
         * @param uncles                the uncles
         * @param sealFields            the seal fields
         * @param baseFeePerGas         the base fee per gas
         * @param withdrawalsRoot       the withdrawals root
         * @param withdrawals           the withdrawals
         * @param blobGasUsed           the blob gas used
         * @param excessBlobGas         the excess blob gas
         */
        public Block(
                String number,
                String hash,
                String parentHash,
                String parentBeaconBlockRoot,
                String nonce,
                String sha3Uncles,
                String logsBloom,
                String transactionsRoot,
                String stateRoot,
                String receiptsRoot,
                String author,
                String miner,
                String mixHash,
                String difficulty,
                String totalDifficulty,
                String extraData,
                String size,
                String gasLimit,
                String gasUsed,
                String timestamp,
                List<TransactionResult> transactions,
                List<String> uncles,
                List<String> sealFields,
                String baseFeePerGas,
                String withdrawalsRoot,
                List<EthBlock.Withdrawal> withdrawals,
                String blobGasUsed,
                String excessBlobGas) {
            this.number = number;
            this.hash = hash;
            this.parentHash = parentHash;
            this.parentBeaconBlockRoot = parentBeaconBlockRoot;
            this.nonce = nonce;
            this.sha3Uncles = sha3Uncles;
            this.logsBloom = logsBloom;
            this.transactionsRoot = transactionsRoot;
            this.stateRoot = stateRoot;
            this.receiptsRoot = receiptsRoot;
            this.author = author;
            this.miner = miner;
            this.mixHash = mixHash;
            this.difficulty = difficulty;
            this.totalDifficulty = totalDifficulty;
            this.extraData = extraData;
            this.size = size;
            this.gasLimit = gasLimit;
            this.gasUsed = gasUsed;
            this.timestamp = timestamp;
            this.transactions = transactions;
            this.uncles = uncles;
            this.sealFields = sealFields;
            this.baseFeePerGas = baseFeePerGas;
            this.withdrawalsRoot = withdrawalsRoot;
            this.withdrawals = withdrawals;
            this.blobGasUsed = blobGasUsed;
            this.excessBlobGas = excessBlobGas;
        }

        /**
         * Instantiates a new Block.
         *
         * @param number           the number
         * @param hash             the hash
         * @param parentHash       the parent hash
         * @param nonce            the nonce
         * @param sha3Uncles       the sha 3 uncles
         * @param logsBloom        the logs bloom
         * @param transactionsRoot the transactions root
         * @param stateRoot        the state root
         * @param receiptsRoot     the receipts root
         * @param author           the author
         * @param miner            the miner
         * @param mixHash          the mix hash
         * @param difficulty       the difficulty
         * @param totalDifficulty  the total difficulty
         * @param extraData        the extra data
         * @param size             the size
         * @param gasLimit         the gas limit
         * @param gasUsed          the gas used
         * @param timestamp        the timestamp
         * @param transactions     the transactions
         * @param uncles           the uncles
         * @param sealFields       the seal fields
         * @param baseFeePerGas    the base fee per gas
         * @param withdrawalsRoot  the withdrawals root
         * @param withdrawals      the withdrawals
         */
        public Block(
                String number,
                String hash,
                String parentHash,
                String nonce,
                String sha3Uncles,
                String logsBloom,
                String transactionsRoot,
                String stateRoot,
                String receiptsRoot,
                String author,
                String miner,
                String mixHash,
                String difficulty,
                String totalDifficulty,
                String extraData,
                String size,
                String gasLimit,
                String gasUsed,
                String timestamp,
                List<TransactionResult> transactions,
                List<String> uncles,
                List<String> sealFields,
                String baseFeePerGas,
                String withdrawalsRoot,
                List<EthBlock.Withdrawal> withdrawals) {
            this.number = number;
            this.hash = hash;
            this.parentHash = parentHash;
            this.nonce = nonce;
            this.sha3Uncles = sha3Uncles;
            this.logsBloom = logsBloom;
            this.transactionsRoot = transactionsRoot;
            this.stateRoot = stateRoot;
            this.receiptsRoot = receiptsRoot;
            this.author = author;
            this.miner = miner;
            this.mixHash = mixHash;
            this.difficulty = difficulty;
            this.totalDifficulty = totalDifficulty;
            this.extraData = extraData;
            this.size = size;
            this.gasLimit = gasLimit;
            this.gasUsed = gasUsed;
            this.timestamp = timestamp;
            this.transactions = transactions;
            this.uncles = uncles;
            this.sealFields = sealFields;
            this.baseFeePerGas = baseFeePerGas;
            this.withdrawalsRoot = withdrawalsRoot;
            this.withdrawals = withdrawals;
        }

        /**
         * Gets number.
         *
         * @return the number
         */
        public BigInteger getNumber() {
            return Numeric.decodeQuantity(number);
        }

        /**
         * Gets number raw.
         *
         * @return the number raw
         */
        public String getNumberRaw() {
            return number;
        }

        /**
         * Sets number.
         *
         * @param number the number
         */
        public void setNumber(String number) {
            this.number = number;
        }

        /**
         * Gets hash.
         *
         * @return the hash
         */
        public String getHash() {
            return hash;
        }

        /**
         * Sets hash.
         *
         * @param hash the hash
         */
        public void setHash(String hash) {
            this.hash = hash;
        }

        /**
         * Gets parent hash.
         *
         * @return the parent hash
         */
        public String getParentHash() {
            return parentHash;
        }

        /**
         * Sets parent hash.
         *
         * @param parentHash the parent hash
         */
        public void setParentHash(String parentHash) {
            this.parentHash = parentHash;
        }

        /**
         * Gets parent beacon block root.
         *
         * @return the parent beacon block root
         */
        public String getParentBeaconBlockRoot() {
            return parentBeaconBlockRoot;
        }

        /**
         * Sets parent beacon block root.
         *
         * @param parentBeaconBlockRoot the parent beacon block root
         */
        public void setParentBeaconBlockRoot(String parentBeaconBlockRoot) {
            this.parentBeaconBlockRoot = parentBeaconBlockRoot;
        }

        /**
         * Gets nonce.
         *
         * @return the nonce
         */
        public BigInteger getNonce() {
            return Numeric.decodeQuantity(nonce);
        }

        /**
         * Gets nonce raw.
         *
         * @return the nonce raw
         */
        public String getNonceRaw() {
            return nonce;
        }

        /**
         * Sets nonce.
         *
         * @param nonce the nonce
         */
        public void setNonce(String nonce) {
            this.nonce = nonce;
        }

        /**
         * Gets sha 3 uncles.
         *
         * @return the sha 3 uncles
         */
        public String getSha3Uncles() {
            return sha3Uncles;
        }

        /**
         * Sets sha 3 uncles.
         *
         * @param sha3Uncles the sha 3 uncles
         */
        public void setSha3Uncles(String sha3Uncles) {
            this.sha3Uncles = sha3Uncles;
        }

        /**
         * Gets logs bloom.
         *
         * @return the logs bloom
         */
        public String getLogsBloom() {
            return logsBloom;
        }

        /**
         * Sets logs bloom.
         *
         * @param logsBloom the logs bloom
         */
        public void setLogsBloom(String logsBloom) {
            this.logsBloom = logsBloom;
        }

        /**
         * Gets transactions root.
         *
         * @return the transactions root
         */
        public String getTransactionsRoot() {
            return transactionsRoot;
        }

        /**
         * Sets transactions root.
         *
         * @param transactionsRoot the transactions root
         */
        public void setTransactionsRoot(String transactionsRoot) {
            this.transactionsRoot = transactionsRoot;
        }

        /**
         * Gets state root.
         *
         * @return the state root
         */
        public String getStateRoot() {
            return stateRoot;
        }

        /**
         * Sets state root.
         *
         * @param stateRoot the state root
         */
        public void setStateRoot(String stateRoot) {
            this.stateRoot = stateRoot;
        }

        /**
         * Gets receipts root.
         *
         * @return the receipts root
         */
        public String getReceiptsRoot() {
            return receiptsRoot;
        }

        /**
         * Sets receipts root.
         *
         * @param receiptsRoot the receipts root
         */
        public void setReceiptsRoot(String receiptsRoot) {
            this.receiptsRoot = receiptsRoot;
        }

        /**
         * Gets author.
         *
         * @return the author
         */
        public String getAuthor() {
            return author;
        }

        /**
         * Sets author.
         *
         * @param author the author
         */
        public void setAuthor(String author) {
            this.author = author;
        }

        /**
         * Gets miner.
         *
         * @return the miner
         */
        public String getMiner() {
            return miner;
        }

        /**
         * Sets miner.
         *
         * @param miner the miner
         */
        public void setMiner(String miner) {
            this.miner = miner;
        }

        /**
         * Gets mix hash.
         *
         * @return the mix hash
         */
        public String getMixHash() {
            return mixHash;
        }

        /**
         * Sets mix hash.
         *
         * @param mixHash the mix hash
         */
        public void setMixHash(String mixHash) {
            this.mixHash = mixHash;
        }

        /**
         * Gets difficulty.
         *
         * @return the difficulty
         */
        public BigInteger getDifficulty() {
            return Numeric.decodeQuantity(difficulty);
        }

        /**
         * Gets difficulty raw.
         *
         * @return the difficulty raw
         */
        public String getDifficultyRaw() {
            return difficulty;
        }

        /**
         * Sets difficulty.
         *
         * @param difficulty the difficulty
         */
        public void setDifficulty(String difficulty) {
            this.difficulty = difficulty;
        }

        /**
         * Gets total difficulty.
         *
         * @return the total difficulty
         */
        public BigInteger getTotalDifficulty() {
            return Numeric.decodeQuantity(totalDifficulty);
        }

        /**
         * Gets total difficulty raw.
         *
         * @return the total difficulty raw
         */
        public String getTotalDifficultyRaw() {
            return totalDifficulty;
        }

        /**
         * Sets total difficulty.
         *
         * @param totalDifficulty the total difficulty
         */
        public void setTotalDifficulty(String totalDifficulty) {
            this.totalDifficulty = totalDifficulty;
        }

        /**
         * Gets extra data.
         *
         * @return the extra data
         */
        public String getExtraData() {
            return extraData;
        }

        /**
         * Sets extra data.
         *
         * @param extraData the extra data
         */
        public void setExtraData(String extraData) {
            this.extraData = extraData;
        }

        /**
         * Gets size.
         *
         * @return the size
         */
        public BigInteger getSize() {
            return size != null ? Numeric.decodeQuantity(size) : BigInteger.ZERO;
        }

        /**
         * Gets size raw.
         *
         * @return the size raw
         */
        public String getSizeRaw() {
            return size;
        }

        /**
         * Sets size.
         *
         * @param size the size
         */
        public void setSize(String size) {
            this.size = size;
        }

        /**
         * Gets gas limit.
         *
         * @return the gas limit
         */
        public BigInteger getGasLimit() {
            return Numeric.decodeQuantity(gasLimit);
        }

        /**
         * Gets gas limit raw.
         *
         * @return the gas limit raw
         */
        public String getGasLimitRaw() {
            return gasLimit;
        }

        /**
         * Sets gas limit.
         *
         * @param gasLimit the gas limit
         */
        public void setGasLimit(String gasLimit) {
            this.gasLimit = gasLimit;
        }

        /**
         * Gets gas used.
         *
         * @return the gas used
         */
        public BigInteger getGasUsed() {
            return Numeric.decodeQuantity(gasUsed);
        }

        /**
         * Gets gas used raw.
         *
         * @return the gas used raw
         */
        public String getGasUsedRaw() {
            return gasUsed;
        }

        /**
         * Sets gas used.
         *
         * @param gasUsed the gas used
         */
        public void setGasUsed(String gasUsed) {
            this.gasUsed = gasUsed;
        }

        /**
         * Gets timestamp.
         *
         * @return the timestamp
         */
        public BigInteger getTimestamp() {
            return Numeric.decodeQuantity(timestamp);
        }

        /**
         * Gets timestamp raw.
         *
         * @return the timestamp raw
         */
        public String getTimestampRaw() {
            return timestamp;
        }

        /**
         * Sets timestamp.
         *
         * @param timestamp the timestamp
         */
        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }

        /**
         * Gets transactions.
         *
         * @return the transactions
         */
        public List<TransactionResult> getTransactions() {
            return transactions;
        }

        /**
         * Sets transactions.
         *
         * @param transactions the transactions
         */
        @JsonDeserialize(using = ResultTransactionDeserialiser.class)
        public void setTransactions(List<TransactionResult> transactions) {
            this.transactions = transactions;
        }

        /**
         * Gets uncles.
         *
         * @return the uncles
         */
        public List<String> getUncles() {
            return uncles;
        }

        /**
         * Sets uncles.
         *
         * @param uncles the uncles
         */
        public void setUncles(List<String> uncles) {
            this.uncles = uncles;
        }

        /**
         * Gets seal fields.
         *
         * @return the seal fields
         */
        public List<String> getSealFields() {
            return sealFields;
        }

        /**
         * Sets seal fields.
         *
         * @param sealFields the seal fields
         */
        public void setSealFields(List<String> sealFields) {
            this.sealFields = sealFields;
        }

        /**
         * Gets base fee per gas.
         *
         * @return the base fee per gas
         */
        public BigInteger getBaseFeePerGas() {
            return Numeric.decodeQuantity(baseFeePerGas);
        }

        /**
         * Sets base fee per gas.
         *
         * @param baseFeePerGas the base fee per gas
         */
        public void setBaseFeePerGas(String baseFeePerGas) {
            this.baseFeePerGas = baseFeePerGas;
        }

        /**
         * Gets base fee per gas raw.
         *
         * @return the base fee per gas raw
         */
        public String getBaseFeePerGasRaw() {
            return baseFeePerGas;
        }

        /**
         * Gets withdrawals root.
         *
         * @return the withdrawals root
         */
        public String getWithdrawalsRoot() {
            return withdrawalsRoot;
        }

        /**
         * Sets withdrawals root.
         *
         * @param withdrawalsRoot the withdrawals root
         */
        public void setWithdrawalsRoot(String withdrawalsRoot) {
            this.withdrawalsRoot = withdrawalsRoot;
        }

        /**
         * Gets withdrawals.
         *
         * @return the withdrawals
         */
        public List<EthBlock.Withdrawal> getWithdrawals() {
            return withdrawals;
        }

        /**
         * Sets withdrawals.
         *
         * @param withdrawals the withdrawals
         */
        public void setWithdrawals(List<EthBlock.Withdrawal> withdrawals) {
            this.withdrawals = withdrawals;
        }

        /**
         * Gets blob gas used.
         *
         * @return the blob gas used
         */
        public BigInteger getBlobGasUsed() {
            if (blobGasUsed == null) return BigInteger.ZERO;
            return Numeric.decodeQuantity(blobGasUsed);
        }

        /**
         * Gets blob gas used raw.
         *
         * @return the blob gas used raw
         */
        public String getBlobGasUsedRaw() {
            if (blobGasUsed == null) return "0";
            return blobGasUsed;
        }

        /**
         * Sets blob gas used.
         *
         * @param blobGasUsed the blob gas used
         */
        public void setBlobGasUsed(String blobGasUsed) {
            this.blobGasUsed = blobGasUsed;
        }

        /**
         * Gets excess blob gas.
         *
         * @return the excess blob gas
         */
        public BigInteger getExcessBlobGas() {
            if (excessBlobGas == null) return BigInteger.ZERO;
            return Numeric.decodeQuantity(excessBlobGas);
        }

        /**
         * Gets excess blob gas raw.
         *
         * @return the excess blob gas raw
         */
        public String getExcessBlobGasRaw() {
            if (excessBlobGas == null) return "0";
            return excessBlobGas;
        }

        /**
         * Sets excess blob gas.
         *
         * @param excessBlobGas the excess blob gas
         */
        public void setExcessBlobGas(String excessBlobGas) {
            this.excessBlobGas = excessBlobGas;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Block)) {
                return false;
            }

            Block block = (Block) o;

            if (getNumberRaw() != null ? !getNumberRaw().equals(block.getNumberRaw()) : block.getNumberRaw() != null) {
                return false;
            }
            if (getHash() != null ? !getHash().equals(block.getHash()) : block.getHash() != null) {
                return false;
            }
            if (getParentHash() != null
                    ? !getParentHash().equals(block.getParentHash())
                    : block.getParentHash() != null) {
                return false;
            }
            if (getParentBeaconBlockRoot() != null
                    ? !getParentBeaconBlockRoot().equals(block.getParentBeaconBlockRoot())
                    : block.getParentBeaconBlockRoot() != null) {
                return false;
            }
            if (getNonceRaw() != null ? !getNonceRaw().equals(block.getNonceRaw()) : block.getNonceRaw() != null) {
                return false;
            }
            if (getSha3Uncles() != null
                    ? !getSha3Uncles().equals(block.getSha3Uncles())
                    : block.getSha3Uncles() != null) {
                return false;
            }
            if (getLogsBloom() != null ? !getLogsBloom().equals(block.getLogsBloom()) : block.getLogsBloom() != null) {
                return false;
            }
            if (getTransactionsRoot() != null
                    ? !getTransactionsRoot().equals(block.getTransactionsRoot())
                    : block.getTransactionsRoot() != null) {
                return false;
            }
            if (getStateRoot() != null ? !getStateRoot().equals(block.getStateRoot()) : block.getStateRoot() != null) {
                return false;
            }
            if (getReceiptsRoot() != null
                    ? !getReceiptsRoot().equals(block.getReceiptsRoot())
                    : block.getReceiptsRoot() != null) {
                return false;
            }
            if (getAuthor() != null ? !getAuthor().equals(block.getAuthor()) : block.getAuthor() != null) {
                return false;
            }
            if (getMiner() != null ? !getMiner().equals(block.getMiner()) : block.getMiner() != null) {
                return false;
            }
            if (getMixHash() != null ? !getMixHash().equals(block.getMixHash()) : block.getMixHash() != null) {
                return false;
            }
            if (getDifficultyRaw() != null
                    ? !getDifficultyRaw().equals(block.getDifficultyRaw())
                    : block.getDifficultyRaw() != null) {
                return false;
            }
            if (getTotalDifficultyRaw() != null
                    ? !getTotalDifficultyRaw().equals(block.getTotalDifficultyRaw())
                    : block.getTotalDifficultyRaw() != null) {
                return false;
            }
            if (getExtraData() != null ? !getExtraData().equals(block.getExtraData()) : block.getExtraData() != null) {
                return false;
            }
            if (getSizeRaw() != null ? !getSizeRaw().equals(block.getSizeRaw()) : block.getSizeRaw() != null) {
                return false;
            }
            if (getGasLimitRaw() != null
                    ? !getGasLimitRaw().equals(block.getGasLimitRaw())
                    : block.getGasLimitRaw() != null) {
                return false;
            }
            if (getGasUsedRaw() != null
                    ? !getGasUsedRaw().equals(block.getGasUsedRaw())
                    : block.getGasUsedRaw() != null) {
                return false;
            }
            if (getTimestampRaw() != null
                    ? !getTimestampRaw().equals(block.getTimestampRaw())
                    : block.getTimestampRaw() != null) {
                return false;
            }
            if (getTransactions() != null
                    ? !getTransactions().equals(block.getTransactions())
                    : block.getTransactions() != null) {
                return false;
            }
            if (getUncles() != null ? !getUncles().equals(block.getUncles()) : block.getUncles() != null) {
                return false;
            }

            if (getBaseFeePerGasRaw() != null
                    ? !getBaseFeePerGasRaw().equals(block.getBaseFeePerGasRaw())
                    : block.getBaseFeePerGasRaw() != null) {
                return false;
            }

            if (getSealFields() != null
                    ? !getSealFields().equals(block.getSealFields())
                    : block.getSealFields() != null) {
                return false;
            }

            if (getBlobGasUsedRaw() != null
                    ? !getBlobGasUsedRaw().equals(block.getBlobGasUsedRaw())
                    : block.getBlobGasUsedRaw() != null) {
                return false;
            }

            if (getExcessBlobGasRaw() != null
                    ? !getExcessBlobGasRaw().equals(block.getExcessBlobGasRaw())
                    : block.getExcessBlobGasRaw() != null) {
                return false;
            }

            if (getWithdrawalsRoot() != null
                    ? !getWithdrawalsRoot().equals(block.getWithdrawalsRoot())
                    : block.getWithdrawalsRoot() != null) {
                return false;
            }

            return getWithdrawals() != null
                    ? getWithdrawals().equals(block.getWithdrawals())
                    : block.getWithdrawals() == null;
        }

        @Override
        public int hashCode() {
            int result = getNumberRaw() != null ? getNumberRaw().hashCode() : 0;
            result = 31 * result + (getHash() != null ? getHash().hashCode() : 0);
            result = 31 * result + (getParentHash() != null ? getParentHash().hashCode() : 0);
            result = 31 * result
                    + (getParentBeaconBlockRoot() != null
                            ? getParentBeaconBlockRoot().hashCode()
                            : 0);
            result = 31 * result + (getNonceRaw() != null ? getNonceRaw().hashCode() : 0);
            result = 31 * result + (getSha3Uncles() != null ? getSha3Uncles().hashCode() : 0);
            result = 31 * result + (getLogsBloom() != null ? getLogsBloom().hashCode() : 0);
            result = 31 * result
                    + (getTransactionsRoot() != null ? getTransactionsRoot().hashCode() : 0);
            result = 31 * result + (getStateRoot() != null ? getStateRoot().hashCode() : 0);
            result =
                    31 * result + (getReceiptsRoot() != null ? getReceiptsRoot().hashCode() : 0);
            result = 31 * result + (getAuthor() != null ? getAuthor().hashCode() : 0);
            result = 31 * result + (getMiner() != null ? getMiner().hashCode() : 0);
            result = 31 * result + (getMixHash() != null ? getMixHash().hashCode() : 0);
            result = 31 * result
                    + (getDifficultyRaw() != null ? getDifficultyRaw().hashCode() : 0);
            result = 31 * result
                    + (getTotalDifficultyRaw() != null ? getTotalDifficultyRaw().hashCode() : 0);
            result = 31 * result + (getExtraData() != null ? getExtraData().hashCode() : 0);
            result = 31 * result + (getSizeRaw() != null ? getSizeRaw().hashCode() : 0);
            result = 31 * result + (getGasLimitRaw() != null ? getGasLimitRaw().hashCode() : 0);
            result = 31 * result + (getGasUsedRaw() != null ? getGasUsedRaw().hashCode() : 0);
            result =
                    31 * result + (getTimestampRaw() != null ? getTimestampRaw().hashCode() : 0);
            result =
                    31 * result + (getTransactions() != null ? getTransactions().hashCode() : 0);
            result = 31 * result + (getUncles() != null ? getUncles().hashCode() : 0);
            result = 31 * result + (getSealFields() != null ? getSealFields().hashCode() : 0);
            result = 31 * result
                    + (getBaseFeePerGasRaw() != null ? getBaseFeePerGasRaw().hashCode() : 0);
            result = 31 * result
                    + (getWithdrawalsRoot() != null ? getWithdrawalsRoot().hashCode() : 0);
            result = 31 * result + (getWithdrawals() != null ? getWithdrawals().hashCode() : 0);
            result = 31 * result
                    + (getBlobGasUsedRaw() != null ? getBlobGasUsedRaw().hashCode() : 0);
            result = 31 * result
                    + (getExcessBlobGasRaw() != null ? getExcessBlobGasRaw().hashCode() : 0);
            return result;
        }
    }

    /**
     * The interface Transaction result.
     *
     * @param <T> the type parameter
     */
    public interface TransactionResult<T> {
        /**
         * Get t.
         *
         * @return the t
         */
        T get();
    }

    /**
     * The type Transaction hash.
     */
    public static class TransactionHash implements TransactionResult<String> {
        private String value;

        /**
         * Instantiates a new Transaction hash.
         */
        public TransactionHash() {}

        /**
         * Instantiates a new Transaction hash.
         *
         * @param value the value
         */
        public TransactionHash(String value) {
            this.value = value;
        }

        @Override
        public String get() {
            return value;
        }

        /**
         * Sets value.
         *
         * @param value the value
         */
        public void setValue(String value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof TransactionHash)) {
                return false;
            }

            TransactionHash that = (TransactionHash) o;

            return value != null ? value.equals(that.value) : that.value == null;
        }

        @Override
        public int hashCode() {
            return value != null ? value.hashCode() : 0;
        }
    }

    /**
     * The type Transaction object.
     */
    public static class TransactionObject extends OpTransaction implements TransactionResult<OpTransaction> {
        /**
         * Instantiates a new Transaction object.
         */
        public TransactionObject() {}

        /**
         * Instantiates a new Transaction object.
         *
         * @param hash                  the hash
         * @param nonce                 the nonce
         * @param blockHash             the block hash
         * @param blockNumber           the block number
         * @param chainId               the chain id
         * @param transactionIndex      the transaction index
         * @param from                  the from
         * @param to                    the to
         * @param value                 the value
         * @param gasPrice              the gas price
         * @param gas                   the gas
         * @param input                 the input
         * @param creates               the creates
         * @param publicKey             the public key
         * @param raw                   the raw
         * @param r                     the r
         * @param s                     the s
         * @param v                     the v
         * @param yParity               the y parity
         * @param type                  the type
         * @param maxFeePerGas          the max fee per gas
         * @param maxPriorityFeePerGas  the max priority fee per gas
         * @param accessList            the access list
         * @param sourceHash            the source hash
         * @param mint                  the mint
         * @param depositReceiptVersion the deposit receipt version
         */
        public TransactionObject(
                String hash,
                String nonce,
                String blockHash,
                String blockNumber,
                String chainId,
                String transactionIndex,
                String from,
                String to,
                String value,
                String gasPrice,
                String gas,
                String input,
                String creates,
                String publicKey,
                String raw,
                String r,
                String s,
                long v,
                String yParity,
                String type,
                String maxFeePerGas,
                String maxPriorityFeePerGas,
                List<AccessListObject> accessList,
                String sourceHash,
                String mint,
                String depositReceiptVersion) {
            super(
                    hash,
                    nonce,
                    blockHash,
                    blockNumber,
                    chainId,
                    transactionIndex,
                    from,
                    to,
                    value,
                    gas,
                    gasPrice,
                    input,
                    creates,
                    publicKey,
                    raw,
                    r,
                    s,
                    v,
                    yParity,
                    type,
                    maxFeePerGas,
                    maxPriorityFeePerGas,
                    accessList,
                    sourceHash,
                    mint,
                    depositReceiptVersion);
        }

        /**
         * Instantiates a new Transaction object.
         *
         * @param hash                  the hash
         * @param nonce                 the nonce
         * @param blockHash             the block hash
         * @param blockNumber           the block number
         * @param chainId               the chain id
         * @param transactionIndex      the transaction index
         * @param from                  the from
         * @param to                    the to
         * @param value                 the value
         * @param gasPrice              the gas price
         * @param gas                   the gas
         * @param input                 the input
         * @param creates               the creates
         * @param publicKey             the public key
         * @param raw                   the raw
         * @param r                     the r
         * @param s                     the s
         * @param v                     the v
         * @param yParity               the y parity
         * @param type                  the type
         * @param maxFeePerGas          the max fee per gas
         * @param maxPriorityFeePerGas  the max priority fee per gas
         * @param accessList            the access list
         * @param maxFeePerBlobGas      the max fee per blob gas
         * @param blobVersionedHashes   the blob versioned hashes
         * @param sourceHash            the source hash
         * @param mint                  the mint
         * @param depositReceiptVersion the deposit receipt version
         */
        public TransactionObject(
                String hash,
                String nonce,
                String blockHash,
                String blockNumber,
                String chainId,
                String transactionIndex,
                String from,
                String to,
                String value,
                String gasPrice,
                String gas,
                String input,
                String creates,
                String publicKey,
                String raw,
                String r,
                String s,
                long v,
                String yParity,
                String type,
                String maxFeePerGas,
                String maxPriorityFeePerGas,
                List<AccessListObject> accessList,
                String maxFeePerBlobGas,
                List<String> blobVersionedHashes,
                String sourceHash,
                String mint,
                String depositReceiptVersion) {
            super(
                    hash,
                    nonce,
                    blockHash,
                    blockNumber,
                    chainId,
                    transactionIndex,
                    from,
                    to,
                    value,
                    gas,
                    gasPrice,
                    input,
                    creates,
                    publicKey,
                    raw,
                    r,
                    s,
                    v,
                    yParity,
                    type,
                    maxFeePerGas,
                    maxPriorityFeePerGas,
                    accessList,
                    maxFeePerBlobGas,
                    blobVersionedHashes,
                    sourceHash,
                    mint,
                    depositReceiptVersion);
        }

        /**
         * Convert this transaction object to a web3j transaction object.
         * Will ignore sourceHash, mint, and depositReceiptVersion fields.
         *
         * @return the web3j transaction instant
         */
        public EthBlock.TransactionObject toWeb3j() {
            return new EthBlock.TransactionObject(
                    getHash(),
                    getNonceRaw(),
                    getBlockHash(),
                    getBlockNumberRaw(),
                    getChainIdRaw(),
                    getTransactionIndexRaw(),
                    getFrom(),
                    getTo(),
                    getValueRaw(),
                    getGasPriceRaw(),
                    getGasRaw(),
                    getInput(),
                    getCreates(),
                    getPublicKey(),
                    getRaw(),
                    getR(),
                    getS(),
                    getV(),
                    getyParity(),
                    getType(),
                    getMaxFeePerGasRaw(),
                    getMaxPriorityFeePerGasRaw(),
                    getAccessList(),
                    getMaxFeePerBlobGasRaw(),
                    getBlobVersionedHashes());
        }

        @Override
        public OpTransaction get() {
            return this;
        }
    }

    /**
     * The type Result transaction deserialiser.
     */
    public static class ResultTransactionDeserialiser extends JsonDeserializer<List<TransactionResult>> {

        private ObjectReader objectReader = ObjectMapperFactory.getObjectReader();

        /**
         * Instantiates a new Result transaction deserialiser.
         */
        public ResultTransactionDeserialiser() {}

        @Override
        public List<TransactionResult> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException {

            List<TransactionResult> transactionResults = new ArrayList<>();
            JsonToken nextToken = jsonParser.nextToken();

            if (nextToken == JsonToken.START_OBJECT) {
                Iterator<TransactionObject> transactionObjectIterator =
                        objectReader.readValues(jsonParser, TransactionObject.class);
                while (transactionObjectIterator.hasNext()) {
                    transactionResults.add(transactionObjectIterator.next());
                }
            } else if (nextToken == JsonToken.VALUE_STRING) {
                jsonParser.getValueAsString();

                Iterator<TransactionHash> transactionHashIterator =
                        objectReader.readValues(jsonParser, TransactionHash.class);
                while (transactionHashIterator.hasNext()) {
                    transactionResults.add(transactionHashIterator.next());
                }
            }

            return transactionResults;
        }
    }

    /**
     * The type Response deserialiser.
     */
    public static class ResponseDeserialiser extends JsonDeserializer<Block> {

        private ObjectReader objectReader = ObjectMapperFactory.getObjectReader();

        /**
         * Instantiates a new Response deserialiser.
         */
        public ResponseDeserialiser() {}

        @Override
        public Block deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
                throws IOException {
            if (jsonParser.getCurrentToken() != JsonToken.VALUE_NULL) {
                return objectReader.readValue(jsonParser, Block.class);
            } else {
                return null; // null is wrapped by Optional in above getter
            }
        }
    }
}
