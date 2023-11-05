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

/**
 * TxCandidate is a transaction candidate that can be submitted to ask to construct a transaction
 * with gas price bounds.
 *
 * @param txData the transaction data to be used in the constructed tx.
 * @param address To is the recipient of the constructed tx. Nil means contract creation.
 * @param gasLimit the gas limit to be used in the constructed tx.
 * @author thinkAfCod
 * @since 0.1.1
 */
public record TxCandidate(byte[] txData, String address, long gasLimit) {}
