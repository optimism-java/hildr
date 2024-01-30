package io.optimism.derive.stages;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import io.optimism.config.Config;
import io.optimism.derive.stages.Channels.Channel;
import io.optimism.utilities.derive.stages.Batch;
import io.optimism.utilities.derive.stages.Frame;
import io.optimism.utilities.derive.stages.RawSpanBatch;
import io.optimism.utilities.derive.stages.SingularBatch;
import io.optimism.utilities.derive.stages.SpanBatch;
import java.math.BigInteger;
import java.util.List;
import org.bouncycastle.jcajce.provider.digest.Keccak;
import org.bouncycastle.util.encoders.Hex;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.web3j.utils.Numeric;

/**
 * The type BatchesTest.
 *
 * @author grapebaba
 * @since 0.1.0
 */
class BatchesTest {

    /**
     * Decode singular batches.
     */
    @Test
    @DisplayName("Test decode singular batches successfully")
    void decodeBatches() {
        String data = "78dad459793894fbdb7f9e19db20eb902dbb9086b410b2af2939b66c255bd60991"
                + "c8a133c6845276c9daa36c21bb3211932c8908591a6509132a3b1959decb"
                + "e93ade73aeebbc745ee7f773755fd7fcf599effdfd3cf7f5b93ff7f7f93e"
                + "786a5d804cad05255ef05f6445189cc97f1b4ef3656d2cdd318bcbe30a93"
                + "f689737dea1f3297ed8d83029fa64364f70716e8c138e517e81606f661f7"
                + "54e982039eda1366dc277286510cf7142b717613166832d56279316cb100"
                + "0ba65305f1e230eb3fec23da58628342a55fc9ee47fa1db79e1d672c3968"
                + "bfd4740253ae81b0ca2a01fe1456ad32f374dd47270af5fcc69839881590"
                + "a92137b059305c9d2280500faf1a489d7179f26143eb2923944efb05a138"
                + "1b4536499f9ed9be14ff2817142427de6d4a59af3be62c8fa3d5927fef36"
                + "15e6226f4bc1ad412d4b8c609853dc8b87b591612d4170a5d9df4953a7f1"
                + "c73ebc397a8f742d3526ac08559a86953e948c9e75c7e061f68d186f3960"
                + "f3c06c0e83d0e6380c0041601bf197c591f9a7553e1647f6f171fa191419"
                + "c90d03f08605100061f06d6c60bd054eb119788b6b8ff14ee2eb052e0af9"
                + "78632db54e63fed6900a3ad0b179456da86a97b9134d00b9d0b04b97a604"
                + "dd743bb92fa035f0412bec13a2793e7a9ad5d33bb1bdcbf20d22146377f9"
                + "d0ca56f9d51733a63507dc9270cc575fd67821d24e1d76a18bce5c503c71"
                + "05ed33cd51c62075c2284ee2e2120bf1154d553ccc2694c37ef478185d64"
                + "e7c7e23d8d1ca784c7b17034d436d228729fd385b9a73a2900b0adc7ec9e"
                + "be6a12bbd61c2b23cc5ab27a0bd80beda6203f2ef8e02540f41dd4154ca8"
                + "b52563434b3a0d6dae239607cff261e9f4cbf317f3b030b72030180a02cf"
                + "45c6d6f5b401fb6e5f1ae6541b1a1fafe55ab9b462e28729d77840995cf1"
                + "67f2bd365a1af9538a93022353d6019218be002b7cfba60fbb348559e7cb"
                + "9ca6cc20642cf82997cb7d58b7c2c919b96f29f9f0c52ceb792c4ec403ad"
                + "cf025d38461918536ade57d6256794c54d9591726b85ae5ca645790264f5"
                + "ce99be48fcce9766836f76e9b73c52a9fd2c2a675e4122f85d148b406cd3"
                + "f6f8c2ca860ad88b4201609def590ffbe3d8667b8495284986b19e918fd4"
                + "f26e7aacf5e8d7bc6733e3bda1f65a90a4b901166e8317198816e8b8f6a2"
                + "35b2735954b95a877177b165b1dd19064d9eef7cb936f83a68a52447c996"
                + "a14e2d7967b2a0f20a8e440bc8fc8bf54da41df6d00a95ee76eea6a1e43c"
                + "d90b374dee48a889b33ec87480a8d776204b17e24aa9f787efc9cb246560"
                + "634d57bf1ac252549f9d9f9f4b141f0ba3435c09837fe71bb8c1f7ffb0e4"
                + "edf20518d554d6f97211849d7bdf9e1d4d6dad75f3ffaa29f5f5bed74c29"
                + "1159ddb4d274dd4c7f72113a2f9fe17534fc9b46f02ffcb153d6a0efcd41"
                + "c7de92d78f16e73cbeec5b2496f17fe71bbcc1175fd6914a7890e046782b"
                + "25d58a0e33c8e046996e932f68a7e97bf6c6773dd414db0992ee66f862ef"
                + "d7b0d4cbb38a2725a6b15af899c579f5f73395a46ac6439a19c1ac17300a"
                + "69dd16434ea3f0abc7382c254daeedb28cb28ce8a4715a16f3c0532e0164"
                + "ca052880911a317f464a05ac6f507f15e4d2507c37acc2672f2a65ba8945"
                + "2cd462e4c10f0f53373265f61f83c987716330c5ad883c130aef10d53512"
                + "4188963915286248c63fe160a25aa04ce01182bdcf7cabffe445c9c40200"
                + "6fa1d9c9c12406bec7637610ffbdc0114419d3d1c2665984e38779b84aa0"
                + "406a349297e54ec1a783c92edc841c4a5f8af3ab9fa54b24fb31dfd02339"
                + "b55153b01c472a83b7bf729c6ea4d16268a519df2abfc77da516e51cbad5"
                + "b523bacf2fa0510ca7809952648a79ee1749ae815455db8bbf5adc99f5ca"
                + "08a2486c653e8ab649921b701814ef71ed1c312261efe82c0c7960e1aed0"
                + "ac772a7a2d4a8ad5c72cfe4b4153af34aa62f09866423392fe1ee9158054"
                + "e7877883c2be453f6f873fbcc5bfa785cf96646d7020bba6b16726f7bd76"
                + "bf8e6b9ec886a69936346d9eef031cbddfef860b9aa276fc98d9e57b7282"
                + "f0dfd2f4f6e22f9adecf6ec5acb74cef4d49beeedc4b607f0cc01b0c7750"
                + "d3300d5ea95f13770efffea7ee9214aa608830831027a6cac7e43f5263b6"
                + "09ec5ac8392856353d8d543ca1f56c7fa91581533ba051a7521ea8b34067"
                + "75e144c3f49fa69ee7c4b19d344a99df2abfad67aa357a685e092af3f27b"
                + "aa103215d1299e79bcdf523975e98d79bc8892bf67f091e78d11d8525ac9"
                + "73c7925330ef4a1f45f7e851fa464c16e2bc6fb8ea74ad9bbf6cad30116d"
                + "6eef0e98654be15e71c33a9d6a54709f9cd192375a7b68ba8509905f5243"
                + "96ac59cb99b80757cbd2ae33093dbd51d426ee10ec98b966fde1e81919bb"
                + "727d60f12444e546317fcd852c9fa41a622735d32f28716c9a7726dcedf3"
                + "613a7782a67888c40f5bbf07e18f69a29975d88f645a878b8f9889ef2f9c"
                + "2f2aa6d5e7111be9e71825db4ebef6375bf9e1949e7f9a264a731b9d57aa"
                + "9d548c58ae610dcc797a805e9e0920b0d405ff849d3737009e8af53f45ac"
                + "fddc95f16a36c40c80bfe6ded1d71c9670466827f1f502fb36485df66b7c"
                + "3d35669fdb34dd9ed97fd3d78a973eb0c1c4452f212660cc155545bc93f3"
                + "755f150a56e0453410f37a721e465d48f09b5f26a97356cac9cb176f957f"
                + "8f0ca7d01518275b5c9cf7a3eb7908dc9bc84ee704915bb4353aba2bc01d"
                + "9b2277fc527487470d429f45f8dd2ac154d9a24af8c85be039e5a0125f95"
                + "414f1b6ebdf3507abe4371059ecb17564fe60829d393a4af4dc91ba02869"
                + "451ba5579a726f8f43f23315d143b465b436cbd5c65c2c7eec76e99ae3d1"
                + "e6c885f7b9b56d079db9fff7d57d7e43d346056b4b3e80fd41a4ab83bfe3"
                + "924fd91bca2b0a3fe1098961d9770959672e55d1203cce4573c60180d7b3"
                + "51eda4a62588777c77125f2f3045fa5304178bfee869bb89570f6119d16a"
                + "bb5e8f7334266864d5791cacd655e1ad9b2b9cd60aebb5d2b53832281831"
                + "5e3bd9fd793f4cea6925ca7c363d2d245170abfcdad50d221509fa89e708"
                + "3c4f92436dbe527a7f48fdd6c24edb36991e8874e83cab0406a0463b966f"
                + "f376f194e14c4171a5b05d3cfb4cd69e0512e063ed87e32faf9f900afd76"
                + "1f9e7858d96fc600e3e353e7bae4d0dbe455f6f5b9e31beef46255372739"
                + "88514d2088e8d79c14162c29955b91ef33a8467208283ffdd0750fcbeebd"
                + "6c621578582e408665419705c9a3495ac8b9ea9595986cf5cc03579bd43d"
                + "898e96c55cc5828691b5f8ea1f36ff4b6498391e761a46861962c1f4200a"
                + "5c355694092bca1404fa88c536b029cbce2c0d1cfb86465a4a08ed0ebe7b"
                + "adc715830787d113aec15b946b8b7600f9b7c0adb7d76effac9ffe26b6e0"
                + "07506b1aeb48991869fca7f6a7d9c67ad1b9884307b6b93f4800a1eceb15"
                + "cb4e3ebc394e77da220de3b227739a05094f3e4848d3199b2255ba431ca0"
                + "dfa8f5625fba3725f9d3c514c5513c763b7caffbfaa43a77411e876ac8b9"
                + "4fbc56788a11804c31089994cc79d273068924c7ef9f5de11a4ea6da0f32"
                + "1316f7cf7774f5843712448c7e58ad97c914311bb6beb061eb6946166e1c"
                + "98bdef8e2c921e63a4ed085d0db4693fa1addb84a7db0f7649c488528df6"
                + "a9f1be1c05e0a37d7010beade3d0b66c1d085966df161e8adafcc6355496"
                + "632bdbcd825623f88f18b7f1b9c2cfa949bf793859c51a57a8c23cbc7f7a"
                + "f5aa5155f1dcf1c71de23c0bfcb40a09aa4deda6050c8569ab2f5c537eb9"
                + "e087c42c3a670c286e959f5fcf1e57393465caf598def15e14c588dd7088"
                + "4248da9c6b6bd44d54cc73bde72a23aa259d7b8ff77d8ae97b3150e02124"
                + "5ddf4ada65661daf806e9d9dabec5558b7f550ebf7ec260b16b6eeca8b7a"
                + "1aaaf9c5a26c0d951e22723402ab211f1e29dba840729edee9496582beaa"
                + "d4554e5e2eed3d11a14283c9e23ace5d2b4e433d0fcc3078b0124606cbb1"
                + "603aec8f6f23415408e358da0a8b733edac893e8b77bef4f59328a6ae5d3"
                + "ca87b0e58e7f115001f0a0c6214938f69fb4f9df5d94fd7349511c8be8f7"
                + "6872e109bd9bc6c2fdfff03993e49ed485a226b1da209b4d975acc32f9a9"
                + "00ffa6cfffddf31340280d2efa59844d59a7ec592dd5a87998b6113506c4"
                + "4c665ca197cebff1c90e5484cc8a6cb2c5b1badab35aefa35c1384f0bb64"
                + "59061ad574c2f37f8bbbd2e8dff5f27f020000ffff8db46838";
        Channel channel = new Channel(BigInteger.ONE, Hex.decode(data), BigInteger.ONE);
        List<Batch> batches = Batches.decodeBatches(channel);

        assertEquals(6, batches.size());
        assertEquals(
                "0x9a6d7cf81309515caed98e08edffe9a467a71a707910474b5fde43e2a6fc6454",
                ((SingularBatch) batches.get(0).batch()).parentHash());
    }

