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

/** ## types. */
public class Types {
  /** The default engine api authentication port. */
  public static final Integer DEFAULT_AUTH_PORT = 8551;

  /** The ID of the static payload. */
  public static final Integer STATIC_ID = 1;

  /** The json rpc version string. */
  public static final String JSONRPC_VERSION = "2.0";

  /** The new payload method string. */
  public static final String ENGINE_NEW_PAYLOAD_V1 = "engine_newPayloadV1";

  /** The get payload method string. */
  public static final String ENGINE_GET_PAYLOAD_V1 = "engine_getPayloadV1";

  /** The forkchoice updated method string. */
  public static final String ENGINE_FORKCHOICE_UPDATED_V1 = "engine_forkchoiceUpdatedV1";
}
