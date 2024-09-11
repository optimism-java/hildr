package io.optimism.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.ImmutableSet;
import io.libp2p.core.pubsub.ValidationResult;
import io.optimism.types.ExecutionPayload;
import io.optimism.types.enums.BlockVersion;
import io.optimism.types.enums.HildrNodeMetricsCategory;
import java.util.EnumSet;
import java.util.concurrent.ExecutionException;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.units.bigints.UInt64;
import org.hyperledger.besu.metrics.StandardMetricCategory;
import org.hyperledger.besu.metrics.prometheus.PrometheusMetricsSystem;
import org.hyperledger.besu.plugin.services.MetricsSystem;
import org.hyperledger.besu.plugin.services.metrics.MetricCategory;
import org.jctools.queues.MpscUnboundedXaddArrayQueue;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.web3j.utils.Numeric;
import tech.pegasys.teku.infrastructure.async.AsyncRunner;
import tech.pegasys.teku.infrastructure.async.AsyncRunnerFactory;
import tech.pegasys.teku.infrastructure.async.MetricTrackingExecutorFactory;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.networking.p2p.gossip.PreparedGossipMessage;

/**
 * The type AbstractTopicHandlerTest.
 * <p>
 * All tests are timestamp sensitive and are disabled by default.
 *
 * @author grapebaba
 * @since 0.2.6
 */
class AbstractTopicHandlerTest {

    @Test
    @Disabled
    void testV3RejectNonZeroExcessGas() throws ExecutionException, InterruptedException {
        String data =
                "0xf104f0436c000b5448b55b27c4a06a556d4eecd6e502995ffbcd738843263d7ed92489e66d3776f1c829e0b43c7d354840b1b8983752bd797278d246b7d4ec0e100c6ae9010000006a03000412346a1d00fe0100fe0100fe0100fe0100fe0100fe01004201000c6e75d465219504100201067601007c752580fe037c2d682ee9b17b7e7b477cda0569cf4422284a570e918d526d283f0144010411011c0100000000000000";

        final MetricsSystem metricsSystem = new PrometheusMetricsSystem(
                ImmutableSet.<MetricCategory>builder()
                        .addAll(EnumSet.allOf(StandardMetricCategory.class))
                        .addAll(EnumSet.allOf(HildrNodeMetricsCategory.class))
                        .build(),
                true);
        final AsyncRunner gossipAsyncRunner = AsyncRunnerFactory.createDefault(
                        new MetricTrackingExecutorFactory(metricsSystem))
                .create("hildr_node_gossip", 20);
        MpscUnboundedXaddArrayQueue<ExecutionPayload> unsafeBlockQueue = new MpscUnboundedXaddArrayQueue<>(1024 * 64);
        BlockV3TopicHandler blockV3TopicHandler = new BlockV3TopicHandler(
                new SnappyPreparedGossipMessageFactory(),
                gossipAsyncRunner,
                UInt64.valueOf(100L),
                "0x9965507D1a55bcC2695C58ba16FB37d819B0A4dc",
                unsafeBlockQueue);

        PreparedGossipMessage preparedGossipMessage = blockV3TopicHandler.prepareMessage(Bytes.fromHexString(data));
        SafeFuture<ValidationResult> future = blockV3TopicHandler.handleMessage(preparedGossipMessage);
        ValidationResult res = future.get();
        assertEquals(ValidationResult.Invalid, res);
    }

