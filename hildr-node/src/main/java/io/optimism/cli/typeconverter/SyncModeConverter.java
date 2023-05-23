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

package io.optimism.cli.typeconverter;

import io.optimism.config.Config;
import picocli.CommandLine;

/**
 * enum SyncMode type converter, used for picocli parse args.
 *
 * @author thinkAfCod
 * @since 2023.05
 */
public class SyncModeConverter implements CommandLine.ITypeConverter<Config.SyncMode> {

  /** the SyncModeConverter constructor. */
  public SyncModeConverter() {}

  @Override
  public Config.SyncMode convert(String value) throws Exception {
    return Config.SyncMode.from(value);
  }
}
