package io.optimism.type;

import org.web3j.protocol.core.Response;

/**
 * RollupConfig Response.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public class RollupConfigRes extends Response<RollupConfigResult> {

    /** Constructor of RollupConfigRes. */
    public RollupConfigRes() {}

    /**
     * Returns RollupConfig.
     *
     * @return rollup config info
     */
    public RollupConfigResult getConfig() {
        return getResult();
    }

    @Override
    public void setResult(RollupConfigResult result) {
        super.setResult(result);
    }
}
