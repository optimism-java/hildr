package io.optimism.utilities.derive.stages;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.web3j.rlp.RlpEncoder;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpString;
import org.web3j.rlp.RlpType;
import org.web3j.utils.Numeric;

/**
 * The type Batch.
 *
 * @param parentHash the parent hash
 * @param epochNum the epoch num
 * @param epochHash the epoch hash
 * @param timestamp the timestamp
 * @param transactions the transactions
 * @param l1InclusionBlock L1 inclusion block
 * @author grapebaba
 * @since 0.1.0
 */
public record Batch(
        String parentHash,
        BigInteger epochNum,
        String epochHash,
        BigInteger timestamp,
        List<String> transactions,
        BigInteger l1InclusionBlock) {

    /**
     * Encode batch.
     *
     * @return encoded bytes by the batch
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
     * @param l1InclusionBlock L1 inclusion block
     * @return the batch
     */
    public static Batch decode(RlpList rlp, BigInteger l1InclusionBlock) {
        String parentHash = ((RlpString) rlp.getValues().get(0)).asString();
        BigInteger epochNum = ((RlpString) rlp.getValues().get(1)).asPositiveBigInteger();
        String epochHash = ((RlpString) rlp.getValues().get(2)).asString();
        BigInteger timestamp = ((RlpString) rlp.getValues().get(3)).asPositiveBigInteger();
        List<String> transactions = ((RlpList) rlp.getValues().get(4))
                .getValues().stream()
                        .map(rlpString -> ((RlpString) rlpString).asString())
                        .collect(Collectors.toList());
        return new Batch(parentHash, epochNum, epochHash, timestamp, transactions, l1InclusionBlock);
    }

    /**
     * Has invalid transactions boolean.
     *
     * @return the boolean
     */
    public boolean hasInvalidTransactions() {
        return this.transactions.stream()
                .anyMatch(s -> StringUtils.isEmpty(s)
                        || (Numeric.containsHexPrefix("0x")
                                ? StringUtils.startsWithIgnoreCase(s, "0x7E")
                                : StringUtils.startsWithIgnoreCase(s, "7E")));
    }
}
