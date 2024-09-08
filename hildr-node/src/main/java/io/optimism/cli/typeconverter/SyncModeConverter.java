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
    public Config.SyncMode convert(String value) {
        return Config.SyncMode.from(value);
    }
}