    @Test
    @Disabled
    void testV3RejectNonZeroBlobGasUsed() throws ExecutionException, InterruptedException {
        String data =
                "0xf104f0436cd94b95a37c662c0d7a22346105f4af7d7dac0929d2e5a2047fd945f7cc37db2598623bb0355244e691c2d8deb925b0fc691b9b6875fc64e34b80f0cdb7649a000000006a03000412346a1d00fe0100fe0100fe0100fe0100fe0100fe01004201000cbd77d465219504100201067601007cf5248c2d75a4532d5c6d2bd780a59f0ba5edfcb9393ebb2a5ded6acd8ff3eb5a014401043c01000000000000000000000000000000";

        final MetricsSystem metricsSystem = new PrometheusMetricsSystem(
                ImmutableSet.<MetricCategory>builder()
                        .addAll(EnumSet.allOf(StandardMetricCategory.class))
                        .addAll(EnumSet.allOf(HildrNodeMetricsCategory.class))
                        .build(),
                true);
        final AsyncRunner gossipAsyncRunner = AsyncRunnerFactory.createDefault(
                        new MetricTrackingExecutorFactory(metricsSystem))
                .create("hildr_node_gossip", 20);
        MpscUnboundedXaddArrayQueue<ExecutionPayload> unsafeBlockQueue = new MpscUnboundedXaddArrayQueue<>(1024 * 64);
        BlockV3TopicHandler blockV3TopicHandler = new BlockV3TopicHandler(
                new SnappyPreparedGossipMessageFactory(),
                gossipAsyncRunner,
                UInt64.valueOf(100L),
                "0x9965507D1a55bcC2695C58ba16FB37d819B0A4dc",
                unsafeBlockQueue);

        PreparedGossipMessage preparedGossipMessage = blockV3TopicHandler.prepareMessage(Bytes.fromHexString(data));
        SafeFuture<ValidationResult> future = blockV3TopicHandler.handleMessage(preparedGossipMessage);
        ValidationResult res = future.get();
        assertEquals(ValidationResult.Invalid, res);
    }

    @Test
    @Disabled
    void testV3Valid() throws ExecutionException, InterruptedException {
        String data =
                "0xf104f0434442b9eb38b259f5b23826e6b623e829d2fb878dac70187a1aecf42a3f9bedfd29793d1fcb5822324be0d3e12340a95855553a65d64b83e5579dffb31470df5d010000006a03000412346a1d00fe0100fe0100fe0100fe0100fe0100fe01004201000cc588d465219504100201067601007cfece77b89685f60e3663b6e0faf2de0734674eb91339700c4858c773a8ff921e014401043e0100";

        final MetricsSystem metricsSystem = new PrometheusMetricsSystem(
                ImmutableSet.<MetricCategory>builder()
                        .addAll(EnumSet.allOf(StandardMetricCategory.class))
                        .addAll(EnumSet.allOf(HildrNodeMetricsCategory.class))
                        .build(),
                true);
        final AsyncRunner gossipAsyncRunner = AsyncRunnerFactory.createDefault(
                        new MetricTrackingExecutorFactory(metricsSystem))
                .create("hildr_node_gossip", 20);
        MpscUnboundedXaddArrayQueue<ExecutionPayload> unsafeBlockQueue = new MpscUnboundedXaddArrayQueue<>(1024 * 64);
        BlockV3TopicHandler blockV3TopicHandler = new BlockV3TopicHandler(
                new SnappyPreparedGossipMessageFactory(),
                gossipAsyncRunner,
                UInt64.valueOf(100L),
                "0x9965507D1a55bcC2695C58ba16FB37d819B0A4dc",
                unsafeBlockQueue);

        PreparedGossipMessage preparedGossipMessage = blockV3TopicHandler.prepareMessage(Bytes.fromHexString(data));
        SafeFuture<ValidationResult> future = blockV3TopicHandler.handleMessage(preparedGossipMessage);
        ValidationResult res = future.get();
        assertEquals(ValidationResult.Valid, res);
    }

