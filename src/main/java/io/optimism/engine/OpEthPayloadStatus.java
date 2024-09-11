package io.optimism.engine;

import io.optimism.types.ExecutionPayload.PayloadStatus;
import org.web3j.protocol.core.Response;

/** The type OpEthPayloadStatus. */
public class OpEthPayloadStatus extends Response<PayloadStatus> {

    /** Instantiates a new Op eth payload status. */
    public OpEthPayloadStatus() {}

    /**
     * Gets payload status.
     *
     * @return the payload status
     */
    public PayloadStatus getPayloadStatus() {
        return getResult();
    }

    @Override
    public void setResult(PayloadStatus result) {
        super.setResult(result);
    }
}
