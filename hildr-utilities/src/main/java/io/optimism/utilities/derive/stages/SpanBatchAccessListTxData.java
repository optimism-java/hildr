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

/**
 * EIP-2930.
 *
 */
public record SpanBatchAccessListTxData(
        BigInteger value, BigInteger gasPrice, String data, List<AccessListObject> accessList)
        implements SpanBatchTxData {

    @Override
    public byte txType() {
        return TransactionType.EIP2930.getRlpType();
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
                RlpString.create(gasPrice()),
                RlpString.create(data()),
                new RlpList(rlpList)));
    }

    public static SpanBatchAccessListTxData decode(RlpList rlp) {
        BigInteger value = ((RlpString) rlp.getValues().get(0)).asPositiveBigInteger();
        BigInteger gasPrice = ((RlpString) rlp.getValues().get(1)).asPositiveBigInteger();
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
        return new SpanBatchAccessListTxData(value, gasPrice, data, accessObjList);
    }
}
