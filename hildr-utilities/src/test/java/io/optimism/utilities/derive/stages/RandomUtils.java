package io.optimism.utilities.derive.stages;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import org.web3j.utils.Numeric;

public class RandomUtils {

    private static byte[] computeHash(byte[] input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(input);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String randomHex() {
        // Create a SecureRandom object
        SecureRandom rng = new SecureRandom();
        // Generate a random byte array
        byte[] randomBytes = new byte[32];
        rng.nextBytes(randomBytes);
        // Compute the hash of the random byte array
        byte[] hash = computeHash(randomBytes);
        // Convert the byte array to a hexadecimal string
        return Numeric.toHexString(hash);
    }
}
