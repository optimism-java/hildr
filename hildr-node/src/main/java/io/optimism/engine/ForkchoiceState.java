/*
 * Copyright 2023 281165273grape@gmail.com
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

package io.optimism.engine;

/**
 * the type ForkchoiceState.
 *
 * <p>Note: [ForkchoiceState.safe_block_hash] and [ForkchoiceState.finalized_block_hash]fields are
 * allowed to have 0x0000000000000000000000000000000000000000000000000000000000000000 value unless
 * transition block is finalized.
 *
 * @author zhouop0
 * @since 0.1.0
 */
public record ForkchoiceState(
    /** 32 byte block hash of the head of the canonical chain */
    String headBlockHash,
    /**
     * 32 byte "safe" block hash of the canonical chain under certain synchrony and honesty
     * assumptions This value MUST be either equal to or an ancestor of headBlockHash
     */
    String safeBlockHash,
    /** 32 byte block hash of the most recent finalized block */
    String finalizedBlockHash) {}
