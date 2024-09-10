package io.optimism.types;

import java.math.BigInteger;
import java.util.Objects;
import org.web3j.protocol.core.methods.response.EthBlock;

/**
 * L2 block brief information.
 *
 * @param hash L1 block hash
 * @param number L1 block number
 * @param parentHash L1 block parent hash
 * @param timestamp L1 Block timestamp
 * @param l1origin L1 blockId information
 * @param sequenceNumber sequence number that distance to first block of epoch
 * @author thinkAfCod
 * @since 0.1.1
 */
public record L2BlockRef(
        String hash,
        BigInteger number,
        String parentHash,
        BigInteger timestamp,
        Epoch l1origin,
        BigInteger sequenceNumber) {

    public static final L2BlockRef EMPTY = new L2BlockRef(null, null, null, null, null, null);

    /**
     * L2BlockRef instance converts to BlockId instance.
     *
     * @return BlockId instance
     */
    public BlockId toId() {
        return new BlockId(hash, number);
    }

    /**
     * Create a L2BlockRef instance from EthBlock.Block and L1BlockInfo
     *
     * @param block block info
     * @param l1Info l1 block info
     * @return L2BlockRef instance
     */
    public static L2BlockRef fromBlockAndL1Info(EthBlock.Block block, L1BlockInfo l1Info) {
        return new L2BlockRef(
                block.getHash(),
                block.getNumber(),
                block.getParentHash(),
                block.getTimestamp(),
                l1Info.toEpoch(),
                l1Info.sequenceNumber());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof L2BlockRef that)) return false;
        return Objects.equals(hash, that.hash)
                && Objects.equals(number, that.number)
                && Objects.equals(parentHash, that.parentHash)
                && Objects.equals(timestamp, that.timestamp)
                && Objects.equals(l1origin, that.l1origin)
                && Objects.equals(sequenceNumber, that.sequenceNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash, number, parentHash, timestamp, l1origin, sequenceNumber);
    }

    @Override
    public String toString() {
        return "L2BlockRef{" + "hash='"
                + hash + '\'' + ", number="
                + number + ", parentHash='"
                + parentHash + '\'' + ", timestamp="
                + timestamp + ", l1origin="
                + l1origin + ", sequenceNumber="
                + sequenceNumber + '}';
    }
}
