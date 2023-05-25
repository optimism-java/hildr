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

package io.optimism;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import io.optimism.config.Config.CliConfig;
import org.junit.jupiter.api.Test;

/**
 * The type AppTest.
 *
 * @author grapebaba
 * @since 0.1.0
 */
class AppTest {

  /**
   * Hildr has greeting.
   *
   * @throws JsonProcessingException the json processing exception
   */
  @Test
  void appHasGreeting() throws JsonProcessingException {
    CliConfig cliConfig = new CliConfig("test", "test", "test", "test", null, null);
    TomlMapper mapper = new TomlMapper();
    String cliConfigStr = mapper.writerFor(CliConfig.class).writeValueAsString(cliConfig);

    CliConfig cliConfig1 = mapper.readerFor(CliConfig.class).readValue(cliConfigStr);
    assertEquals("test", cliConfig1.jwtSecret());
    Hildr classUnderTest = new Hildr();
    assertNotNull(classUnderTest.getGreeting(), "app should have a greeting");
  }
}
