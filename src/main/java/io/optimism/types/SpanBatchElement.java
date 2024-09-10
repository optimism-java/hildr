package io.optimism.types;

import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

/**
 * The type SpanBatchElement.
 *
 * @author grapebaba
 * @since 0.2.4
 */
public class SpanBatchElement {
    private BigInteger epochNum;
    private BigInteger timestamp;
    private List<String> transactions;

    /**
     * Instantiates a new Span batch element.
     */
    public SpanBatchElement() {}

    /**
     * Instantiates a new Span batch element.
     *
     * @param epochNum     the epoch num
     * @param timestamp    the timestamp
     * @param transactions the transactions
     */
    public SpanBatchElement(BigInteger epochNum, BigInteger timestamp, List<String> transactions) {
        this.epochNum = epochNum;
        this.timestamp = timestamp;
        this.transactions = transactions;
    }

    /**
     * Singular batch to element span batch element.
     *
     * @param singularBatch the singular batch
     * @return the span batch element
     */
    public static SpanBatchElement singularBatchToElement(SingularBatch singularBatch) {
        return new SpanBatchElement(singularBatch.epochNum(), singularBatch.timestamp(), singularBatch.transactions());
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
     * Sets epoch num.
     *
     * @param epochNum the epoch num
     */
    public void setEpochNum(BigInteger epochNum) {
        this.epochNum = epochNum;
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
        if (!(o instanceof SpanBatchElement that)) return false;
        return Objects.equals(epochNum, that.epochNum)
                && Objects.equals(timestamp, that.timestamp)
                && Objects.equals(transactions, that.transactions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(epochNum, timestamp, transactions);
    }

    @Override
    public String toString() {
        return "SpanBatchElement[epochNum=%s, timestamp=%s, transactions=%s]"
                .formatted(epochNum, timestamp, transactions);
    }
}
