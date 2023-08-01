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

package io.optimism.type;

import java.math.BigInteger;

/**
 * Genesis info.
 *
 * @param l1 The L1 block that the rollup starts *after* (no derived transactions)
 * @param l2 The L2 block the rollup starts from (no transactions, pre-configured state)
 * @param l2Time Timestamp of L2 block
 * @param systemConfig Initial system configuration values. The L2 genesis block may not include
 *     transactions, and thus cannot encode the config values, unlike later L2 blocks.
 * @author thinkAfCod
 * @since 0.1.1
 */
public record Genesis(BlockId l1, BlockId l2, BigInteger l2Time, SystemConfig systemConfig) {}
