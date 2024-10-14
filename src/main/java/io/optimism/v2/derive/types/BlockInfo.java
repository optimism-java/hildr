package io.optimism.v2.derive.types;

import io.optimism.exceptions.BlockNotIncludedException;
import io.optimism.rpc.response.OpEthBlock;
import java.math.BigInteger;
import java.util.Objects;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.utils.Numeric;

/**
 * The type BlockInfo.
 *
 * <p>Selected block header info.
 *
 * @param number Block number.
 * @param hash Block hash.
 * @param timestamp Block timestamp.
 * @param parentHash Block parent hash.
 * @author grapebaba
 * @since 0.1.0
 */
public record BlockInfo(String hash, BigInteger number, String parentHash, BigInteger timestamp) {

    public static final BlockInfo EMPTY = new BlockInfo(
            Numeric.toHexString(new byte[32]), BigInteger.ZERO, Numeric.toHexString(new byte[32]), BigInteger.ZERO);

    /**
     * create block info From EthBlock.Block.
     *
     * @param block the op block
     * @return the block info
     */
    public static BlockInfo from(EthBlock.Block block) {
        if (block == null) {
            throw new BlockNotIncludedException();
        }
        return new BlockInfo(block.getHash(), block.getNumber(), block.getParentHash(), block.getTimestamp());
    }

    /**
     * create block info From OpEthBlock.Block.
     * @param block the op block
     * @return the block info
     */
    public static BlockInfo from(OpEthBlock.Block block) {
        if (block == null) {
            throw new BlockNotIncludedException();
        }
        return new BlockInfo(block.getHash(), block.getNumber(), block.getParentHash(), block.getTimestamp());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BlockInfo blockInfo)) {
            return false;
        }
        return Objects.equals(hash, blockInfo.hash)
                && Objects.equals(number, blockInfo.number)
                && Objects.equals(parentHash, blockInfo.parentHash)
                && Objects.equals(timestamp, blockInfo.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hash, number, parentHash, timestamp);
    }
}
