package io.optimism.v2.derive.types;

import io.optimism.types.Epoch;
import java.math.BigInteger;
import java.util.List;
import org.web3j.protocol.core.methods.response.EthBlock;

/**
 * The OpPayloadAttributes type.
 *
 * @param timestamp             64 bit value for the timestamp field of the new payload.
 * @param prevRandao            32 byte value for the prevRandao field of the new payload.
 * @param suggestedFeeRecipient 20 bytes suggested value for the feeRecipient field of the new
 *                              payload.
 * @param transactions          List of transactions to be included in the new payload.
 * @param withdrawals           List of withdrawals to be included in the new payload.
 * @param noTxPool              Boolean value indicating whether the payload should be built without including
 *                              transactions from the txpool.
 * @param gasLimit              64 bit value for the gasLimit field of the new payload.The gasLimit is optional
 *                              w.r.t. compatibility with L1, but required when used as rollup.This field overrides the gas
 *                              limit used during block-building.If not specified as rollup, a STATUS_INVALID is returned.
 * @param epoch                 The batch epoch number from derivation. This value is not expected by the engine
 *                              is skipped during serialization and deserialization.
 * @param l1InclusionBlock      The L1 block number when this batch was first fully derived. This value
 *                              is not expected by the engine and is skipped during serialization and deserialization.
 * @param seqNumber             The L2 sequence number of the block. This value is not expected by the engine
 *                              and is skipped during serialization and deserialization.
 * @param parentBeaconBlockRoot The parent beacon block root.
 *
 * @author zhouop0
 * @since 0.1.0
 */
public record OpPayloadAttributes(
        BigInteger timestamp,
        String prevRandao,
        String suggestedFeeRecipient,
        List<String> transactions,
        List<EthBlock.Withdrawal> withdrawals,
        boolean noTxPool,
        BigInteger gasLimit,
        Epoch epoch,
        BigInteger l1InclusionBlock,
        BigInteger seqNumber,
        String parentBeaconBlockRoot) {}
