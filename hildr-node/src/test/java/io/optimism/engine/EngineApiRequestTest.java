package io.optimism.engine;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import io.jsonwebtoken.security.Keys;
import io.optimism.utilities.rpc.Web3jProvider;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.tuweni.units.ethereum.Wei;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.utils.Numeric;

/**
 * @author thinkAfCod
 * @since 0.1.1
 */
public class EngineApiRequestTest {

    static final Key key = Keys.hmacShaKeyFor(
            Numeric.hexStringToByteArray("bf549f5188556ce0951048ef467ec93067bc4ea21acebe46ef675cd4e8e015ff"));
    ;

    @Test
    @DisplayName("SendRequest")
    void testSendReq() throws IOException {
        //    final String localServiceUrl = "http://localhost:8551";
        final String localServiceUrl = "http://127.0.0.1:9552";
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(Duration.ofMinutes(5))
                .callTimeout(Duration.ofMinutes(5))
                .connectTimeout(Duration.ofMinutes(5))
                .build();

        final var startIndex = 0;
        //    final var startIndex = 99149;
        //    final var startIndex = 13241;
        final var pattern = Pattern.compile("\\d+");
        var sorter = (Comparator<File>) (file1, file2) -> {
            Matcher no1Matcher = pattern.matcher(file1.getName());
            Matcher no2Matcher = pattern.matcher(file2.getName());
            no1Matcher.find();
            no2Matcher.find();
            String file1No = no1Matcher.group();
            String file2No = no2Matcher.group();
            return Integer.valueOf(file1No).compareTo(Integer.valueOf(file2No));
        };
        // 遍历"./requeset_raw"文件夹内的文件
        var requestFiles = Arrays.stream(Objects.requireNonNull(
                        //            new File("/Users/xiaqingchuan/WorkSpace/github/rollup/hildr/request_raw")
                        new File("/Users/xiaqingchuan/WorkSpace/github/rollup/hildr/request_rollop")
                                .listFiles(prefilterFile -> {
                                    if (!prefilterFile.getName().contains("Request")) {
                                        return false;
                                    }
                                    if (startIndex == 0) {
                                        return true;
                                    }
                                    Matcher no1Matcher = pattern.matcher(prefilterFile.getName());
                                    no1Matcher.find();
                                    String no = no1Matcher.group();
                                    return Integer.parseInt(no) >= startIndex;
                                })))
                .sorted(sorter)
                .toList();
        var mapper = new ObjectMapper();
        for (int i = 0; i < requestFiles.size(); i++) {
            final File file = requestFiles.get(i);
            // 读取文件内容
            Matcher no1Matcher = pattern.matcher(file.getName());
            no1Matcher.find();
            String unused = no1Matcher.group();
            String postBody = Files.readString(Paths.get(file.getPath()));
            List<String> split = Splitter.on("\r\n\r\n").splitToList(postBody);
            Map<String, Object> map = mapper.readValue(split.getLast(), new TypeReference<Map<String, Object>>() {});

            var params = (ArrayList) map.get("params");
            if (map.get("method").equals("engine_forkchoiceUpdatedV2") && params.getLast() != null) {
                var txs = (ArrayList) ((Map) params.getLast()).get("transactions");
                if (txs.size() != 1) {
                    System.out.println("has normal tx: " + unused);
                }
            }
            // 构造请求
            RequestBody requestBody = RequestBody.create(split.getLast(), MediaType.get("application/json"));
            final okhttp3.Request request = new okhttp3.Request.Builder()
                    .url(localServiceUrl)
                    .addHeader("authorization", String.format("Bearer %1$s", EngineApi.generateJws(key)))
                    .post(requestBody)
                    .build();
            // 发送请求
            try {
                Response execute = client.newCall(request).execute();
                if (!execute.isSuccessful()) {
                    throw new IllegalStateException(
                            String.format("request failed: \n file: %s \n body:\n%s", file.getName(), postBody));
                } else {
                    unused = execute.body().string();
                    System.out.println(unused);
                }
                execute.close();
            } catch (IllegalStateException e) {
                throw e;
            } catch (Exception e) {
                e.printStackTrace();
                throw new IllegalStateException(
                        String.format("request failed: \n file: %s \n body:\n%s", file.getName(), postBody));
            }
        }
    }

    @Test
    void testSendTransaction() throws Exception {
        Web3j client = Web3jProvider.createClient("http://192.168.50.109:9545");
        Credentials credentials =
                Credentials.create("ac0974bec39a17e36ba4a6b4d238ff944bacb478cbed5efcae784d7bf4f2ff80");
        TransactionManager txMgr = new RawTransactionManager(client, credentials);
        // BigInteger gasPrice, BigInteger gasLimit, String to, String data, BigInteger value
        EthSendTransaction unused = txMgr.sendEIP1559Transaction(
                1201101712L,
                BigInteger.valueOf(10000000L),
                BigInteger.valueOf(30000000L),
                BigInteger.valueOf(1000000L),
                "0xf5f0A5135486fF2715b1dfAead54eEaFfe6B8404",
                "test12345668",
                Wei.fromEth(1L).toBigInteger().divide(BigInteger.valueOf(100L)));
    }

