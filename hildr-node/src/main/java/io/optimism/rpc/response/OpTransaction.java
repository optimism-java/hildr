package io.optimism.rpc.response;

import java.math.BigInteger;
import java.util.List;
import org.web3j.crypto.TransactionUtils;
import org.web3j.protocol.core.methods.response.AccessListObject;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthTransaction;
import org.web3j.utils.Numeric;

/**
 * Transaction object used by both {@link EthTransaction} and {@link EthBlock}.
 */
public class OpTransaction {

    private String hash;

    private String nonce;

    private String blockHash;

    private String blockNumber;

    private String chainId;

    private String transactionIndex;

    private String from;

    private String to;

    private String value;

    private String gasPrice;

    private String gas;

    private String input;

    private String creates;

    private String publicKey;

    private String raw;

    private String r;

    private String s;

    private long v; // see https://github.com/web3j/web3j/issues/44

    private String yParity;

    private String type;

    private String maxFeePerGas;

    private String maxPriorityFeePerGas;

    private List<AccessListObject> accessList;

    private String maxFeePerBlobGas;

    private List<String> blobVersionedHashes;

    private String sourceHash;

    private String mint;

    private String depositReceiptVersion;

    /**
     * Instantiates a new Op transaction.
     */
    public OpTransaction() {}

    /**
     * Instantiates a new Op transaction.
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
     * @param gas                   the gas
     * @param gasPrice              the gas price
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
    public OpTransaction(
            String hash,
            String nonce,
            String blockHash,
            String blockNumber,
            String chainId,
            String transactionIndex,
            String from,
            String to,
            String value,
            String gas,
            String gasPrice,
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
            List accessList,
            String sourceHash,
            String mint,
            String depositReceiptVersion) {
        this.hash = hash;
        this.nonce = nonce;
        this.blockHash = blockHash;
        this.blockNumber = blockNumber;
        this.chainId = chainId;
        this.transactionIndex = transactionIndex;
        this.from = from;
        this.to = to;
        this.value = value;
        this.gasPrice = gasPrice;
        this.gas = gas;
        this.input = input;
        this.creates = creates;
        this.publicKey = publicKey;
        this.raw = raw;
        this.r = r;
        this.s = s;
        this.v = v;
        this.yParity = yParity;
        this.type = type;
        this.maxFeePerGas = maxFeePerGas;
        this.maxPriorityFeePerGas = maxPriorityFeePerGas;
        this.accessList = accessList;
        this.sourceHash = sourceHash;
        this.mint = mint;
        this.depositReceiptVersion = depositReceiptVersion;
    }

    /**
     * Instantiates a new Op transaction.
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
     * @param gas                   the gas
     * @param gasPrice              the gas price
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
     * @param versionedHashes       the versioned hashes
     * @param sourceHash            the source hash
     * @param mint                  the mint
     * @param depositReceiptVersion the deposit receipt version
     */
    public OpTransaction(
            String hash,
            String nonce,
            String blockHash,
            String blockNumber,
            String chainId,
            String transactionIndex,
            String from,
            String to,
            String value,
            String gas,
            String gasPrice,
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
            List accessList,
            String maxFeePerBlobGas,
            List versionedHashes,
            String sourceHash,
            String mint,
            String depositReceiptVersion) {
        this.hash = hash;
        this.nonce = nonce;
        this.blockHash = blockHash;
        this.blockNumber = blockNumber;
        this.chainId = chainId;
        this.transactionIndex = transactionIndex;
        this.from = from;
        this.to = to;
        this.value = value;
        this.gasPrice = gasPrice;
        this.gas = gas;
        this.input = input;
        this.creates = creates;
        this.publicKey = publicKey;
        this.raw = raw;
        this.r = r;
        this.s = s;
        this.v = v;
        this.yParity = yParity;
        this.type = type;
        this.maxFeePerGas = maxFeePerGas;
        this.maxPriorityFeePerGas = maxPriorityFeePerGas;
        this.accessList = accessList;
        this.maxFeePerBlobGas = maxFeePerBlobGas;
        this.blobVersionedHashes = versionedHashes;
        this.sourceHash = sourceHash;
        this.mint = mint;
        this.depositReceiptVersion = depositReceiptVersion;
    }