    @Test
    @Disabled
    void testV2Valid() throws ExecutionException, InterruptedException {
        String data =
                "0xc104f0433805080eb36c0b130a7cc1dc74c3f721af4e249aa6f61bb89d1557143e971bb738a3f3b98df7c457e74048e9d2d7e5cd82bb45e3760467e2270e9db86d1271a700000000fe0300fe0300fe0300fe0300fe0300fe0300a203000c6b89d46525ad000205067201009cda69cb5b9b73fc4eb2458b37d37f04ff507fe6c9cd2ab704a05ea9dae3cd61760002000000020000";

        final MetricsSystem metricsSystem = new PrometheusMetricsSystem(
                ImmutableSet.<MetricCategory>builder()
                        .addAll(EnumSet.allOf(StandardMetricCategory.class))
                        .addAll(EnumSet.allOf(HildrNodeMetricsCategory.class))
                        .build(),
                true);
        final AsyncRunner gossipAsyncRunner = AsyncRunnerFactory.createDefault(
                        new MetricTrackingExecutorFactory(metricsSystem))
                .create("hildr_node_gossip", 20);
        MpscUnboundedXaddArrayQueue<ExecutionPayload> unsafeBlockQueue = new MpscUnboundedXaddArrayQueue<>(1024 * 64);
        BlockV2TopicHandler blockV2TopicHandler = new BlockV2TopicHandler(
                new SnappyPreparedGossipMessageFactory(),
                gossipAsyncRunner,
                UInt64.valueOf(100L),
                "0x9965507D1a55bcC2695C58ba16FB37d819B0A4dc",
                unsafeBlockQueue);

        PreparedGossipMessage preparedGossipMessage = blockV2TopicHandler.prepareMessage(Bytes.fromHexString(data));
        SafeFuture<ValidationResult> future = blockV2TopicHandler.handleMessage(preparedGossipMessage);
        ValidationResult res = future.get();
        assertEquals(ValidationResult.Valid, res);
    }

    @Test
    @Disabled
    void testV2NonZeroWithdrawals() {
        String data =
                "0xed04f0430cc8e59221e3d3eeb5c42307557aae2cb4823de2950cf54229ec29453662c92e748f4249507ee1f1c95d1f6ed96377244e5ee0b13f39a58c7d34a0579570579001000000fe0300fe0300fe0300fe0300fe0300fe0300a203000ccd89d46525ad000205067201007cc6713cb4dfd6e18fac545e2a48dd42ce51dcae91533d96048ff379e9e8e1f62d05440c020000010d4611086e0100";

        final MetricsSystem metricsSystem = new PrometheusMetricsSystem(
                ImmutableSet.<MetricCategory>builder()
                        .addAll(EnumSet.allOf(StandardMetricCategory.class))
                        .addAll(EnumSet.allOf(HildrNodeMetricsCategory.class))
                        .build(),
                true);
        final AsyncRunner gossipAsyncRunner = AsyncRunnerFactory.createDefault(
                        new MetricTrackingExecutorFactory(metricsSystem))
                .create("hildr_node_gossip", 20);
        MpscUnboundedXaddArrayQueue<ExecutionPayload> unsafeBlockQueue = new MpscUnboundedXaddArrayQueue<>(1024 * 64);
        BlockV2TopicHandler blockV2TopicHandler = new BlockV2TopicHandler(
                new SnappyPreparedGossipMessageFactory(),
                gossipAsyncRunner,
                UInt64.valueOf(100L),
                "0x9965507D1a55bcC2695C58ba16FB37d819B0A4dc",
                unsafeBlockQueue);

        PreparedGossipMessage preparedGossipMessage = blockV2TopicHandler.prepareMessage(Bytes.fromHexString(data));
        SafeFuture<ValidationResult> future = blockV2TopicHandler.handleMessage(preparedGossipMessage);
        ValidationResult res = future.join();
        assertEquals(ValidationResult.Invalid, res);
    }

