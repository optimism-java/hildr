package io.optimism;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;
import io.optimism.config.Config;
import io.optimism.config.Config.CliConfig;
import org.junit.jupiter.api.Test;

/**
 * The type HildrTest.
 *
 * @author grapebaba
 * @since 0.1.0
 */
class HildrTest {

    /**
     * Hildr has greeting.
     *
     * @throws JsonProcessingException the json processing exception
     */
    @Test
    void appHasGreeting() throws JsonProcessingException {
        CliConfig cliConfig = new CliConfig(
                "test",
                "test",
                "test",
                "test",
                "test",
                "test",
                "test",
                null,
                null,
                null,
                null,
                null,
                Config.SyncMode.Full,
                false,
                false);
        TomlMapper mapper = new TomlMapper();
        String cliConfigStr = mapper.writerFor(CliConfig.class).writeValueAsString(cliConfig);

        CliConfig cliConfig1 = mapper.readerFor(CliConfig.class).readValue(cliConfigStr);
        assertEquals("test", cliConfig1.jwtSecret());
        Hildr classUnderTest = new Hildr();
        assertNotNull(classUnderTest.getGreeting(), "app should have a greeting");
    }
}
