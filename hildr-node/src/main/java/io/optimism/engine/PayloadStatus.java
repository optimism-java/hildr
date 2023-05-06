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
 * The type PayloadStatus.
 *
 * @param status          The status of the payload.
 * @param latestValidHash 32 Bytes - the hash of the most recent valid block in the branch defined by payload and its ancestors.
 * @param validationError A message providing additional details on the validation error if the payload is classified as INVALID or INVALID_BLOCK_HASH.
 * @author zhouop0
 * @since 0.1.0
 */
public record PayloadStatus(Status status, String latestValidHash, String validationError) {
}
