package io.optimism.types;

import java.math.BigInteger;
import java.util.Objects;
import org.web3j.protocol.core.methods.response.EthBlock;

/**
 * L1 block brief information.
 *
 * @param hash L1 block hash
 * @param number L1 block number
 * @param parentHash L1 block parent hash
 * @param timestamp L1 Block timestamp
 * @author thinkAfCod
 * @since 0.1.1
 */
public record L1BlockRef(String hash, BigInteger number, String parentHash, BigInteger timestamp) {

    public static final L1BlockRef emptyBlock = new L1BlockRef(
            "0x0000000000000000000000000000000000000000000000000000000000000000",
            BigInteger.ZERO,
            "0x0000000000000000000000000000000000000000000000000000000000000000",
            BigInteger.ZERO);

    /**
     * L1BlockRef instance converts to BlockId instance.
     *
     * @return BlockId instance
     */
    public BlockId toId() {
        return new BlockId(hash, number);
    }

    /**
     * Create a L1BlockRef instance from EthBlock.Block instance.
     *
     * @param block EthBlock.Block instance.
     * @return a L1BlockRef instance
     */
    public static L1BlockRef from(EthBlock.Block block) {
        return new L1BlockRef(block.getHash(), block.getNumber(), block.getParentHash(), block.getTimestamp());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof L1BlockRef that)) return false;
        return Objects.equals(hash, that.hash)
                && Objects.equals(number, that.number)
                && Objects.equals(parentHash, that.parentHash)
                && Objects.equals(timestamp, that.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash, number, parentHash, timestamp);
    }

    @Override
    public String toString() {
        return "L1BlockRef{hash='%s', number=%s, parentHash='%s', timestamp=%s}"
                .formatted(hash, number, parentHash, timestamp);
    }
}
