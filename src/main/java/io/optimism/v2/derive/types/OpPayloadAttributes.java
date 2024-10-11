package io.optimism.v2.derive.types;

import io.optimism.types.Epoch;
import java.math.BigInteger;
import java.util.List;
import org.web3j.protocol.core.methods.response.EthBlock;

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
