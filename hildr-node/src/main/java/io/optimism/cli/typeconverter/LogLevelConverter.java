package io.optimism.cli.typeconverter;

import ch.qos.logback.classic.Level;
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
        return Level.valueOf(value);
    }
}
