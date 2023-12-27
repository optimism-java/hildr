package io.optimism.utilities.derive.stages;

public enum BatchType {
    SINGULAR_BATCH_TYPE(0, "SingularBatchType"),
    SPAN_BATCH_TYPE(1, "SpanBatchType");
    private final int code;
    private final String name;

    BatchType(int code, String name) {
        this.code = code;
        this.name = name;
    }

    public int getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public static BatchType from(int code) {
        for (BatchType batchType : BatchType.values()) {
            if (batchType.getCode() == code) {
                return batchType;
            }
        }
        throw new IllegalArgumentException("Invalid BatchType code: " + code);
    }
}
