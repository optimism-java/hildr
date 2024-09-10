package io.optimism.l1;

import static org.junit.jupiter.api.Assertions.*;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import io.optimism.TestConstants;
import io.optimism.config.Config;
import io.optimism.types.BlobSidecar;
import io.optimism.types.SpecConfig;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
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
        config = TestConstants.createConfig();
        if (!TestConstants.isConfiguredApiKeyEnv) {
            return;
        }
        fetcher = new BeaconBlobFetcher(config.l1BeaconUrl(), config.l1BeaconArchiverUrl());
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
        BigInteger slotFromTime = fetcher.getSlotFromTime(BigInteger.valueOf(1708659300L));
        List<BlobSidecar> blobSidecards = fetcher.getBlobSidecards(slotFromTime.toString(), null);
        assertTrue(blobSidecards != null && !blobSidecards.isEmpty());
    }

    @Test
    void verifyBlobSidecars() throws IOException {
        URL url = Resources.getResource("verify_blob.txt");
        String blob = Resources.toString(url, Charsets.UTF_8);
        BlobSidecar blobSidecar = new BlobSidecar(
                "1",
                blob,
                null,
                "0x8fa54464cc0e8239eece0aaece71c0a77c3982458c90e23cf7d76047e4bc18ca53b9db993ea7c3f9c46e02a3e18c6f3f",
                "0x86d8f4e5064978a200d4b3f14433f9027b3e9fbcadf9c55cba620f188b2bb478716b0ca0fc3baa9127078696a3fc52c1",
                null);
        List<BlobSidecar> blobSidecars = List.of(blobSidecar);
        List<String> versionedHashes = List.of("0x0117c47bf5c7f09ff5bf881d102ed3896050fdd3eccda6f46d94698d77b20331");
        assertTrue(BeaconBlobFetcher.verifyBlobSidecars(blobSidecars, versionedHashes));
    }
}
