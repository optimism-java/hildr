package io.optimism.derive.stages;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.optimism.config.Config;
import io.optimism.config.Config.ChainConfig;
import io.optimism.derive.State;
import io.optimism.derive.stages.BatcherTransactions.BatcherTransactionMessage;
import io.optimism.derive.stages.Channels.Channel;
import io.optimism.types.Frame;
import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import org.bouncycastle.util.encoders.Hex;
import org.jctools.queues.MessagePassingQueue;
import org.jctools.queues.MpscGrowableArrayQueue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.utils.Numeric;

/**
 * The type ChannelsTest.
 *
 * @author grapebaba
 * @since 0.1.0
 */
class ChannelsTest {

    @Test
    @DisplayName("test push single channel frame")
    void testPushSingleChannelFrame() {
        Tuple2<Channels<BatcherTransactions>, MessagePassingQueue<BatcherTransactionMessage>> tuple2 = createStage();
        Frame frame = new Frame(BigInteger.valueOf(5L), 0, 0, new byte[0], true, BigInteger.ZERO);

        tuple2.component1().pushFrame(frame);
        assertEquals(1, tuple2.component1().getPendingChannels().size());
        assertEquals(
                BigInteger.valueOf(5L),
                tuple2.component1().getPendingChannels().get(0).getChannelId());
        assertTrue(tuple2.component1().getPendingChannels().get(0).isComplete());
    }

    @Test
    @DisplayName("test push multi channel frames")
    void testPushMultiChannelFrames() {
        Tuple2<Channels<BatcherTransactions>, MessagePassingQueue<BatcherTransactionMessage>> tuple2 = createStage();
        Frame frame1 = new Frame(BigInteger.valueOf(5L), 0, 0, new byte[0], false, BigInteger.ZERO);

        tuple2.component1().pushFrame(frame1);
        assertEquals(1, tuple2.component1().getPendingChannels().size());
        assertEquals(
                BigInteger.valueOf(5L),
                tuple2.component1().getPendingChannels().get(0).getChannelId());
        assertFalse(tuple2.component1().getPendingChannels().get(0).isComplete());
        Frame frame2 = new Frame(BigInteger.valueOf(5L), 1, 0, new byte[0], true, BigInteger.ZERO);

        tuple2.component1().pushFrame(frame2);
        assertEquals(1, tuple2.component1().getPendingChannels().size());
        assertEquals(
                BigInteger.valueOf(5L),
                tuple2.component1().getPendingChannels().get(0).getChannelId());
        assertTrue(tuple2.component1().getPendingChannels().get(0).isComplete());
    }

    @Test
    @DisplayName("test ready channel")
    void testReadyChannel() {
        Tuple2<Channels<BatcherTransactions>, MessagePassingQueue<BatcherTransactionMessage>> tuple2 = createStage();
        Frame frame1 = new Frame(BigInteger.valueOf(5L), 0, 0, new byte[0], false, BigInteger.valueOf(43L));

        Frame frame2 = new Frame(BigInteger.valueOf(5L), 1, 0, new byte[0], true, BigInteger.valueOf(96L));

        tuple2.component1().pushFrame(frame1);
        tuple2.component1().pushFrame(frame2);
        Channel channel =
                tuple2.component1().fetchReadyChannel(BigInteger.valueOf(5L)).get();
        assertEquals(BigInteger.valueOf(5L), channel.id());
        assertEquals(BigInteger.valueOf(96L), channel.l1InclusionBlock());
    }

    @Test
    @DisplayName("test ready channel not found")
    void testReadyChannelStillPending() {
        Tuple2<Channels<BatcherTransactions>, MessagePassingQueue<BatcherTransactionMessage>> tuple2 = createStage();
        Frame frame1 = new Frame(BigInteger.valueOf(5L), 0, 0, new byte[0], false, BigInteger.valueOf(43L));

        tuple2.component1().pushFrame(frame1);
        Optional<Channel> channelOpt = tuple2.component1().fetchReadyChannel(BigInteger.valueOf(5L));
        assertTrue(channelOpt.isEmpty());
    }

