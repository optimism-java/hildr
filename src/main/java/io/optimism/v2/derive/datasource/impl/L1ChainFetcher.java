package io.optimism.v2.derive.datasource.impl;

import com.google.common.cache.Cache;
import io.optimism.exceptions.BlockNotIncludedException;
import io.optimism.utilities.LruCacheProvider;
import io.optimism.utilities.web3j.Web3jUtil;
import io.optimism.v2.derive.datasource.ChainProvider;
import io.optimism.v2.derive.types.BlockInfo;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

/**
 * the l1 chain data fetcher.
 *
 * @author thinkAfCod
 * @since 0.4.6
 */
public class L1ChainFetcher implements ChainProvider {

    private final Web3j l1Client;

    private Cache<String, EthBlock> blockByHashCache;

    private Cache<BigInteger, BlockInfo> blockInfoByNumCache;

    private Cache<String, List<TransactionReceipt>> receiptsByHashCache;

    private final boolean devnet;

    public L1ChainFetcher(Web3j l1Client, boolean devnet) {
        this.l1Client = l1Client;
        this.blockByHashCache = LruCacheProvider.create();
        this.blockInfoByNumCache = LruCacheProvider.create();
        this.receiptsByHashCache = LruCacheProvider.create();
        this.devnet = devnet;
    }

    @Override
    public EthBlock.Block headerByHash(String hash) {
        EthBlock block = this.blockByHashCache.getIfPresent(hash);
        if (block != null) {
            return block.getBlock();
        }

        EthBlock ethBlock = Web3jUtil.pollBlockByHash(this.l1Client, hash, true);
        if (ethBlock == null || ethBlock.getBlock() == null) {
            throw new BlockNotIncludedException("Block not found by hash: " + hash);
        }
        cacheBlock(ethBlock);
        cacheBlockInfo(fromBlock(ethBlock));
        return null;
    }

    @Override
    public BlockInfo blockInfoByNumber(BigInteger num) {
        final BlockInfo cachedBlockInfo = this.blockInfoByNumCache.getIfPresent(num);
        if (cachedBlockInfo != null) {
            return cachedBlockInfo;
        }
        EthBlock ethBlock = Web3jUtil.pollBlockByNum(this.l1Client, DefaultBlockParameter.valueOf(num), true);
        cacheBlock(ethBlock);
        BlockInfo info = fromBlock(ethBlock);
        if (info == null) {
            throw new BlockNotIncludedException("Block Info not found by number: " + num);
        }
        cacheBlockInfo(info);
        return null;
    }

    @Override
    public List<TransactionReceipt> receiptsByHash(String hash) {
        List<TransactionReceipt> cachedReceipts = this.receiptsByHashCache.getIfPresent(hash);
        if (cachedReceipts != null) {
            return cachedReceipts;
        }
        EthBlock.Block block = this.headerByHash(hash);
        var receipts = Web3jUtil.getBlockReceipts(this.l1Client, DefaultBlockParameter.valueOf(block.getNumber()));
        if (receipts == null) {
            throw new BlockNotIncludedException("Block Receipts not found by hash:" + hash);
        }
        List<TransactionReceipt> inner;
        if (receipts.getBlockReceipts().isPresent()) {
            inner = receipts.getBlockReceipts().get();
        } else {
            inner = List.of();
        }
        this.receiptsByHashCache.put(hash, inner);
        return inner;
    }

    @Override
    public EthBlock.Block blockInfoNTxsByHash(String hash) {
        return this.headerByHash(hash);
    }

    public EthBlock.Block getSafe() throws ExecutionException, InterruptedException {
        var parameter = this.devnet ? DefaultBlockParameterName.LATEST : DefaultBlockParameterName.SAFE;
        return getHeaderByTag(this.l1Client, parameter);
    }

    public EthBlock.Block getFinalized() throws ExecutionException, InterruptedException {
        var parameter = this.devnet ? DefaultBlockParameterName.LATEST : DefaultBlockParameterName.FINALIZED;
        return getHeaderByTag(this.l1Client, parameter);
    }

    public EthBlock.Block getHead() throws ExecutionException, InterruptedException {
        return getHeaderByTag(this.l1Client, DefaultBlockParameterName.LATEST);
    }

    private void cacheBlock(EthBlock block) {
        if (block != null && block.getBlock() != null) {
            this.blockByHashCache.put(block.getBlock().getHash(), block);
        }
    }

    private void cacheBlockInfo(BlockInfo blockInfo) {
        if (blockInfo != null) {
            this.blockInfoByNumCache.put(blockInfo.number(), blockInfo);
        }
    }

    private BlockInfo fromBlock(EthBlock block) {
        if (block == null || block.getBlock() == null) {
            return null;
        }
        var inner = block.getBlock();
        return new BlockInfo(inner.getHash(), inner.getNumber(), inner.getParentHash(), inner.getTimestamp());
    }

    private EthBlock.Block getHeaderByTag(Web3j client, DefaultBlockParameterName tag)
            throws ExecutionException, InterruptedException {
        var blockWrapper = Web3jUtil.pollBlockByNum(client, tag, false);
        if (blockWrapper == null || blockWrapper.getBlock() == null) {
            throw new BlockNotIncludedException();
        }
        return blockWrapper.getBlock();
    }
}
