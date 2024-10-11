package io.optimism.v2.derive.datasource.impl;

import io.optimism.config.Config;
import io.optimism.rpc.response.OpEthBlock;
import io.optimism.types.L2BlockRef;
import io.optimism.types.SystemConfig;
import io.optimism.v2.derive.datasource.L2ChainProvider;
import java.math.BigInteger;

public class L2ChainFetcher implements L2ChainProvider {
    @Override
    public L2BlockRef l2BlockInfoByNumber(BigInteger num) {
        return null;
    }

    @Override
    public OpEthBlock blockByNum(BigInteger num) {
        return null;
    }

    @Override
    public SystemConfig systemConfigByNumber(BigInteger num, Config.ChainConfig chainConfig) {
        return null;
    }
}
