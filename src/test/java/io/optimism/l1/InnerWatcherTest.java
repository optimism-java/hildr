package io.optimism.l1;

import static org.junit.jupiter.api.Assertions.assertTrue;

import io.optimism.TestConstants;
import io.optimism.config.Config;
import io.optimism.telemetry.TracerTaskWrapper;
import io.optimism.types.BlockUpdate;
import io.optimism.types.enums.Logging;
import java.math.BigInteger;
import org.jctools.queues.MessagePassingQueue;
import org.jctools.queues.MpscGrowableArrayQueue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Test case of InnerWatcher.
 *
 * @author thinkAfCod
 * @since 2023.05
 */
public class InnerWatcherTest {

    private static Config config;

    @BeforeAll
    static void setUp() {
        config = TestConstants.createConfig();
    }

    InnerWatcher createWatcher(BigInteger l2StartBlock, MessagePassingQueue<BlockUpdate> queue) {
        var watcherl2StartBlock = l2StartBlock;
        if (l2StartBlock == null) {
            watcherl2StartBlock = config.chainConfig().l2Genesis().number();
        }
        return new InnerWatcher(
                config, queue, config.chainConfig().l1StartEpoch().number(), watcherl2StartBlock);
    }

    @Test
    void testCreateInnerWatcher() {
        if (!TestConstants.isConfiguredApiKeyEnv) {
            return;
        }
        var queue = new MpscGrowableArrayQueue<BlockUpdate>(1024 * 4, 1024 * 64);
        var unused = this.createWatcher(null, queue);
        unused = this.createWatcher(config.chainConfig().l2Genesis().number().add(BigInteger.TEN), queue);
    }

    @Test
    void testTryIngestBlock() throws Exception {
        if (!TestConstants.isConfiguredApiKeyEnv) {
            return;
        }
        TracerTaskWrapper.setTracerSupplier(Logging.INSTANCE::getTracer);
        var queue = new MpscGrowableArrayQueue<BlockUpdate>(1024 * 4, 1024 * 64);
        var watcher = this.createWatcher(null, queue);
        watcher.startUp();
        watcher.tryIngestBlock();
        assertTrue(queue.size() > 0);
    }
}
