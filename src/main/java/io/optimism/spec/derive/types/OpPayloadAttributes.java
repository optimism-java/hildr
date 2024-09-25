package io.optimism.spec.derive.types;

import io.optimism.types.Epoch;
import org.web3j.protocol.core.methods.response.EthBlock;

import java.math.BigInteger;
import java.util.List;

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

