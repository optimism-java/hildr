package io.optimism.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.collect.ImmutableSet;
import io.libp2p.core.pubsub.ValidationResult;
import io.optimism.engine.ExecutionPayload;
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
