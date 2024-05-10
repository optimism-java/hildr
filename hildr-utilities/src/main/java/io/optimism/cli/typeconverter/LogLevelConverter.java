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

package io.optimism.cli.typeconverter;

import ch.qos.logback.classic.Level;
import org.apache.commons.lang3.StringUtils;
import picocli.CommandLine;

/**
 * the Log Level converter, used for picocli parse args.
 *
 * @author thinkAfCod
 * @since 0.2.0
 */
public class LogLevelConverter implements CommandLine.ITypeConverter<ch.qos.logback.classic.Level> {

    /** the LogLevelConverter constructor. */
    public LogLevelConverter() {}

    @Override
    public Level convert(String value) throws Exception {
        return StringUtils.isEmpty(value) ? Level.DEBUG : Level.valueOf(value);
    }
}