    /**
     * Decode span batches.
     */
    @Test
    @DisplayName("Test decode span batches successfully")
    void testDecodeSpanBatch() {
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

        List<Batch> batches = Batches.decodeBatches(channel);
        assertEquals(1, batches.size());

        final Config.ChainConfig chainConfig = Config.ChainConfig.optimismSepolia();
        final var batch = (RawSpanBatch) batches.get(0).batch();

        final SpanBatch spanBatch = batch.toSpanBatch(
                chainConfig.blockTime(), chainConfig.l2Genesis().timestamp(), chainConfig.l2ChainId());
        long blockTxCountsSum = spanBatch.getBatches().stream()
                .mapToLong(e -> e.transactions().size())
                .sum();
        assertEquals(9, blockTxCountsSum);

        assertEquals(BigInteger.ZERO, batches.get(0).l1InclusionBlock());

        spanBatch.getBatches().forEach(element -> {
            BigInteger unusedBlockNum = element.timestamp()
                    .subtract(chainConfig.l2Genesis().timestamp())
                    .divide(BigInteger.TWO);
            BigInteger unusedEpochNum = element.epochNum();
            System.out.println(
                    "block: %d, epoch: %d".formatted(unusedBlockNum.longValue(), unusedEpochNum.longValue()));
            element.transactions().forEach(txStr -> {
                var digest = new Keccak.Digest256();
                byte[] hash = digest.digest(Numeric.hexStringToByteArray(txStr));
                System.out.println(Numeric.toHexString(hash));
            });
        });
    }

    @Test
    void testSub() {
        String s1 = "0xe0bc110ffac850cd0de7fe2b110eb717a68a9799";
        String s2 = "0xe0bc110ffac850cd0de7fe2b110eb717a68a9799797fd99cd89bf1b155a7aac2";
        String s3 = s2.substring(0, 42);
        assertEquals(s1, s3);
    }
}
