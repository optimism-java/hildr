package io.optimism.spec.derive.datasource;

import io.optimism.types.BlockInfo;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigInteger;

public interface ChainProvider {

  EthBlock.Block headerByHash(String hash);

  BlockInfo blockInfoByNumber(BigInteger num);

  TransactionReceipt receiptsByHash(String hash);

  EthBlock.Block blockInfoNTxsByHash(String hash);

}
