package io.optimism.utilities.derive.stages;

import java.math.BigInteger;
import org.web3j.rlp.RlpEncoder;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpString;

public record SpanBatchLegacyTxData(BigInteger value, BigInteger gasPrice, String data) implements SpanBatchTxData {

    @Override
    public byte txType() {
        return 0x00;
    }

    public byte[] encode() {
        return RlpEncoder.encode(
                new RlpList(RlpString.create(value()), RlpString.create(gasPrice()), RlpString.create(data())));
    }

    public static SpanBatchLegacyTxData decode(RlpList rlp) {
        BigInteger value = ((RlpString) rlp.getValues().get(0)).asPositiveBigInteger();
        BigInteger gasPrice = ((RlpString) rlp.getValues().get(1)).asPositiveBigInteger();
        String data = ((RlpString) rlp.getValues().get(2)).asString();
        return new SpanBatchLegacyTxData(value, gasPrice, data);
    }
}
