package io.optimism.types;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.web3j.rlp.RlpEncoder;
import org.web3j.rlp.RlpList;
import org.web3j.rlp.RlpString;
import org.web3j.rlp.RlpType;
import org.web3j.utils.Numeric;

/**
 * Class of DepositTransaction.
 * Only declared in Optimism.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public class DepositTransaction {

    /**
     * Uniquely identifies the source of the deposit
     */
    private String sourceHash;

    /**
     * Exposed through the types.Signer, not through TxData
     */
    private String from;

    /**
     * Means contract creation
     */
    private String to;

    /**
     * Minted on L2, locked on L1, null if no minting.
     */
    private BigInteger mint;

    /**
     * Transferred from L2 balance, executed after Mint (if any)
     */
    private BigInteger value;

    /**
     * Gas limit
     */
    private BigInteger gas;

    /**
     * Field indicating if this transaction is exempt from the L2 gas limit.
     */
    private boolean isSystemTransaction;

    /**
     * Normal Tx data
     */
    private String data;

    /**
     * Instantiates a new Deposit transaction.
     */
    public DepositTransaction() {}

    /**
     * Instantiates a new Deposit transaction.
     *
     * @param sourceHash          the source hash
     * @param from                the from
     * @param to                  the to
     * @param mint                the mint
     * @param value               the value
     * @param gas                 the gas
     * @param isSystemTransaction the is system transaction
     * @param data                the data
     */
    public DepositTransaction(
            String sourceHash,
            String from,
            String to,
            BigInteger mint,
            BigInteger value,
            BigInteger gas,
            boolean isSystemTransaction,
            String data) {
        this.sourceHash = sourceHash;
        this.from = from;
        this.to = to;
        this.mint = mint;
        this.value = value;
        this.gas = gas;
        this.isSystemTransaction = isSystemTransaction;
        this.data = data;
    }

    /**
     * Gets source hash.
     *
     * @return the source hash
     */
    public String getSourceHash() {
        return sourceHash;
    }

    /**
     * Gets from.
     *
     * @return the from
     */
    public String getFrom() {
        return from;
    }

    /**
     * Gets to.
     *
     * @return the to
     */
    public String getTo() {
        return to;
    }

    /**
     * Gets mint.
     *
     * @return the mint
     */
    public BigInteger getMint() {
        return mint;
    }

    /**
     * Gets value.
     *
     * @return the value
     */
    public BigInteger getValue() {
        return value;
    }

    /**
     * Gets gas.
     *
     * @return the gas
     */
    public BigInteger getGas() {
        return gas;
    }

    /**
     * Is system transaction boolean.
     *
     * @return the boolean
     */
    public boolean isSystemTransaction() {
        return isSystemTransaction;
    }

    /**
     * Gets data.
     *
     * @return the data
     */
    public String getData() {
        return data;
    }

    /**
     * As rlp values list.
     *
     * @return the list
     */
    public byte[] encode() {
        List<RlpType> result = new ArrayList<>();

        result.add(RlpString.create(Numeric.hexStringToByteArray(this.sourceHash)));
        result.add(RlpString.create(Numeric.hexStringToByteArray(this.from)));

        if (StringUtils.isNotEmpty(this.to)) {
            result.add(RlpString.create(Numeric.hexStringToByteArray(this.to)));
        } else {
            result.add(RlpString.create(""));
        }

        result.add(RlpString.create(this.mint));
        result.add(RlpString.create(this.value));
        result.add(RlpString.create(this.gas));
        result.add(RlpString.create(this.isSystemTransaction ? 1L : 0L));
        result.add(RlpString.create(Numeric.hexStringToByteArray(this.data)));

        RlpList rlpList = new RlpList(result);
        byte[] encoded = RlpEncoder.encode(rlpList);

        return ByteBuffer.allocate(encoded.length + 1)
                .put((byte) 0x7e)
                .put(encoded)
                .array();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DepositTransaction that)) return false;
        return isSystemTransaction == that.isSystemTransaction
                && Objects.equals(sourceHash, that.sourceHash)
                && Objects.equals(from, that.from)
                && Objects.equals(to, that.to)
                && Objects.equals(mint, that.mint)
                && Objects.equals(value, that.value)
                && Objects.equals(gas, that.gas)
                && Objects.equals(data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceHash, from, to, mint, value, gas, isSystemTransaction, data);
    }
}
