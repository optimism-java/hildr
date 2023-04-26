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

package io.optimism.config;

/**
 * The enum Sync mode.
 *
 * @author grapebaba
 * @since 0.1.0
 */
public enum SyncMode {
  /** Fast sync mode. */
  Fast,
  /** Challenge sync mode. */
  Challenge,
  /** Full sync mode. */
  Full;

  static SyncMode from(String value) {
    return switch (value) {
      case "fast" -> Fast;
      case "challenge" -> Challenge;
      case "full" -> Full;
      default -> throw new RuntimeException("invalid sync mode");
    };
  }
}
