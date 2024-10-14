package io.optimism.v2.derive.datasource.impl;

import com.google.common.cache.Cache;
import io.optimism.config.Config;
import io.optimism.exceptions.BlockNotIncludedException;
import io.optimism.rpc.response.OpEthBlock;
import io.optimism.types.ParseBlockException;
import io.optimism.types.enums.TxType;
import io.optimism.utilities.LruCacheProvider;
import io.optimism.utilities.web3j.Web3jUtil;
import io.optimism.v2.derive.datasource.L2ChainProvider;
import io.optimism.v2.derive.types.Epoch;
import io.optimism.v2.derive.types.L1BlockInfoTx;
import io.optimism.v2.derive.types.L2BlockRef;
import io.optimism.v2.derive.types.SystemConfig;
import java.math.BigInteger;
import org.apache.commons.collections4.CollectionUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.tuples.generated.Tuple2;

/**
 * the L2 chain data fetcher
 *
 * @author thinkAfCod
 * @since 0.4.6
 */
public class L2ChainFetcher implements L2ChainProvider {

    private final Web3j l2Client;

    private final Web3jService l2Service;

    private final Config.ChainConfig rollupConfig;

    private final Cache<BigInteger, OpEthBlock> blockByNumCache;

    private final Cache<BigInteger, L2BlockRef> l2InfoByNumCache;

    private final Cache<BigInteger, SystemConfig> sysConfigByNumCache;

    public L2ChainFetcher(Tuple2<Web3j, Web3jService> tuple, Config.ChainConfig rollupConfig) {
        this.l2Client = tuple.component1();
        this.l2Service = tuple.component2();
        this.rollupConfig = rollupConfig;
        this.blockByNumCache = LruCacheProvider.create();
        this.l2InfoByNumCache = LruCacheProvider.create();
        this.sysConfigByNumCache = LruCacheProvider.create();
    }

    @Override
    public L2BlockRef l2BlockInfoByNumber(BigInteger num) {
        L2BlockRef cachedL2Ref = this.l2InfoByNumCache.getIfPresent(num);
        if (cachedL2Ref != null) {
            return cachedL2Ref;
        }
        OpEthBlock block = this.blockByNum(num);
        BigInteger l1OriginNum;
        String l1OriginHash;
        BigInteger l1OriginTimestamp;
        BigInteger seqNum;
        if (rollupConfig.l2Genesis().number().equals(block.getBlock().getNumber())) {
            l1OriginNum = rollupConfig.l1StartEpoch().number();
            l1OriginHash = rollupConfig.l1StartEpoch().hash();
            l1OriginTimestamp = rollupConfig.l1StartEpoch().timestamp();
            seqNum = BigInteger.ZERO;
        } else {
            if (CollectionUtils.isEmpty(block.getBlock().getTransactions())) {
                throw new ParseBlockException("Missing L1 info deposit: " + num);
            }
            var tx = (OpEthBlock.TransactionObject)
                    block.getBlock().getTransactions().get(0);
            if (!TxType.OPTIMISM_DEPOSIT.is(tx.getType())) {
                throw new ParseBlockException("First tx Non Deposit");
            }
            final L1BlockInfoTx l1BlockInfoTx = L1BlockInfoTx.decodeFrom(tx.getInput());
            l1OriginNum = l1BlockInfoTx.number();
            l1OriginHash = l1BlockInfoTx.blockHash();
            l1OriginTimestamp = l1BlockInfoTx.timestamp();
            seqNum = l1BlockInfoTx.sequenceNumber();
        }
        final L2BlockRef ref = new L2BlockRef(
                block.getBlock().getHash(),
                block.getBlock().getNumber(),
                block.getBlock().getParentHash(),
                block.getBlock().getTimestamp(),
                new Epoch(l1OriginNum, l1OriginHash, l1OriginTimestamp, null),
                seqNum);
        this.l2InfoByNumCache.put(num, ref);
        return ref;
    }

    @Override
    public OpEthBlock blockByNum(BigInteger num) {
        OpEthBlock cachedBlock = this.blockByNumCache.getIfPresent(num);
        if (cachedBlock != null) {
            return cachedBlock;
        }

        OpEthBlock block = Web3jUtil.pollOpBlockByNum(this.l2Service, DefaultBlockParameter.valueOf(num), true);
        if (block == null || block.getBlock() == null) {
            throw new BlockNotIncludedException("Optimism block not found by number:" + num);
        }
        this.blockByNumCache.put(num, block);
        return null;
    }

    @Override
    public SystemConfig systemConfigByNumber(BigInteger num, Config.ChainConfig chainConfig) {
        SystemConfig cachedConfig = this.sysConfigByNumCache.getIfPresent(num);
        if (cachedConfig != null) {
            return cachedConfig;
        }
        OpEthBlock opBlock = this.blockByNum(num);
        var sysConfig = SystemConfig.fromOpBlock(opBlock.getBlock(), chainConfig);
        this.sysConfigByNumCache.put(num, sysConfig);
        return sysConfig;
    }
}
