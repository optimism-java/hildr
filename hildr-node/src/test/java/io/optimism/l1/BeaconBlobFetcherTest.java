package io.optimism.l1;

import static org.junit.jupiter.api.Assertions.*;

import io.optimism.TestConstants;
import io.optimism.config.Config;
import io.optimism.type.BlobSidecar;
import io.optimism.type.SpecConfig;
import java.math.BigInteger;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author thinkAfCod
 * @since 0.1.1
 */
class BeaconBlobFetcherTest {

    private static BeaconBlobFetcher fetcher;

    private static Config config;

    @BeforeAll
    static void setUp() {
        if (!TestConstants.isConfiguredApiKeyEnv) {
            return;
        }
        config = TestConstants.createConfig();
        fetcher = new BeaconBlobFetcher(config.l1BeaconUrl());
    }

    @Test
    void getSpecConfig() {
        if (!TestConstants.isConfiguredApiKeyEnv) {
            return;
        }
        SpecConfig specConfig = fetcher.getSpecConfig();
        assertEquals(BigInteger.valueOf(12L), specConfig.getSecondsPerSlot());
    }

    @Test
    void getSlotFromTime() {
        if (!TestConstants.isConfiguredApiKeyEnv) {
            return;
        }
        BigInteger slotFromTime = fetcher.getSlotFromTime(BigInteger.valueOf(1708659300L));
        assertEquals(BigInteger.valueOf(4410475), slotFromTime);
    }

    @Test
    void getBlobSidecardsAlreadyPrune() {
        if (!TestConstants.isConfiguredApiKeyEnv) {
            return;
        }
        var blobFetcher = new BeaconBlobFetcher(config.l1BeaconUrl(), config.l1BeaconArchiverUrl());
        BigInteger slotFromTime = blobFetcher.getSlotFromTime(BigInteger.valueOf(1708659300L));
        List<BlobSidecar> blobSidecards = blobFetcher.getBlobSidecards(slotFromTime.toString(), null);
        assertTrue(blobSidecards != null && blobSidecards.size() > 0);
    }
}
