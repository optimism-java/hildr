package io.optimism.utilities.derive.stages;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.web3j.crypto.AccessListObject;
import org.web3j.crypto.transaction.type.TransactionType;
import org.web3j.rlp.RlpEncoder;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpString;
import org.web3j.rlp.RlpType;

public record SpanBatchDynamicFeeTxData(
        BigInteger value, BigInteger gasTipCap, BigInteger gasFeeCap, String data, List<AccessListObject> accessList)
        implements SpanBatchTxData {
    @Override
    public byte txType() {
        return TransactionType.EIP1559.getRlpType();
    }

    public byte[] encode() {
        List<RlpType> rlpList = new ArrayList<>();
        for (AccessListObject access : accessList) {
            rlpList.add(RlpString.create(access.getAddress()));
            rlpList.add(new RlpList(
                    access.getStorageKeys().stream().map(RlpString::create).collect(Collectors.toList())));
        }
        return RlpEncoder.encode(new RlpList(
                RlpString.create(value()),
                RlpString.create(gasTipCap()),
                RlpString.create(gasFeeCap()),
                RlpString.create(data()),
                new RlpList(rlpList)));
    }

    public static SpanBatchDynamicFeeTxData decode(RlpList rlp) {
        BigInteger value = ((RlpString) rlp.getValues().get(0)).asPositiveBigInteger();
        BigInteger gasTipCap = ((RlpString) rlp.getValues().get(1)).asPositiveBigInteger();
        BigInteger gasFeeCap = ((RlpString) rlp.getValues().get(2)).asPositiveBigInteger();
        String data = ((RlpString) rlp.getValues().get(2)).asString();
        List<AccessListObject> accessObjList = new ArrayList<>();
        ((RlpList) rlp.getValues().get(3)).getValues().forEach(rlpType -> {
            RlpList rlpList = (RlpList) rlpType;
            String address = ((RlpString) rlpList.getValues().get(0)).asString();
            List<String> storageKeys = ((RlpList) rlpList.getValues().get(1))
                    .getValues().stream()
                            .map(stKey -> ((RlpString) stKey).asString())
                            .collect(Collectors.toList());
            accessObjList.add(new AccessListObject(address, storageKeys));
        });
        return new SpanBatchDynamicFeeTxData(value, gasTipCap, gasFeeCap, data, accessObjList);
    }
}
