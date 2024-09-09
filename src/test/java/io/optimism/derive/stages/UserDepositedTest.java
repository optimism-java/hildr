package io.optimism.derive.stages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import io.optimism.derive.stages.Attributes.UserDeposited;
import java.math.BigInteger;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.web3j.protocol.core.methods.response.EthLog.LogObject;

/**
 * The type UserDepositedTest.
 *
 * @author grapebaba
 * @since 0.1.0
 */
class UserDepositedTest {

    /** From log. */
    @Test
    void fromLog() {
        LogObject log = new LogObject();
        log.setData("0x00000000000000000000000000000000000000000000000000000000000000200000000000000000"
                + "000000000000000000000000000000000000000000000049000000000000000000000000000000000000"
                + "0000000000000011c37937e080000000000000000000000000000000000000000000000000000011c379"
                + "37e0800000000000000186a0000000000000000000000000000000000000000000000000");
        log.setTopics(List.of(
                "0xb3813568d9991fc951961fcb4c784893574240a28925604d09fc577c55bb7c32",
                "0x000000000000000000000000445c250cb0b46d326f571ec6e278cc92ec984dd3",
                "0x000000000000000000000000445c250cb0b46d326f571ec6e278cc92ec984dd3",
                "0x0000000000000000000000000000000000000000000000000000000000000000"));
        log.setBlockNumber("323232");
        log.setBlockHash("0x2e4f4aff36bb7951be9742ad349fb1db84643c6bbac5014f3d196fd88fe333eb");
        log.setLogIndex("0x2");

        UserDeposited userDeposited = UserDeposited.fromLog(log);
        assertEquals("0x445c250cb0b46d326f571ec6e278cc92ec984dd3", userDeposited.from());
        assertEquals("0x445c250cb0b46d326f571ec6e278cc92ec984dd3", userDeposited.to());
        assertEquals(new BigInteger("5000000000000000"), userDeposited.mint());
        assertEquals(new BigInteger("5000000000000000"), userDeposited.value());
        assertEquals(new BigInteger("100000"), userDeposited.gas());
        assertFalse(userDeposited.isCreation());
        assertEquals(new BigInteger("323232"), userDeposited.l1BlockNum());
        assertEquals("0x2e4f4aff36bb7951be9742ad349fb1db84643c6bbac5014f3d196fd88fe333eb", userDeposited.l1BlockHash());
        assertEquals(new BigInteger("2"), userDeposited.logIndex());
    }
}
