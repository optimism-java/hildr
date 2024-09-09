package io.optimism.derive.stages;

import java.math.BigInteger;
import java.security.SecureRandom;
import org.web3j.utils.Numeric;

public class RandomUtils {

    public static String randomHex() {
        // Create a SecureRandom object
        SecureRandom rng = new SecureRandom();
        // Generate a random byte array
        byte[] randomBytes = new byte[32];
        rng.nextBytes(randomBytes);
        // Convert the byte array to a hexadecimal string
        return Numeric.toHexString(randomBytes);
    }

    public static BigInteger randomBigInt() {
        // Create a SecureRandom object
        SecureRandom rng = new SecureRandom();
        // Generate a random byte array
        byte[] randomBytes = new byte[32];
        rng.nextBytes(randomBytes);
        // Convert the byte array to a hexadecimal string
        return new BigInteger(randomBytes);
    }
}
