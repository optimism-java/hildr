package io.optimism.types;

import java.math.BigInteger;
import org.web3j.protocol.core.methods.response.EthBlock;

/**
 * Block id.
 *
 * @param hash Block hash
 * @param number Block number
 * @author thinkAfCod
 * @since 0.1.1
 */
public record BlockId(String hash, BigInteger number) {

    /**
     * Create BlockId from EthBlock.Block.
     *
     * @param block block data
     * @return BlockId object
     */
    public static BlockId from(EthBlock.Block block) {
        return new BlockId(block.getHash(), block.getNumber());
    }

    @Override
    public String toString() {
        return "BlockId{hash='%s', number=%s}".formatted(hash, number);
    }
}