    @Test
    @DisplayName("")
    void testUpdateV2WithNullAttributes() throws IOException {
        //    HttpService httpService = new HttpService("http://127.0.0.1:8551");
        HttpService httpService = new HttpService("http://127.0.0.1:8552");
        ForkChoiceUpdate.ForkchoiceState state = new ForkChoiceUpdate.ForkchoiceState(
                "0x102de6ffb001480cc9b8b548fd05c34cd4f46ae4aa91759393db90ea0409887d",
                "0x102de6ffb001480cc9b8b548fd05c34cd4f46ae4aa91759393db90ea0409887d",
                "0x102de6ffb001480cc9b8b548fd05c34cd4f46ae4aa91759393db90ea0409887d");
        // ForkChoiceUpdate.ForkchoiceState state = new
        // ForkChoiceUpdate.ForkchoiceState(
        // "0xe460dd641f493c0184f2544c9bcfe3b4dcfe69cfa8054f8aed291b0ddda0025e",
        // "0xe460dd641f493c0184f2544c9bcfe3b4dcfe69cfa8054f8aed291b0ddda0025e",
        // "0x102de6ffb001480cc9b8b548fd05c34cd4f46ae4aa91759393db90ea0409887d");
        updatePayload(httpService, Arrays.asList(state, null));
    }

    @Test
    @DisplayName("")
    void testGetPayloadV2() throws IOException {
        HttpService httpService = new HttpService("http://127.0.0.1:8551");
        // HttpService httpService = new HttpService("http://127.0.0.1:8552");
        ExecutionPayload payloadV2 = getPayloadV2(httpService, List.of("0x59a95f1a0e05a669"));
        // ExecutionPayload payloadV2 = getPayloadV2(httpService,
        // List.of("0x2935588c228f5ab5"));

        var payloadStatus =
                newPayloadV2(httpService, Collections.singletonList(payloadV2 != null ? payloadV2.toReq() : null));
        System.out.println(payloadStatus.getPayloadStatus().getStatus());
    }

    @Test
    @DisplayName("test forkchoiceUpdateV2")
    void testUpdateV2() throws IOException {
        HttpService httpService = new HttpService("http://127.0.0.1:8551");
        // HttpService httpService = new HttpService("http://127.0.0.1:8552");

        ForkChoiceUpdate.ForkchoiceState state = new ForkChoiceUpdate.ForkchoiceState(
                "0x102de6ffb001480cc9b8b548fd05c34cd4f46ae4aa91759393db90ea0409887d",
                "0x102de6ffb001480cc9b8b548fd05c34cd4f46ae4aa91759393db90ea0409887d",
                "0x102de6ffb001480cc9b8b548fd05c34cd4f46ae4aa91759393db90ea0409887d");

        ExecutionPayload.PayloadAttributes.PayloadAttributesReq req =
                new ExecutionPayload.PayloadAttributes.PayloadAttributesReq(
                        "0x64d6dbae",
                        "0x32e4469959675ceed3d1a9f43709a8055d689d712844f820aa34dbc9f49e286c",
                        "0x4200000000000000000000000000000000000011",
                        Arrays.asList(
                                "0x7ef90159a03f8cb0302fe9f6cb3df3ea8a99cb388fa68ea51948490cd5ecd1a2572ad66a6c94deaddeaddeaddeaddeaddeaddeaddeaddead00019442000000000000000000000000000000000000158080830f424080b90104015d8eb900000000000000000000000000000000000000000000000000000000003e1ff00000000000000000000000000000000000000000000000000000000064d6dbac000000000000000000000000000000000000000000000000000000000ba15b6748f520cf4ddaf34c8336e6e490632ea3cf1e5e93b0b2bc6e917557e31845371b00000000000000000000000000000000000000000000000000000000000000010000000000000000000000008f23bb38f531600e5d8fddaaec41f13fab46e98c00000000000000000000000000000000000000000000000000000000000000bc00000000000000000000000000000000000000000000000000000000000a6fe0"),
                        null,
                        true,
                        "0x1c9c380");
        OpEthForkChoiceUpdate unused = updatePayload(httpService, Arrays.asList(state, req));

        // var payloadId = send.getForkChoiceUpdate().payloadId();
        // ExecutionPayload executionPayload = getPayloadV2(httpService,
        // Collections.singletonList(
        // payloadId != null ? Numeric.toHexStringWithPrefixZeroPadded(payloadId, 16) :
        // null));
        //
        // var payloadStatus = newPayloadV2(httpService,
        // Collections.singletonList(executionPayload != null ? executionPayload.toReq()
        // : null));
        // System.out.println(ExecutionPayload.Status.VALID.equals(payloadStatus.getPayloadStatus().getStatus()));
    }

    public OpEthForkChoiceUpdate updatePayload(HttpService httpService, List<?> params) throws IOException {
        httpService.addHeader("authorization", String.format("Bearer %1$s", EngineApi.generateJws(key)));
        Request<?, OpEthForkChoiceUpdate> r =
                new Request<>("engine_forkchoiceUpdatedV2", params, httpService, OpEthForkChoiceUpdate.class);
        return r.send();
    }

    public ExecutionPayload getPayloadV2(HttpService httpService, List<?> params) throws IOException {
        httpService.addHeader("authorization", String.format("Bearer %1$s", EngineApi.generateJws(key)));
        Request<?, OpEthExecutionPayload> payloadRes =
                new Request<>("engine_getPayloadV2", params, httpService, OpEthExecutionPayload.class);
        return payloadRes.send().getExecutionPayload();
    }

    public OpEthPayloadStatus newPayloadV2(HttpService httpService, List<?> params) throws IOException {
        httpService.addHeader("authorization", String.format("Bearer %1$s", EngineApi.generateJws(key)));
        Request<?, OpEthPayloadStatus> payloadRes =
                new Request<>("engine_newPayloadV2", params, httpService, OpEthPayloadStatus.class);
        return payloadRes.send();
    }
}
