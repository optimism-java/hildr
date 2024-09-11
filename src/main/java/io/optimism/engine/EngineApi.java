package io.optimism.engine;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.optimism.config.Config;
import io.optimism.rpc.Web3jProvider;
import io.optimism.types.ExecutionPayload;
import io.optimism.types.ExecutionPayload.PayloadAttributes;
import io.optimism.types.ForkChoiceUpdate.ForkchoiceState;
import java.io.IOException;
import java.math.BigInteger;
import java.security.Key;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthChainId;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Numeric;

/**
 * The type EngineApi.
 *
 * @author zhouxing
 * @since 0.1.0
 */
public class EngineApi implements Engine {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(EngineApi.class);

    /**
     * The forkchoice updated v2 method string.
     */
    public static final String ENGINE_FORKCHOICE_UPDATED_V2 = "engine_forkchoiceUpdatedV2";

    /**
     * The forkchoice updated v3 method string.
     */
    public static final String ENGINE_FORKCHOICE_UPDATED_V3 = "engine_forkchoiceUpdatedV3";

    /**
     * The new payload v2 method string.
     */
    public static final String ENGINE_NEW_PAYLOAD_V2 = "engine_newPayloadV2";

    /**
     * The new payload v3 method string .
     */
    public static final String ENGINE_NEW_PAYLOAD_V3 = "engine_newPayloadV3";

    /**
     * The get payload v2 method string.
     */
    public static final String ENGINE_GET_PAYLOAD_V2 = "engine_getPayloadV2";

    /**
     * The get payload v3 method string.
     */
    public static final String ENGINE_GET_PAYLOAD_V3 = "engine_getPayloadV3";

    /**
     * The default engine api authentication port.
     */
    public static final Integer DEFAULT_AUTH_PORT = 8851;

    /**
     * HttpService web3jService.
     */
    private final HttpService web3jService;

    private final Key key;

    private final Config config;

    /**
     * Creates an engine api from environment variables.
     *
     * @param config the hildr config
     * @return EngineApi. engine api
     */
    public EngineApi fromEnv(Config config) {
        String baseUrlParm = System.getenv("ENGINE_API_URL");
        if (StringUtils.isBlank(baseUrlParm)) {
            throw new RuntimeException(
                    """
                        ENGINE_API_URL environment variable not set.
                        Please set this to the base url of the engine api
                        """);
        }
        String secretKey = System.getenv("JWT_SECRET");
        if (StringUtils.isBlank(secretKey)) {
            throw new RuntimeException(
                    """
                        JWT_SECRET environment variable not set.
                        Please set this to the 256 bit hex-encoded secret key
                         used to authenticate with the engine api.
                        This should be the same as set in the `--auth.secret`
                         flag when executing go-ethereum.
                        """);
        }
        String baseUrlFormat = authUrlFromAddr(baseUrlParm, null);
        return new EngineApi(config, baseUrlFormat, secretKey);
    }

    /**
     * Creates a new [`EngineApi`] with a base url and secret.
     *
     * @param config    config
     * @param baseUrl   baseUrl
     * @param secretStr secret
     */
    public EngineApi(final Config config, final String baseUrl, final String secretStr) {
        this.config = config;
        this.key = Keys.hmacShaKeyFor(Numeric.hexStringToByteArray(secretStr));
        this.web3jService = (HttpService) Web3jProvider.create(baseUrl).component2();
    }

    /**
     * Constructs the base engine api url for the given address.
     *
     * @param addr     addr
     * @param portParm port
     * @return url string
     */
    public static String authUrlFromAddr(String addr, Integer portParm) {
        String stripped = addr.replace("http://", "").replace("https://", "");
        Integer port = portParm == null ? DEFAULT_AUTH_PORT : portParm;
        return String.format("http://%1$s:%2$s", stripped, port);
    }

    /**
     * Generate jws string.
     *
     * @param key the key
     * @return the string
     */
    protected static String generateJws(Key key) {
        Instant now = Instant.now();
        Date nowDate = Date.from(now);
        Date expirationDate = Date.from(now.plusSeconds(60));
        return Jwts.builder()
                .setIssuedAt(nowDate)
                .setExpiration(expirationDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Override
    public OpEthForkChoiceUpdate forkchoiceUpdated(ForkchoiceState forkchoiceState, PayloadAttributes payloadAttributes)
            throws IOException {
        var method = ENGINE_FORKCHOICE_UPDATED_V2;
        var ecotoneTime = this.config.chainConfig().ecotoneTime();
        if (payloadAttributes == null || payloadAttributes.timestamp().compareTo(ecotoneTime) >= 0) {
            method = ENGINE_FORKCHOICE_UPDATED_V3;
        }
        web3jService.addHeader("authorization", String.format("Bearer %1$s", generateJws(key)));
        Request<?, OpEthForkChoiceUpdate> r = new Request<>(
                method,
                Arrays.asList(forkchoiceState, payloadAttributes != null ? payloadAttributes.toReq() : null),
                web3jService,
                OpEthForkChoiceUpdate.class);
        return r.send();
    }

    @Override
    public OpEthPayloadStatus newPayload(ExecutionPayload executionPayload) throws IOException {
        var ecotoneTime = this.config.chainConfig().ecotoneTime();
        List<Object> params;
        String method;
        var payloadReq = executionPayload != null ? executionPayload.toReq() : null;
        if (executionPayload != null && executionPayload.timestamp().compareTo(ecotoneTime) >= 0) {
            method = ENGINE_NEW_PAYLOAD_V3;
            params = new ArrayList<>();
            Collections.addAll(params, payloadReq, Collections.EMPTY_LIST, executionPayload.parentBeaconBlockRoot());
        } else {
            method = ENGINE_NEW_PAYLOAD_V2;
            params = Collections.singletonList(payloadReq);
        }
        web3jService.addHeader("authorization", String.format("Bearer %1$s", generateJws(key)));
        Request<?, OpEthPayloadStatus> r = new Request<>(method, params, web3jService, OpEthPayloadStatus.class);
        return r.send();
    }

    @Override
    public OpEthExecutionPayload getPayload(BigInteger timestamp, BigInteger payloadId) throws IOException {
        var method = ENGINE_GET_PAYLOAD_V2;
        var ecotoneTime = this.config.chainConfig().ecotoneTime();
        if (timestamp == null || timestamp.compareTo(ecotoneTime) >= 0) {
            method = ENGINE_GET_PAYLOAD_V3;
        }
        web3jService.addHeader("authorization", String.format("Bearer %1$s", generateJws(key)));
        Request<?, OpEthExecutionPayload> r = new Request<>(
                method,
                Collections.singletonList(
                        payloadId != null ? Numeric.toHexStringWithPrefixZeroPadded(payloadId, 16) : null),
                web3jService,
                OpEthExecutionPayload.class);
        return r.send();
    }

    /**
     * Is available boolean.
     *
     * @return the boolean
     */
    public boolean isAvailable() {
        LOGGER.debug("Checking if EngineApi is available");
        web3jService.addHeader("authorization", String.format("Bearer %1$s", generateJws(key)));
        Request<?, EthChainId> r = new Request<>("eth_chainId", List.of(), web3jService, EthChainId.class);
        EthChainId chainId;
        try {
            chainId = r.send();
        } catch (IOException e) {
            LOGGER.error("EngineApi is not available", e);
            return false;
        }
        return chainId != null;
    }
}
