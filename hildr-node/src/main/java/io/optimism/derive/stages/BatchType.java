/*
 * Copyright 2023 q315xia@163.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package io.optimism.derive.stages;

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
