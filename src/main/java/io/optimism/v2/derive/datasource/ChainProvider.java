package io.optimism.v2.derive.datasource;

import io.optimism.v2.derive.types.BlockInfo;
import java.math.BigInteger;
import java.util.List;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

public interface ChainProvider {

    EthBlock.Block headerByHash(String hash);

    BlockInfo blockInfoByNumber(BigInteger num);

    List<TransactionReceipt> receiptsByHash(String hash);

    EthBlock.Block blockInfoNTxsByHash(String hash);
}