    /**
     * Sets chain id.
     *
     * @param chainId the chain id
     */
    public void setChainId(String chainId) {
        this.chainId = chainId;
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
     * Gets nonce.
     *
     * @return the nonce
     */
    public BigInteger getNonce() {
        return Numeric.decodeQuantity(nonce);
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
     * Gets nonce raw.
     *
     * @return the nonce raw
     */
    public String getNonceRaw() {
        return nonce;
    }

    /**
     * Gets block hash.
     *
     * @return the block hash
     */
    public String getBlockHash() {
        return blockHash;
    }

    /**
     * Sets block hash.
     *
     * @param blockHash the block hash
     */
    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    /**
     * Gets block number.
     *
     * @return the block number
     */
    public BigInteger getBlockNumber() {
        return Numeric.decodeQuantity(blockNumber);
    }

    /**
     * Sets block number.
     *
     * @param blockNumber the block number
     */
    public void setBlockNumber(String blockNumber) {
        this.blockNumber = blockNumber;
    }

    /**
     * Gets block number raw.
     *
     * @return the block number raw
     */
    public String getBlockNumberRaw() {
        return blockNumber;
    }

    /**
     * Gets transaction index.
     *
     * @return the transaction index
     */
    public BigInteger getTransactionIndex() {
        return Numeric.decodeQuantity(transactionIndex);
    }

    /**
     * Sets transaction index.
     *
     * @param transactionIndex the transaction index
     */
    public void setTransactionIndex(String transactionIndex) {
        this.transactionIndex = transactionIndex;
    }

    /**
     * Gets transaction index raw.
     *
     * @return the transaction index raw
     */
    public String getTransactionIndexRaw() {
        return transactionIndex;
    }

    /**
     * Gets from.
     *
     * @return the from
     */
    public String getFrom() {
        return from;
    }

    /**
     * Sets from.
     *
     * @param from the from
     */
    public void setFrom(String from) {
        this.from = from;
    }

    /**
     * Gets to.
     *
     * @return the to
     */
    public String getTo() {
        return to;
    }

    /**
     * Sets to.
     *
     * @param to the to
     */
    public void setTo(String to) {
        this.to = to;
    }

    /**
     * Gets value.
     *
     * @return the value
     */
    public BigInteger getValue() {
        return Numeric.decodeQuantity(value);
    }

    /**
     * Sets value.
     *
     * @param value the value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets value raw.
     *
     * @return the value raw
     */
    public String getValueRaw() {
        return value;
    }

    /**
     * Gets gas price.
     *
     * @return the gas price
     */
    public BigInteger getGasPrice() {
        return Numeric.decodeQuantity(gasPrice);
    }

    /**
     * Sets gas price.
     *
     * @param gasPrice the gas price
     */
    public void setGasPrice(String gasPrice) {
        this.gasPrice = gasPrice;
    }

    /**
     * Gets gas price raw.
     *
     * @return the gas price raw
     */
    public String getGasPriceRaw() {
        return gasPrice;
    }

    /**
     * Gets gas.
     *
     * @return the gas
     */
    public BigInteger getGas() {
        return Numeric.decodeQuantity(gas);
    }

    /**
     * Sets gas.
     *
     * @param gas the gas
     */
    public void setGas(String gas) {
        this.gas = gas;
    }

    /**
     * Gets gas raw.
     *
     * @return the gas raw
     */
    public String getGasRaw() {
        return gas;
    }

    /**
     * Gets input.
     *
     * @return the input
     */
    public String getInput() {
        return input;
    }

    /**
     * Sets input.
     *
     * @param input the input
     */
    public void setInput(String input) {
        this.input = input;
    }

    /**
     * Gets creates.
     *
     * @return the creates
     */
    public String getCreates() {
        return creates;
    }

    /**
     * Sets creates.
     *
     * @param creates the creates
     */
    public void setCreates(String creates) {
        this.creates = creates;
    }

    /**
     * Gets public key.
     *
     * @return the public key
     */
    public String getPublicKey() {
        return publicKey;
    }

    /**
     * Sets public key.
     *
     * @param publicKey the public key
     */
    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    /**
     * Gets raw.
     *
     * @return the raw
     */
    public String getRaw() {
        return raw;
    }

    /**
     * Sets raw.
     *
     * @param raw the raw
     */
    public void setRaw(String raw) {
        this.raw = raw;
    }

    /**
     * Gets r.
     *
     * @return the r
     */
    public String getR() {
        return r;
    }

    /**
     * Sets r.
     *
     * @param r the r
     */
    public void setR(String r) {
        this.r = r;
    }

    /**
     * Gets s.
     *
     * @return the s
     */
    public String getS() {
        return s;
    }

    /**
     * Sets s.
     *
     * @param s the s
     */
    public void setS(String s) {
        this.s = s;
    }

    /**
     * Gets v.
     *
     * @return the v
     */
    public long getV() {
        return v;
    }

    /**
     * Sets v.
     *
     * @param v the v
     */
    // Workaround until Geth & Parity return consistent values. At present
    // Parity returns a byte value, Geth returns a hex-encoded string
    // https://github.com/ethereum/go-ethereum/issues/3339
    public void setV(Object v) {
        if (v instanceof String) {
            // longValueExact() is not implemented on android 11 or later only on 12 so it was
            // replaced with longValue.
            this.v = Numeric.toBigInt((String) v).longValue();
        } else if (v instanceof Integer) {
            this.v = ((Integer) v).longValue();
        } else {
            this.v = (Long) v;
        }
    }

    /**
     * Gets parity.
     *
     * @return the parity
     */
    public String getyParity() {
        return yParity;
    }

    /**
     * Sets parity.
     *
     * @param yParity the y parity
     */
    public void setyParity(String yParity) {
        this.yParity = yParity;
    }

    /**
     * Gets chain id.
     *
     * @return the chain id
     */
    public Long getChainId() {
        if (chainId != null) {
            return Numeric.decodeQuantity(chainId).longValue();
        }

        return TransactionUtils.deriveChainId(v);
    }

    /**
     * Gets chain id raw.
     *
     * @return the chain id raw
     */
    public String getChainIdRaw() {
        return this.chainId;
    }

    /**
     * Gets type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets type.
     *
     * @param type the type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets max fee per gas.
     *
     * @return the max fee per gas
     */
    public BigInteger getMaxFeePerGas() {
        if (maxFeePerGas == null) return null;
        return Numeric.decodeQuantity(maxFeePerGas);
    }

    /**
     * Gets max fee per gas raw.
     *
     * @return the max fee per gas raw
     */
    public String getMaxFeePerGasRaw() {
        return maxFeePerGas;
    }

    /**
     * Sets max fee per gas.
     *
     * @param maxFeePerGas the max fee per gas
     */
    public void setMaxFeePerGas(String maxFeePerGas) {
        this.maxFeePerGas = maxFeePerGas;
    }

    /**
     * Gets max priority fee per gas raw.
     *
     * @return the max priority fee per gas raw
     */
    public String getMaxPriorityFeePerGasRaw() {
        return maxPriorityFeePerGas;
    }

    /**
     * Gets max priority fee per gas.
     *
     * @return the max priority fee per gas
     */
    public BigInteger getMaxPriorityFeePerGas() {
        return Numeric.decodeQuantity(maxPriorityFeePerGas);
    }

    /**
     * Sets max priority fee per gas.
     *
     * @param maxPriorityFeePerGas the max priority fee per gas
     */
    public void setMaxPriorityFeePerGas(String maxPriorityFeePerGas) {
        this.maxPriorityFeePerGas = maxPriorityFeePerGas;
    }

    /**
     * Gets access list.
     *
     * @return the access list
     */
    public List<AccessListObject> getAccessList() {
        return accessList;
    }

    /**
     * Sets access list.
     *
     * @param accessList the access list
     */
    public void setAccessList(List<AccessListObject> accessList) {
        this.accessList = accessList;
    }

    /**
     * Gets max fee per blob gas raw.
     *
     * @return the max fee per blob gas raw
     */
    public String getMaxFeePerBlobGasRaw() {
        return maxFeePerBlobGas;
    }

    /**
     * Gets max fee per blob gas.
     *
     * @return the max fee per blob gas
     */
    public BigInteger getMaxFeePerBlobGas() {
        return Numeric.decodeQuantity(maxFeePerBlobGas);
    }

    /**
     * Sets max fee per blob gas.
     *
     * @param maxFeePerBlobGas the max fee per blob gas
     */
    public void setMaxFeePerBlobGas(String maxFeePerBlobGas) {
        this.maxFeePerBlobGas = maxFeePerBlobGas;
    }

    /**
     * Gets blob versioned hashes.
     *
     * @return the blob versioned hashes
     */
    public List<String> getBlobVersionedHashes() {
        return blobVersionedHashes;
    }

    /**
     * Sets blob versioned hashes.
     *
     * @param blobVersionedHashes the blob versioned hashes
     */
    public void setBlobVersionedHashes(List<String> blobVersionedHashes) {
        this.blobVersionedHashes = blobVersionedHashes;
    }

    /**
     * Gets source hash.
     *
     * @return the source hash
     */
    public String getSourceHash() {
        return sourceHash;
    }

    /**
     * Sets source hash.
     *
     * @param sourceHash the source hash
     */
    public void setSourceHash(String sourceHash) {
        this.sourceHash = sourceHash;
    }

    /**
     * Gets mint.
     *
     * @return the mint
     */
    public String getMint() {
        return mint;
    }

    /**
     * Sets mint.
     *
     * @param mint the mint
     */
    public void setMint(String mint) {
        this.mint = mint;
    }

    /**
     * Gets deposit receipt version.
     *
     * @return the deposit receipt version
     */
    public String getDepositReceiptVersion() {
        return depositReceiptVersion;
    }

    /**
     * Sets deposit receipt version.
     *
     * @param depositReceiptVersion the deposit receipt version
     */
    public void setDepositReceiptVersion(String depositReceiptVersion) {
        this.depositReceiptVersion = depositReceiptVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OpTransaction)) {
            return false;
        }

        OpTransaction that = (OpTransaction) o;

        if (getV() != that.getV()) {
            return false;
        }
        if (getHash() != null ? !getHash().equals(that.getHash()) : that.getHash() != null) {
            return false;
        }
        if (getNonceRaw() != null ? !getNonceRaw().equals(that.getNonceRaw()) : that.getNonceRaw() != null) {
            return false;
        }
        if (getBlockHash() != null ? !getBlockHash().equals(that.getBlockHash()) : that.getBlockHash() != null) {
            return false;
        }

        if (getChainIdRaw() != null ? !getChainIdRaw().equals(that.getChainIdRaw()) : that.getChainIdRaw() != null) {
            return false;
        }

        if (getBlockNumberRaw() != null
                ? !getBlockNumberRaw().equals(that.getBlockNumberRaw())
                : that.getBlockNumberRaw() != null) {
            return false;
        }
        if (getTransactionIndexRaw() != null
                ? !getTransactionIndexRaw().equals(that.getTransactionIndexRaw())
                : that.getTransactionIndexRaw() != null) {
            return false;
        }
        if (getFrom() != null ? !getFrom().equals(that.getFrom()) : that.getFrom() != null) {
            return false;
        }
        if (getTo() != null ? !getTo().equals(that.getTo()) : that.getTo() != null) {
            return false;
        }
        if (getValueRaw() != null ? !getValueRaw().equals(that.getValueRaw()) : that.getValueRaw() != null) {
            return false;
        }
        if (getGasPriceRaw() != null
                ? !getGasPriceRaw().equals(that.getGasPriceRaw())
                : that.getGasPriceRaw() != null) {
            return false;
        }
        if (getGasRaw() != null ? !getGasRaw().equals(that.getGasRaw()) : that.getGasRaw() != null) {
            return false;
        }
        if (getInput() != null ? !getInput().equals(that.getInput()) : that.getInput() != null) {
            return false;
        }
        if (getCreates() != null ? !getCreates().equals(that.getCreates()) : that.getCreates() != null) {
            return false;
        }
        if (getPublicKey() != null ? !getPublicKey().equals(that.getPublicKey()) : that.getPublicKey() != null) {
            return false;
        }
        if (getRaw() != null ? !getRaw().equals(that.getRaw()) : that.getRaw() != null) {
            return false;
        }
        if (getR() != null ? !getR().equals(that.getR()) : that.getR() != null) {
            return false;
        }
        if (getyParity() != null ? !getyParity().equals(that.getyParity()) : that.getyParity() != null) {
            return false;
        }
        if (getType() != null ? !getType().equals(that.getType()) : that.getType() != null) {
            return false;
        }
        if (getMaxFeePerGasRaw() != null
                ? !getMaxFeePerGasRaw().equals(that.getMaxFeePerGasRaw())
                : that.getMaxFeePerGasRaw() != null) {
            return false;
        }
        if (getMaxPriorityFeePerGasRaw() != null
                ? !getMaxPriorityFeePerGasRaw().equals(that.getMaxPriorityFeePerGasRaw())
                : that.getMaxPriorityFeePerGasRaw() != null) {
            return false;
        }

        if (getMaxFeePerBlobGasRaw() != null
                ? !getMaxFeePerBlobGasRaw().equals(that.getMaxFeePerBlobGasRaw())
                : that.getMaxFeePerBlobGasRaw() != null) {
            return false;
        }
        if (getBlobVersionedHashes() != null
                ? !getBlobVersionedHashes().equals(that.getBlobVersionedHashes())
                : that.getBlobVersionedHashes() != null) {
            return false;
        }
        if (getAccessList() != null ? !getAccessList().equals(that.getAccessList()) : that.getAccessList() != null) {
            return false;
        }
        if (getSourceHash() != null ? !getSourceHash().equals(that.getSourceHash()) : that.getSourceHash() != null) {
            return false;
        }
        if (getMint() != null ? !getMint().equals(that.getMint()) : that.getMint() != null) {
            return false;
        }
        if (getDepositReceiptVersion() != null
                ? !getDepositReceiptVersion().equals(that.getDepositReceiptVersion())
                : that.getDepositReceiptVersion() != null) {
            return false;
        }
        return getS() != null ? getS().equals(that.getS()) : that.getS() == null;
    }

