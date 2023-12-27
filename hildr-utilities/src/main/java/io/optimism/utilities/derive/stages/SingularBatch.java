package io.optimism.utilities.derive.stages;

import io.optimism.type.BlockId;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;
import org.web3j.rlp.RlpEncoder;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpString;
import org.web3j.rlp.RlpType;

public record SingularBatch(
        String parentHash, BigInteger epochNum, String epochHash, BigInteger timestamp, List<String> transactions)
        implements IBatch {

    public BlockId epoch() {
        return new BlockId(epochHash(), epochNum());
    }

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
    public int getBatchType() {
        return BatchType.SINGULAR_BATCH_TYPE.getCode();
    }

    @Override
    public BigInteger getTimestamp() {
        return timestamp();
    }

    public BigInteger getEpochNum() {
        return epochNum();
    }
}
