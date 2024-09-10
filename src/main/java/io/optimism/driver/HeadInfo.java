package io.optimism.driver;

import io.optimism.types.AttributesDepositedCall;
import io.optimism.types.BlockInfo;
import io.optimism.types.Epoch;
import java.math.BigInteger;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlock.TransactionObject;

/**
 * The type HeadInfo.
 *
 * @param l2BlockInfo the L2 block info
 * @param l1Epoch the L1 epoch
 * @param sequenceNumber the sequence number
 * @author grapebaba
 * @since 0.1.0
 */
public record HeadInfo(BlockInfo l2BlockInfo, Epoch l1Epoch, BigInteger sequenceNumber) {

    /**
     * From head info.
     *
     * @param block the block
     * @return the head info
     */
    public static HeadInfo from(EthBlock.Block block) {
        BlockInfo blockInfo = BlockInfo.from(block);

        if (block.getTransactions().isEmpty()) {
            throw new L1AttributesDepositedTxNotFoundException();
        }
        String txCallData = ((TransactionObject) block.getTransactions().get(0)).getInput();

        AttributesDepositedCall call = AttributesDepositedCall.from(txCallData);
        return new HeadInfo(blockInfo, call.toEpoch(), call.sequenceNumber());
    }
}
