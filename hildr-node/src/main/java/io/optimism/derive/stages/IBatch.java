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

import java.math.BigInteger;

/**
 * Batch contains information to build one or multiple L2 blocks.
 * Batcher converts L2 blocks into Batch and writes encoded bytes to Channel.
 * Derivation pipeline decodes Batch from Channel, and converts to one or multiple payload attributes.
 *
 * @author zhouop0
 * @since 0.1.0
 */
public interface IBatch {

    /**
     * Gets batch type.
     *
     * @return the batch type
     */
    BatchType getBatchType();

    /**
     * Gets timestamp.
     *
     * @return the timestamp
     */
    BigInteger getTimestamp();
}
