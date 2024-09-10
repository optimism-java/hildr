package io.optimism.types.enums;

/**
 * The enum Batch type.
 *
 * @author grapebaba
 * @since 0.2.4
 */
public enum BatchType {
    /**
     * Singular batch type batch type.
     */
    SINGULAR_BATCH_TYPE(0, "SingularBatchType"),
    /**
     * Span batch type batch type.
     */
    SPAN_BATCH_TYPE(1, "SpanBatchType");
    private final int code;
    private final String name;

    BatchType(int code, String name) {
        this.code = code;
        this.name = name;
    }

    /**
     * Gets code.
     *
     * @return the code
     */
    public int getCode() {
        return code;
    }

    /**
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * From batch type.
     *
     * @param code the code
     * @return the batch type
     */
    public static BatchType from(int code) {
        for (BatchType batchType : BatchType.values()) {
            if (batchType.getCode() == code) {
                return batchType;
            }
        }
        throw new IllegalArgumentException("Invalid BatchType code: %d".formatted(code));
    }
}