    @Test
    void testV2NonZeroBlobProperties() throws ExecutionException, InterruptedException {
        String data =
                "0xd104f0438a265ca8d1047e6e6dc04f88b82e87b4ac7d083fc271e153a32b183e3d6f0fa7139fa2808187f0ad12291e7c9f0a2296ac90339ddb1711d31b360a41ce67069000000000fe0300fe0300fe0300fe0300fe0300fe0300a203000c828ad46521ad04100201067601007c095842332e4409ec736246c082fc1e2a217d17af81ce0bbda3ed1bdf42f828c8014401043e0100";

        final MetricsSystem metricsSystem = new PrometheusMetricsSystem(
                ImmutableSet.<MetricCategory>builder()
                        .addAll(EnumSet.allOf(StandardMetricCategory.class))
                        .addAll(EnumSet.allOf(HildrNodeMetricsCategory.class))
                        .build(),
                true);
        final AsyncRunner gossipAsyncRunner = AsyncRunnerFactory.createDefault(
                        new MetricTrackingExecutorFactory(metricsSystem))
                .create("hildr_node_gossip", 20);
        MpscUnboundedXaddArrayQueue<ExecutionPayload> unsafeBlockQueue = new MpscUnboundedXaddArrayQueue<>(1024 * 64);
        BlockV2TopicHandler blockV2TopicHandler = new BlockV2TopicHandler(
                new SnappyPreparedGossipMessageFactory(),
                gossipAsyncRunner,
                UInt64.valueOf(100L),
                "0x9965507D1a55bcC2695C58ba16FB37d819B0A4dc",
                unsafeBlockQueue);

        PreparedGossipMessage preparedGossipMessage = blockV2TopicHandler.prepareMessage(Bytes.fromHexString(data));
        SafeFuture<ValidationResult> future = blockV2TopicHandler.handleMessage(preparedGossipMessage);

        ValidationResult res = future.get();
        assertEquals(ValidationResult.Invalid, res);
    }

    @Test
    void testV3BlockMessageParse() {
        String data =
                "0x84380d560f23617f860841983449d094dd1b4b28280fbf687c2014eed37b857c7b7a1c29e4ab38407cc6e91c474370b168d50d098b0acc82aaebee71c3894a5e01731092c4178911c8fccabd77ccea6778bf3fa804541180d2b21d9e1e8fc972ebad985a8500c04d4c79a6f3912d32d1b65ed331a55cb623fc6d22bef36ce0b9254200000000000000000000000000000000000011c83843549f7ae250be9fe37ba3dc8370561a3d5e330b66401c66414fa7b76f5baeaa832b332a9ef529a58b147db289443452f757b8f6fa49ff4a6b7a15c5f29100000000000000000000000000000000000000000000000000000000000000000000100020000000000000000000000000000000000200000000000000200000000000000000000000000001002000000000000001000000000000000000000000000000020000000000000000000800000000000000000000010000000000080000000004000000000000000000000000008480000000000000000000000000000000001000000000000000000000001000000040000000000000000000000000000000000000000000000000000000004008000000000000000000000020000000000000000000000000000000000000000000000000000000000000000000a410fed2cb80e25704c1cab95b5ce301341409e3f9ad85e2eb891941ef239606a1db8f000000000080c3c90100000000739a020000000000ee92f6650000000010020000fc000000000000000000000000000000000000000000000000000000000000000e04fa219744bf66a92382ab7d6cfc7cafe79fb6a266248ddd4a8afef567e1f610020000e70500000000000000000000000000000000000008000000030100007ef8f8a0f8c03c494afb918735be6e57706b1bc4a0957eef9aa095a8d30da052e9b387ca94deaddeaddeaddeaddeaddeaddeaddeaddead00019442000000000000000000000000000000000000158080830f424080b8a4440a5e2000001db0000d273000000000000000020000000065f692a4000000000053f7c50000000000000000000000000000000000000000000000000000000284ffe4fc000000000000000000000000000000000000000000000000000000056b5ee0c5b391490331d2e77a26b664cb609fdb4398f42939e8eb808188de905b06f8fc160000000000000000000000008f23bb38f531600e5d8fddaaec41f13fab46e98cf902d183027ba7843b9aca008307a12094f9a98ec64ca90d712adb5bb46545b6b9f9d184f080b90264c9807539000000000000000000000000000000000000000000000000000000000000008000000000000000000000000000000000000000000000000000000000000001a00000000000000000000000000000000000000000000000000000000000000200010100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000001000000000000000000000000c14a0f1461ec27a9012d37db9856e5b200015de0040201000300000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000006000000000000000000000000000000000000000000000000000000000000000040000000000000000000000000000000000000000000000000000000d241c10500000000000000000000000000000000000000000000000000000000d2479bd880000000000000000000000000000000000000000000000000000000d24b60f8e0000000000000000000000000000000000000000000000000000000d25dfd1800000000000000000000000000000000000000000000000000000000000000002b1f4638cb3e6e4550cdbb45cbc9fcff15c659abbf566233bbe73f4e045e9abbcdc799b4e90497971a43d53b9db1b49b07eb495052d3d2200466e27d60fa26ab80000000000000000000000000000000000000000000000000000000000000002766bc9ad7f8dd00208d9b2c6a5018f552fbdd414dc767c913ef89082cc6cd2a11cfd9a8521ce908d775047cf08628c106abd98a85788a16fb00abeed15ce3ae98401546fdba0b391a5182142d8aecf95eeee2722b72dda0f9d766cfeee5777602c69cac09cb9a029eaf81fbdf50e4f43b65b89e29b2b749384b67b21fc67f399d0d54f5df60ff1";
        AbstractTopicHandler.BlockMessage blockMessage =
                AbstractTopicHandler.BlockMessage.from(Numeric.hexStringToByteArray(data), BlockVersion.V3);
        assertEquals(
                "0x731092c4178911c8fccabd77ccea6778bf3fa804541180d2b21d9e1e8fc972eb",
                blockMessage.payloadEnvelop().executionPayload().parentBeaconBlockRoot());
    }

