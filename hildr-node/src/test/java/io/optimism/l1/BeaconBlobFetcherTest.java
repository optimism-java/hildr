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

    private static String beaconUrl =
            "https://few-sleek-sound.ethereum-sepolia.quiknode.pro/8e8c3ae8c9ddf50628ad22d3d8cdaf36230d52e1";
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
