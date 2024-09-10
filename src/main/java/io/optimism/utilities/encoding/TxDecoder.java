package io.optimism.utilities.encoding;

import io.optimism.types.DepositTransaction;
import java.math.BigInteger;
import java.util.Arrays;
import org.web3j.rlp.RlpDecoder;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpString;
import org.web3j.utils.Numeric;

/**
 * Tx decoder.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public class TxDecoder {

    /**
     * Instantiates a new Tx decoder.
     */
    public TxDecoder() {}

    /**
     * Decode to deposit deposit transaction.
     *
     * @param hexTransaction the hex transaction
     * @return the deposit transaction
     */
    public static DepositTransaction decodeToDeposit(final String hexTransaction) {
        final byte[] transaction = Numeric.hexStringToByteArray(hexTransaction);
        if (transaction.length > 0 && transaction[0] != ((byte) 0x7E)) {
            throw new RuntimeException("tx is not type of deposit tx");
        }
        final byte[] encodedTx = Arrays.copyOfRange(transaction, 1, transaction.length);
        final RlpList rlpList = RlpDecoder.decode(encodedTx);
        var values = ((RlpList) rlpList.getValues().getFirst()).getValues();
        final String sourceHash = ((RlpString) values.getFirst()).asString();
        final String from = ((RlpString) values.get(1)).asString();
        final String to = ((RlpString) values.get(2)).asString();
        final BigInteger mint = ((RlpString) values.get(3)).asPositiveBigInteger();
        final BigInteger value = ((RlpString) values.get(4)).asPositiveBigInteger();
        final BigInteger gas = ((RlpString) values.get(5)).asPositiveBigInteger();
        final boolean isSystemTx =
                ((RlpString) values.get(6)).asPositiveBigInteger().compareTo(BigInteger.ONE) == 0;
        final String data = ((RlpString) values.getLast()).asString();
        return new DepositTransaction(sourceHash, from, to, mint, value, gas, isSystemTx, data);
    }
}