    @Test
    void testV3RejectExecutionPayload() throws ExecutionException, InterruptedException {
        String data =
                "0xd104f043d7f7992d2817a9dd744273494896a1ef6214d9f80e95db22e132a5f95f6a5a1b67fa7e5b7813fcf090eb2ad0870e852400e8c919cc77b9905416674b3d51e3a100000000fe0300fe0300fe0300fe0300fe0300fe0300a203000c6b8ed46521ad04100201067601007c98759d697467f0fd82dc95a7999225840106105c71d3f6022f0b7a7b87791855014401043e0100";

        final MetricsSystem metricsSystem = new PrometheusMetricsSystem(
                ImmutableSet.<MetricCategory>builder()
                        .addAll(EnumSet.allOf(StandardMetricCategory.class))
                        .addAll(EnumSet.allOf(HildrNodeMetricsCategory.class))
                        .build(),
                true);
        final AsyncRunner gossipAsyncRunner = AsyncRunnerFactory.createDefault(
                        new MetricTrackingExecutorFactory(metricsSystem))
                .create("hildr_node_gossip", 20);
        MpscUnboundedXaddArrayQueue<ExecutionPayload> unsafeBlockQueue = new MpscUnboundedXaddArrayQueue<>(1024 * 64);
        BlockV3TopicHandler blockV3TopicHandler = new BlockV3TopicHandler(
                new SnappyPreparedGossipMessageFactory(),
                gossipAsyncRunner,
                UInt64.valueOf(100L),
                "0x9965507D1a55bcC2695C58ba16FB37d819B0A4dc",
                unsafeBlockQueue);

        PreparedGossipMessage preparedGossipMessage = blockV3TopicHandler.prepareMessage(Bytes.fromHexString(data));
        SafeFuture<ValidationResult> future = blockV3TopicHandler.handleMessage(preparedGossipMessage);
        ValidationResult res = future.get();
        assertEquals(ValidationResult.Invalid, res);
    }
}
