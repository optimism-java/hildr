package io.optimism.l1;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.optimism.type.BlobSidecar;
import io.optimism.type.SpecConfig;
import io.optimism.utilities.rpc.HttpClientProvider;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;

/**
 * @author thinkAfCod
 * @since 0.1.1
 */
public class BeaconBlobFetcher {

    private static final String GENESIS_METHOD_FORMAT = "%s/eth/v1/beacon/genesis";

    private static final String SPEC_METHOD_FORMAT = "%s/eth/v1/config/spec";

    private static final String SIDECARS_METHOD_PREFIX_FORMAT = "%s/eth/v1/beacon/blob_sidecars";

    private final String genesisMethod;

    private final String specMethod;

    private final String sidecarsMethod;

    private final OkHttpClient httpClient;

    private final ObjectMapper mapper;

    /**
     * Beacon blob info fetcher constructor.
     *
     * @param beaconUrl L1 beacon client url
     */
    public BeaconBlobFetcher(String beaconUrl) {
        this.genesisMethod = GENESIS_METHOD_FORMAT.formatted(beaconUrl);
        this.specMethod = SPEC_METHOD_FORMAT.formatted(beaconUrl);
        this.sidecarsMethod = SIDECARS_METHOD_PREFIX_FORMAT.formatted(beaconUrl);
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
        Map<String, Map<String, String>> res =
                this.send(req, new TypeReference<>() {
                });
        return new BigInteger(res.get("data").get("genesis_time"));
    }

    /**
     * Get the spec config
     *
     * @return the spec config
     */
    public SpecConfig getSpecConfig() {
        var req = new Request.Builder().get().url(this.specMethod).build();
        return this.send(req, new TypeReference<>() {
        });
    }

    /**
     * Get the blob sidecars
     *
     * @param slot the block id
     * @return the list of blob sidecars
     */
    public List<BlobSidecar> getBlobSidecards(BigInteger slot) {
        var req = new Request.Builder().get()
                .url("%s/%d".formatted(this.sidecarsMethod, slot))
                .build();
        var res = this.send(req, new TypeReference<Map<String, List<BlobSidecar>>>() {
        });
        return res.get("data");
    }

    // todo
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

        }
        return null;
    }

}
