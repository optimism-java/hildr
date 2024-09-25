package io.optimism.spec.derive.datasource;

import io.optimism.config.Config;
import io.optimism.types.ExecutionPayloadEnvelop;
import io.optimism.types.L2BlockRef;
import io.optimism.types.SystemConfig;

import java.math.BigInteger;

public interface L2ChainProvider {

  L2BlockRef l2BlockInfoByNumber(BigInteger num);

  ExecutionPayloadEnvelop payloadByNumber(BigInteger num);

  SystemConfig systemConfigByNumber(BigInteger num, Config.ChainConfig chainConfig);

}
