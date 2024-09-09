package io.optimism.engine;

import io.optimism.engine.ForkChoiceUpdate.ForkChoiceUpdateRes;
import org.web3j.protocol.core.Response;

/** The type Op eth fork choice update. */
public class OpEthForkChoiceUpdate extends Response<ForkChoiceUpdateRes> {

    /** Instantiates a new Op eth fork choice update. */
    public OpEthForkChoiceUpdate() {}

    /**
     * Gets fork choice update.
     *
     * @return the fork choice update
     */
    public ForkChoiceUpdate getForkChoiceUpdate() {
        return getResult().toForkChoiceUpdate();
    }

    @Override
    public void setResult(ForkChoiceUpdateRes result) {
        super.setResult(result);
    }
}
