package io.optimism.types;

import io.optimism.types.enums.BatchType;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.web3j.rlp.RlpEncoder;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpString;
import org.web3j.rlp.RlpType;
import org.web3j.utils.Numeric;

/**
 * The type SingularBatch.
 *
 * @author grapebaba
 * @since 0.2.4
 */
public class SingularBatch implements IBatch {
    private String parentHash;
    private BigInteger epochNum;
    private String epochHash;
    private BigInteger timestamp;
    private List<String> transactions;

    /**
     * Instantiates a new Singular batch.
     */
    public SingularBatch() {}

    /**
     * Instantiates a new Singular batch.
     *
     * @param parentHash   the parent hash
     * @param epochNum     the epoch num
     * @param epochHash    the epoch hash
     * @param timestamp    the timestamp
     * @param transactions the transactions
     */
    public SingularBatch(
            String parentHash, BigInteger epochNum, String epochHash, BigInteger timestamp, List<String> transactions) {
        this.parentHash = parentHash;
        this.epochNum = epochNum;
        this.epochHash = epochHash;
        this.timestamp = timestamp;
        this.transactions = transactions;
    }

    /**
     * Epoch block id.
     *
     * @return the block id
     */
    public BlockId epoch() {
        return new BlockId(epochHash(), epochNum());
    }

    /**
     * Encode byte [ ].
     *
     * @return the byte [ ]
     */
    public byte[] encode() {
        List<RlpType> collect = transactions().stream()
                .map(tx -> (RlpType) RlpString.create(tx))
                .collect(Collectors.toList());
        return RlpEncoder.encode(new RlpList(
                RlpString.create(parentHash()),
                RlpString.create(epochNum()),
                RlpString.create(epochHash()),
                RlpString.create(timestamp()),
                new RlpList(collect)));
    }

    /**
     * Decode batch.
     *
     * @param rlp the rlp
     * @return the batch
     */
    public static SingularBatch decode(RlpList rlp) {
        String parentHash = ((RlpString) rlp.getValues().get(0)).asString();
        BigInteger epochNum = ((RlpString) rlp.getValues().get(1)).asPositiveBigInteger();
        String epochHash = ((RlpString) rlp.getValues().get(2)).asString();
        BigInteger timestamp = ((RlpString) rlp.getValues().get(3)).asPositiveBigInteger();
        List<String> transactions = ((RlpList) rlp.getValues().get(4))
                .getValues().stream()
                        .map(rlpString -> ((RlpString) rlpString).asString())
                        .collect(Collectors.toList());
        return new SingularBatch(parentHash, epochNum, epochHash, timestamp, transactions);
    }

    @Override
    public BatchType getBatchType() {
        return BatchType.SINGULAR_BATCH_TYPE;
    }

    @Override
    public BigInteger getTimestamp() {
        return timestamp();
    }

    /**
     * Gets epoch num.
     *
     * @return the epoch num
     */
    public BigInteger getEpochNum() {
        return epochNum();
    }

    /**
     * To span batch element span batch element.
     *
     * @return the span batch element
     */
    public SpanBatchElement toSpanBatchElement() {
        return new SpanBatchElement(epochNum(), timestamp(), transactions());
    }

    /**
     * Parent hash string.
     *
     * @return the string
     */
    public String parentHash() {
        return parentHash;
    }

    /**
     * Epoch num big integer.
     *
     * @return the big integer
     */
    public BigInteger epochNum() {
        return epochNum;
    }

    /**
     * Epoch hash string.
     *
     * @return the string
     */
    public String epochHash() {
        return epochHash;
    }

    /**
     * Timestamp big integer.
     *
     * @return the big integer
     */
    public BigInteger timestamp() {
        return timestamp;
    }

    /**
     * Transactions list.
     *
     * @return the list
     */
    public List<String> transactions() {
        return transactions;
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
     * Sets epoch num.
     *
     * @param epochNum the epoch num
     */
    public void setEpochNum(BigInteger epochNum) {
        this.epochNum = epochNum;
    }

    /**
     * Sets epoch hash.
     *
     * @param epochHash the epoch hash
     */
    public void setEpochHash(String epochHash) {
        this.epochHash = epochHash;
    }

    /**
     * Sets timestamp.
     *
     * @param timestamp the timestamp
     */
    public void setTimestamp(BigInteger timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Sets transactions.
     *
     * @param transactions the transactions
     */
    public void setTransactions(List<String> transactions) {
        this.transactions = transactions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SingularBatch that)) return false;
        return Objects.equals(parentHash, that.parentHash)
                && Objects.equals(epochNum, that.epochNum)
                && Objects.equals(epochHash, that.epochHash)
                && Objects.equals(timestamp, that.timestamp)
                && Objects.equals(transactions, that.transactions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentHash, epochNum, epochHash, timestamp, transactions);
    }

    @Override
    public String toString() {
        return "SingularBatch[parentHash=%s, epochNum=%s, epochHash=%s, timestamp=%s, transactions=%s]"
                .formatted(parentHash, epochNum, epochHash, timestamp, transactions);
    }

    /**
     * Has invalid transactions boolean.
     *
     * @return the boolean
     */
    public boolean hasInvalidTransactions() {
        return this.transactions.stream()
                .anyMatch(s -> StringUtils.isEmpty(s)
                        || (Numeric.containsHexPrefix(s)
                                ? StringUtils.startsWithIgnoreCase(s, "0x7E")
                                : StringUtils.startsWithIgnoreCase(s, "7E")));
    }
}
