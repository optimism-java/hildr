package io.optimism.v2.derive.datasource;

import io.optimism.config.Config;
import io.optimism.rpc.response.OpEthBlock;
import io.optimism.types.L2BlockRef;
import io.optimism.types.SystemConfig;
import java.math.BigInteger;

public interface L2ChainProvider {

    L2BlockRef l2BlockInfoByNumber(BigInteger num);

    OpEthBlock blockByNum(BigInteger num);

    SystemConfig systemConfigByNumber(BigInteger num, Config.ChainConfig chainConfig);
}