    @Override
    public int hashCode() {
        int result = getHash() != null ? getHash().hashCode() : 0;
        result = 31 * result + (getNonceRaw() != null ? getNonceRaw().hashCode() : 0);
        result = 31 * result + (getBlockHash() != null ? getBlockHash().hashCode() : 0);
        result =
                31 * result + (getBlockNumberRaw() != null ? getBlockNumberRaw().hashCode() : 0);
        result = 31 * result + (getChainIdRaw() != null ? getChainIdRaw().hashCode() : 0);
        result = 31 * result
                + (getTransactionIndexRaw() != null ? getTransactionIndexRaw().hashCode() : 0);
        result = 31 * result + (getFrom() != null ? getFrom().hashCode() : 0);
        result = 31 * result + (getTo() != null ? getTo().hashCode() : 0);
        result = 31 * result + (getValueRaw() != null ? getValueRaw().hashCode() : 0);
        result = 31 * result + (getGasPriceRaw() != null ? getGasPriceRaw().hashCode() : 0);
        result = 31 * result + (getGasRaw() != null ? getGasRaw().hashCode() : 0);
        result = 31 * result + (getInput() != null ? getInput().hashCode() : 0);
        result = 31 * result + (getCreates() != null ? getCreates().hashCode() : 0);
        result = 31 * result + (getPublicKey() != null ? getPublicKey().hashCode() : 0);
        result = 31 * result + (getRaw() != null ? getRaw().hashCode() : 0);
        result = 31 * result + (getR() != null ? getR().hashCode() : 0);
        result = 31 * result + (getS() != null ? getS().hashCode() : 0);
        result = 31 * result + BigInteger.valueOf(getV()).hashCode();
        result = 31 * result + (getyParity() != null ? getyParity().hashCode() : 0);
        result = 31 * result + (getType() != null ? getType().hashCode() : 0);
        result = 31 * result
                + (getMaxFeePerGasRaw() != null ? getMaxFeePerGasRaw().hashCode() : 0);
        result = 31 * result
                + (getMaxPriorityFeePerGasRaw() != null
                        ? getMaxPriorityFeePerGasRaw().hashCode()
                        : 0);
        result = 31 * result
                + (getMaxFeePerBlobGasRaw() != null ? getMaxFeePerBlobGasRaw().hashCode() : 0);
        result = 31 * result
                + (getBlobVersionedHashes() != null ? getBlobVersionedHashes().hashCode() : 0);
        result = 31 * result + (getSourceHash() != null ? getSourceHash().hashCode() : 0);
        result = 31 * result + (getMint() != null ? getMint().hashCode() : 0);
        result = 31 * result
                + (getDepositReceiptVersion() != null
                        ? getDepositReceiptVersion().hashCode()
                        : 0);
        result = 31 * result + (getAccessList() != null ? getAccessList().hashCode() : 0);
        return result;
    }
}