    @Test
    @DisplayName("Test read channel data from batch tx successfully")
    void testReadChannelData() {
        String data =
                "00656531d7fca1ad32740ea3adca85922a0000000005dc78dadac9f58b71c9d7edacb77bd6323dd823c8ffeb44c059dee7ffb405f9b68b2feb9a3ef3508cc78be9f9edab1ea8557c09e3b1e83cffc05f2a8445c09141c08145c0914580010e181930012332c588a68c114323238c603cffb8e3e20ecb8f4f0d365a15b4ffe09abf6ddad1b7755a79ac67ff39b7bb9ddf3c67ab929e46cd439bf56c7757a8f67dddd968dbf1fc647b4498f6929c0b75a5f2d5557d491b6293a37343b33f681e2c37ae551763b8fc8c598271c67aed7426ff8e2dd7170a31ffbdfce97bb5d9ed0b1dfb94efcb6eb5efdb1bfb7152f8c4b9ae321c5b73af7f12517f3ec15e6effd5f0ddae251cd7673eb65b5d26a1b1e5e68e4b328587b5e6dd56717fb93d6cb3d5ea07b7ffdc0c0af2f86ab8485c73cd3fef280316fe282d96b4be42fd9df28d562c77edecef9c923fe9f6a069a346c1b7b33e9cc76c3e46dc4bacfc191cd3c8afcbc12e52eeaa7c9127ed6412c70ebee6b52dbc825971322c5eaea9adfb6673a54fddf37696757ff4aafa433f6da3531b23988abba61d3ba7beeecbb40db56935f1e7661d3812798fb95131b69eefe68f25fbf7ee7dd870517a79b4cecf0bb73ac439d5a7b7942c3cdef156ac284f31467ba5e0b39a4d8f569c303bba2c52e1b8f98c0ce91d4a96b33ffcaa985c94b2c06ec781a0c9e9d3bc2670ef1429e09b782fb323d9692607dbe9a30589dbbb6e479efbbe72d62af9f038b605f38ced7d32266f751189ff6a68f2d4b63d94c5f88cf575f7cfbbc3e3fae64b5cdc7d4cadf8ebc24bb2894b657e733d78fb3e6d47dca4bdfc1d264c9d2562dfaff4396cb83cfd94c2dc7766cbd3d218fde61f12e6b9767ed36dc625138d6778f7187a28075597196a6d522f9ac9b8e60a77dc094daf395ec7175c0f63f1326a5f257762b172c517dfbdf6ce7ed7f518129fac14fa77d84140d9e2f92791a34b7e3d7f27a4e82c7c66fbf38589266a16d3a2db4eba4e0d7b646e98fdbdea9af4e3a7739a0acb5c53f65c70c24ca002361a978eee8e5a59adbce3c786730719839d1fce3e894d8c12bdc48a31fd64126c68e6777268e677cedbc9c4a2bf26538a011f60725ecb801f24e097665c40403fe7fefa0f719efb64a6f1b7ca591d5aaa36bfece6cb15dfc37ea65d6cf37fd3b971b6848de6dc1bd7debe378909b2bdd6afc061fd29fa6e59a3935dea85d34213658e093f3a776abee3b523ab2eb933771ee2f0718c8d55ce0fff7e4b4a3395fba9bd8949656292c2a18d5cb97dcfcfccaeba72f6d59b2f824df5f5ca6eff5f1db96e57b14fe370a9b0cca7aeca4e7d4b5b33a9b06496a936455325669e8b489e2c1e5bf5e55666cf0b57070f7585cf35d922eaf6a57f4d583f2e8d8e6cbf31b7f1d3c9d432b377166db5f61bf7695b6ed67cc4f2e58bc4d1a7b39fe79e63f1582adbac7831454fc322c952de71f9d463ff73b86ec5bcd0e5519176645bc29572fa7df1cf49d3df24ea2e10d00b9f1fdd2c3c4b32d0f3e8a6355bf57708142c6ae3e8e0ff97ae2fe0e9f1a09b5b488140f8317dbed5ba6f8acc3e09bb0299aae517394dea2eb96419548530587fbffde1a7c734b7a625d2193a179630bf3634942998f4517fd6c71b0155779c7f7ff9686daf705934ed00d38f9dedfc5a8b58ba2f30b44466e88308831f3b96186d67c845b6e8de5a7488c75550f328040d84141c60faf181bb59e0e45710def1242c523632b128a984814ae088bb4a55457efea747cf9ec61a2a7aaf7f74cc600b012d5c145a49483f37162f2715270f772f6f6ac097342f74698aa7dafab9714c563029fcc0c0a1f6dbc1049769bc0fb66d5e9ec230104933a9b8b86058c7d3ab866681ea0b4b362847edd3ecff7e22df3661dd5a9eb50c6c4e57171c5c67bebef4ec9e87d33bb9773f9e9f701a49a9492dd781dfb5075a6f58cfdb32d3edd0546dbd035167b8c4266d0c083cb22f5479fa8f6eae66c12d293b5a18577c48fd3355d363bdd5ef7cb6acc5fb7630cf3feda55f5678d57b87f786794f055d8eb1c5d23a8c7e08c91cf439e4237bd867c71da69d779876dd61dab794e5e73ef6090bf9272ce46f5fca3161217fcb69c923b7246ecc976407000000ffff01";
        byte[] dataBytes = Numeric.hexStringToByteArray(data);
        BatcherTransactions.BatcherTransaction tx =
                BatcherTransactions.BatcherTransaction.create(dataBytes, BigInteger.valueOf(10254359L));
        Channels.PendingChannel pendingChannel =
                Channels.PendingChannel.create(tx.frames().get(0));
        List<Frame> frames = tx.frames();
        for (int i = 1; i < frames.size(); i++) {
            pendingChannel.pushFrame(frames.get(i));
        }
        Channel parseChannel = Channel.from(pendingChannel);

        String channelData =
                "78dadac9f58b71c9d7edacb77bd6323dd823c8ffeb44c059dee7ffb405f9b68b2feb9a3ef3508cc78be9f9edab1ea8557c09e3b1e83cffc05f2a8445c09141c08145c0914580010e181930012332c588a68c114323238c603cffb8e3e20ecb8f4f0d365a15b4ffe09abf6ddad1b7755a79ac67ff39b7bb9ddf3c67ab929e46cd439bf56c7757a8f67dddd968dbf1fc647b4498f6929c0b75a5f2d5557d491b6293a37343b33f681e2c37ae551763b8fc8c598271c67aed7426ff8e2dd7170a31ffbdfce97bb5d9ed0b1dfb94efcb6eb5efdb1bfb7152f8c4b9ae321c5b73af7f12517f3ec15e6effd5f0ddae251cd7673eb65b5d26a1b1e5e68e4b328587b5e6dd56717fb93d6cb3d5ea07b7ffdc0c0af2f86ab8485c73cd3fef280316fe282d96b4be42fd9df28d562c77edecef9c923fe9f6a069a346c1b7b33e9cc76c3e46dc4bacfc191cd3c8afcbc12e52eeaa7c9127ed6412c70ebee6b52dbc825971322c5eaea9adfb6673a54fddf37696757ff4aafa433f6da3531b23988abba61d3ba7beeecbb40db56935f1e7661d3812798fb95131b69eefe68f25fbf7ee7dd870517a79b4cecf0bb73ac439d5a7b7942c3cdef156ac284f31467ba5e0b39a4d8f569c303bba2c52e1b8f98c0ce91d4a96b33ffcaa985c94b2c06ec781a0c9e9d3bc2670ef1429e09b782fb323d9692607dbe9a30589dbbb6e479efbbe72d62af9f038b605f38ced7d32266f751189ff6a68f2d4b63d94c5f88cf575f7cfbbc3e3fae64b5cdc7d4cadf8ebc24bb2894b657e733d78fb3e6d47dca4bdfc1d264c9d2562dfaff4396cb83cfd94c2dc7766cbd3d218fde61f12e6b9767ed36dc625138d6778f7187a28075597196a6d522f9ac9b8e60a77dc094daf395ec7175c0f63f1326a5f257762b172c517dfbdf6ce7ed7f518129fac14fa77d84140d9e2f92791a34b7e3d7f27a4e82c7c66fbf38589266a16d3a2db4eba4e0d7b646e98fdbdea9af4e3a7739a0acb5c53f65c70c24ca002361a978eee8e5a59adbce3c786730719839d1fce3e894d8c12bdc48a31fd64126c68e6777268e677cedbc9c4a2bf26538a011f60725ecb801f24e097665c40403fe7fefa0f719efb64a6f1b7ca591d5aaa36bfece6cb15dfc37ea65d6cf37fd3b971b6848de6dc1bd7debe378909b2bdd6afc061fd29fa6e59a3935dea85d34213658e093f3a776abee3b523ab2eb933771ee2f0718c8d55ce0fff7e4b4a3395fba9bd8949656292c2a18d5cb97dcfcfccaeba72f6d59b2f824df5f5ca6eff5f1db96e57b14fe370a9b0cca7aeca4e7d4b5b33a9b06496a936455325669e8b489e2c1e5bf5e55666cf0b57070f7585cf35d922eaf6a57f4d583f2e8d8e6cbf31b7f1d3c9d432b377166db5f61bf7695b6ed67cc4f2e58bc4d1a7b39fe79e63f1582adbac7831454fc322c952de71f9d463ff73b86ec5bcd0e5519176645bc29572fa7df1cf49d3df24ea2e10d00b9f1fdd2c3c4b32d0f3e8a6355bf57708142c6ae3e8e0ff97ae2fe0e9f1a09b5b488140f8317dbed5ba6f8acc3e09bb0299aae517394dea2eb96419548530587fbffde1a7c734b7a625d2193a179630bf3634942998f4517fd6c71b0155779c7f7ff9686daf705934ed00d38f9dedfc5a8b58ba2f30b44466e88308831f3b96186d67c845b6e8de5a7488c75550f328040d84141c60faf181bb59e0e45710def1242c523632b128a984814ae088bb4a55457efea747cf9ec61a2a7aaf7f74cc600b012d5c145a49483f37162f2715270f772f6f6ac097342f74698aa7dafab9714c563029fcc0c0a1f6dbc1049769bc0fb66d5e9ec230104933a9b8b86058c7d3ab866681ea0b4b362847edd3ecff7e22df3661dd5a9eb50c6c4e57171c5c67bebef4ec9e87d33bb9773f9e9f701a49a9492dd781dfb5075a6f58cfdb32d3edd0546dbd035167b8c4266d0c083cb22f5479fa8f6eae66c12d293b5a18577c48fd3355d363bdd5ef7cb6acc5fb7630cf3feda55f5678d57b87f786794f055d8eb1c5d23a8c7e08c91cf439e4237bd867c71da69d779876dd61dab794e5e73ef6090bf9272ce46f5fca3161217fcb69c923b7246ecc976407000000ffff";
        Channel channel = new Channel(BigInteger.ONE, Hex.decode(channelData), BigInteger.ZERO);

        assertArrayEquals(channel.data(), parseChannel.data());
    }

    private Tuple2<Channels<BatcherTransactions>, MessagePassingQueue<BatcherTransactionMessage>> createStage() {
        Config config = new Config(
                "",
                "",
                "",
                "",
                "",
                "",
                null,
                null,
                null,
                9545,
                null,
                null,
                false,
                false,
                Config.SyncMode.Full,
                ChainConfig.optimismSepolia());
        MessagePassingQueue<BatcherTransactionMessage> transactionMessageMessagePassingQueue =
                new MpscGrowableArrayQueue<>(4096);
        AtomicReference<io.optimism.derive.State> state =
                new AtomicReference<>(State.create(new TreeMap<>(), null, null, null, config));
        Channels<BatcherTransactions> channels =
                Channels.create(new BatcherTransactions(transactionMessageMessagePassingQueue), config, state);
        return new Tuple2<>(channels, transactionMessageMessagePassingQueue);
    }
}
