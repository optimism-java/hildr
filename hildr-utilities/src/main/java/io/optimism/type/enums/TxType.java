package io.optimism.type.enums;

import org.hyperledger.besu.datatypes.TransactionType;

public enum TxType {
    LEGACY(0x00, "0x0", TransactionType.FRONTIER),
    /** Access list transaction type. */
    ACCESS_LIST(0x01, "0x1", TransactionType.ACCESS_LIST),
    /** Eip1559 transaction type. */
    EIP1559(0x02, "0x2", TransactionType.EIP1559),
    /** Blob transaction type. */
    BLOB(0x03, "0x3", TransactionType.BLOB),
    /** Optimism Deposit transaction type. */
    OPTIMISM_DEPOSIT(0x7e, "0x7e", null);

    private final int typeValue;
    private final String typeHexString;
    private final TransactionType besuType;

    TxType(final int typeValue, final String typeHexString, final TransactionType txBesuType) {
        this.typeValue = typeValue;
        this.typeHexString = typeHexString;
        this.besuType = txBesuType;
    }

    public boolean is(final String type) {
        return this.typeHexString.equalsIgnoreCase(type);
    }

    public int getValue() {
        return this.typeValue;
    }

    public String getString() {
        return this.typeHexString;
    }

    public TransactionType getBesuType() {
        return besuType;
    }
}
