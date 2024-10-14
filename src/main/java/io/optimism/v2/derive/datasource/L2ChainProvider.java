package io.optimism.v2.derive.datasource;

import io.optimism.config.Config;
import io.optimism.rpc.response.OpEthBlock;
import io.optimism.v2.derive.types.L2BlockRef;
import io.optimism.v2.derive.types.SystemConfig;
import java.math.BigInteger;

/**
 * the l2 chain data provider.
 *
 * @author thinkAfCod
 * @since 0.4.6
 */
public interface L2ChainProvider {

    L2BlockRef l2BlockInfoByNumber(BigInteger num);

    OpEthBlock blockByNum(BigInteger num);

    SystemConfig systemConfigByNumber(BigInteger num, Config.ChainConfig chainConfig);
}
