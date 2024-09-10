package io.optimism.l1;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import ethereum.ckzg4844.CKZG4844JNI;
import io.optimism.rpc.HttpClientProvider;
import io.optimism.rpc.response.BeaconApiResponse;
import io.optimism.types.BlobSidecar;
import io.optimism.types.SpecConfig;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;
import java.util.stream.Collectors;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.web3j.utils.Numeric;

/**
 * The class of BeaconBlobFetcher.
 *
 * @author thinkAfCod
 * @since 0.3.0
 */
public class BeaconBlobFetcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(BeaconBlobFetcher.class);

    private static final String GENESIS_METHOD_FORMAT = "%s/eth/v1/beacon/genesis";

    private static final String SPEC_METHOD_FORMAT = "%s/eth/v1/config/spec";

    private static final String SIDECARS_METHOD_PREFIX_FORMAT = "%s/eth/v1/beacon/blob_sidecars";

    static {
        CKZG4844JNI.loadNativeLibrary();
        CKZG4844JNI.loadTrustedSetupFromResource("/kzg-trusted-setups/mainnet.txt", BeaconBlobFetcher.class);
    }

    private final String genesisMethod;

    private final String specMethod;

    private final String sidecarsMethod;

    private final String archiverSidecarsMethod;

    private final OkHttpClient httpClient;

    private final ObjectMapper mapper;

    private BigInteger genesisTimestamp;

    private BigInteger secondsPerSlot;

    /**
     * Beacon blob info fetcher constructor.
     * @param beaconUrl L1 beacon client url
     */
    public BeaconBlobFetcher(String beaconUrl) {
        this(beaconUrl, null);
    }

    /**
     * Beacon blob info fetcher constructor.
     *
     * @param beaconUrl L1 beacon client url
     * @param beaconArchiverUrl L1 beacon archiver client url
     */
    public BeaconBlobFetcher(String beaconUrl, String beaconArchiverUrl) {
        if (beaconUrl.endsWith("/")) {
            beaconUrl = beaconUrl.replaceAll("/+$", "");
        }
        if (beaconArchiverUrl.endsWith("/")) {
            beaconArchiverUrl = beaconArchiverUrl.replaceAll("/+$", "");
        }
        this.genesisMethod = GENESIS_METHOD_FORMAT.formatted(beaconUrl);
        this.specMethod = SPEC_METHOD_FORMAT.formatted(beaconUrl);
        this.sidecarsMethod = SIDECARS_METHOD_PREFIX_FORMAT.formatted(beaconUrl);
        this.archiverSidecarsMethod = StringUtils.isEmpty(beaconArchiverUrl)
                ? null
                : SIDECARS_METHOD_PREFIX_FORMAT.formatted(beaconArchiverUrl);
        this.httpClient = HttpClientProvider.create();
        this.mapper = new ObjectMapper();
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Get the genesis timestamp
     *
     * @return the genesis timestamp
     */
    public BigInteger getGenesisTimestamp() {
        var req = new Request.Builder().get().url(this.genesisMethod).build();
        var res = this.send(req, new TypeReference<BeaconApiResponse<Map<String, String>>>() {});
        return new BigInteger(res.getData().get("genesis_time"));
    }

    /**
     * Get the spec config
     *
     * @return the spec config
     */
    public SpecConfig getSpecConfig() {
        var req = new Request.Builder().get().url(this.specMethod).build();
        var res = this.send(req, new TypeReference<BeaconApiResponse<SpecConfig>>() {});
        return res.getData();
    }

    /**
     * Get slot from time.
     * @param time the block time
     * @return the slot
     */
    public BigInteger getSlotFromTime(BigInteger time) {
        if (this.genesisTimestamp == null) {
            this.genesisTimestamp = this.getGenesisTimestamp();
            this.secondsPerSlot = this.getSpecConfig().getSecondsPerSlot();
        }
        return time.subtract(this.genesisTimestamp).divide(secondsPerSlot);
    }

    /**
     * Get the blob sidecars
     *
     * @param blockId the block id
     * @param indices the blob indices
     * @return the list of blob sidecars
     */
    public List<BlobSidecar> getBlobSidecards(String blockId, final List<BigInteger> indices) {
        var params = indices == null || indices.isEmpty()
                ? null
                : Map.of("indices", indices.stream().map(BigInteger::toString).collect(Collectors.joining(",")));
        var postfix = "%s%s".formatted(blockId, prepareQueryParams(params));
        var res = getBlobSidecars("%s/%s".formatted(this.sidecarsMethod, postfix));
        if (res != null && res.getData() != null && !res.getData().isEmpty()) {
            return res.getData();
        }
        if (this.archiverSidecarsMethod != null) {
            LOGGER.warn(
                    "blob sidecars may be pruned, try blob archiver sidecars method: blockId = {}, indices = {}",
                    blockId,
                    indices);
            var archiverRes = getBlobSidecars("%s/%s".formatted(this.archiverSidecarsMethod, postfix));
            if (archiverRes.getData() != null && !archiverRes.getData().isEmpty()) {
                return archiverRes.getData();
            }
        } else {
            LOGGER.info(
                    "blob archiver sidecars method is empty, skip retry: block Id = {}, indices = {}",
                    blockId,
                    indices);
        }

        return res.getData();
    }

    private BeaconApiResponse<List<BlobSidecar>> getBlobSidecars(String url) {
        var req = new Request.Builder().get().url(HttpUrl.parse(url)).build();
        return this.send(req, new TypeReference<BeaconApiResponse<List<BlobSidecar>>>() {});
    }

    static boolean verifyBlobSidecars(List<BlobSidecar> blobSidecars, List<String> versionedHashes) {
        if (blobSidecars == null || versionedHashes == null) {
            return false;
        }
        // check length
        if (blobSidecars.size() != versionedHashes.size()) {
            return false;
        }

        for (int i = 0; i < blobSidecars.size(); i++) {
            var blobSidecar = blobSidecars.get(i);
            var versionedHash = versionedHashes.get(i);
            if (!verifyBlobSidecar(blobSidecar, versionedHash)) {
                return false;
            }
        }

        return true;
    }

    static boolean verifyBlobSidecar(BlobSidecar blobSidecar, String versionedHash) {
        if (!blobSidecar.getVersionedHash().equals(versionedHash)) {
            return false;
        }
        return CKZG4844JNI.verifyBlobKzgProof(
                Numeric.hexStringToByteArray(blobSidecar.getBlob()),
                Numeric.hexStringToByteArray(blobSidecar.getKzgCommitment()),
                Numeric.hexStringToByteArray(blobSidecar.getKzgProof()));
    }

    private <T> T send(final Request req, final TypeReference<T> typeRef) {
        Call call = this.httpClient.newCall(req);
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            var task = scope.fork(() -> {
                Response execute = call.execute();
                return this.mapper.readValue(execute.body().byteStream(), typeRef);
            });
            scope.join();
            scope.throwIfFailed();
            return task.get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.warn("request beacon client failed", e);
        }
        return null;
    }

    private String prepareQueryParams(final Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }
        return "?"
                + params.entrySet().stream()
                        .map(e -> e.getKey() + "=" + e.getValue())
                        .collect(Collectors.joining("&"));
    }
}
