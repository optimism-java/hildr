package io.optimism.v2.derive.datasource.impl;

import io.optimism.config.Config;
import io.optimism.exceptions.ResetException;
import io.optimism.v2.derive.datasource.ChainProvider;
import io.optimism.v2.derive.exception.PipelineEofException;
import io.optimism.v2.derive.exception.PipelineProviderException;
import io.optimism.v2.derive.stages.L1RetrievalProvider;
import io.optimism.v2.derive.stages.OriginAdvancer;
import io.optimism.v2.derive.stages.OriginProvider;
import io.optimism.v2.derive.stages.ResettableStage;
import io.optimism.v2.derive.types.BlockInfo;
import io.optimism.v2.derive.types.SystemConfig;
import java.math.BigInteger;
import java.util.List;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

/**
 * the L1 chain data traversal.
 *
 * @author thinkAfCod
 * @since 0.4.6
 */
public class L1Traversal implements L1RetrievalProvider, OriginProvider, OriginAdvancer, ResettableStage {

    private final Config.ChainConfig rollupConfig;

    private ChainProvider dataSource;

    private BlockInfo block;

    private boolean done;

    private SystemConfig curSysConfig;

    public L1Traversal(Config.ChainConfig rollupConfig, ChainProvider provider) {
        this.rollupConfig = rollupConfig;
        this.dataSource = provider;
    }

    @Override
    public BlockInfo nextL1Block() {
        return this.block;
    }

    @Override
    public String batcherAddr() {
        return this.curSysConfig.batcherAddr();
    }

    @Override
    public void advanceOrigin() {
        if (this.block == null) {
            throw new PipelineEofException();
        }

        var block = this.block;
        var nextL1Origin = this.dataSource.blockInfoByNumber(block.number().add(BigInteger.ONE));
        if (nextL1Origin == null) {
            throw new PipelineProviderException();
        }
        if (block.hash().equals(nextL1Origin.hash())) {
            throw new ResetException("reorg detected");
        }

        List<TransactionReceipt> txReceipts = this.dataSource.receiptsByHash(nextL1Origin.hash());
        curSysConfig = curSysConfig.updateByReceipts(
                txReceipts,
                this.rollupConfig.systemConfigContract(),
                this.rollupConfig.isEcotone(nextL1Origin.timestamp()));

        this.block = nextL1Origin;
        this.done = false;

        this.rollupConfig.isHoloceneActivationBlock(nextL1Origin.timestamp());
    }

    @Override
    public BlockInfo origin() {
        return this.block;
    }

    @Override
    public void reset(BlockInfo base, SystemConfig config) {
        this.block = base;
        this.curSysConfig = config;
        // metrics record stage reset for l1 traversal
    }
}
