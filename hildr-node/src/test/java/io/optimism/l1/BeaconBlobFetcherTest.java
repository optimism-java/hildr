package io.optimism.l1;

import static org.junit.jupiter.api.Assertions.*;

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

    private static String beaconUrl = "https://beacon-nd-182-746-446.p2pify.com/ae65ad4cb1ef42d6a520ac0516776939";
    private static BeaconBlobFetcher fetcher;

    @BeforeAll
    static void setUp() {
        fetcher = new BeaconBlobFetcher(beaconUrl);
    }

    @Test
    void getSpecConfig() {
        SpecConfig specConfig = fetcher.getSpecConfig();
        System.out.println(specConfig);
    }

    @Test
    void getSlotFromTime() {
        BigInteger slotFromTime = fetcher.getSlotFromTime(BigInteger.valueOf(1708659300L));
        System.out.println(slotFromTime);
    }

    @Test
    void getBlobSidecards() {
        BigInteger slotFromTime = fetcher.getSlotFromTime(BigInteger.valueOf(1708659300L));
        List<BlobSidecar> blobSidecards = fetcher.getBlobSidecards(slotFromTime.toString(), null);
        System.out.println(blobSidecards);
    }
}
