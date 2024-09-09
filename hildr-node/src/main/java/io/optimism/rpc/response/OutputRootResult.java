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

package io.optimism.rpc.response;

import io.optimism.type.L2BlockRef;

/**
 * Output root result.
 *
 * @param version the version
 * @param outputRoot the output root
 * @param blockRef the l2 block ref
 * @param withdrawalStorageRoot the withdrawal storage root
 * @param stateRoot the state root
 * @param syncStatus the l2 sync status info
 */
public record OutputRootResult(
        String version,
        String outputRoot,
        L2BlockRef blockRef,
        String withdrawalStorageRoot,
        String stateRoot,
        SyncStatusResult syncStatus) {}
