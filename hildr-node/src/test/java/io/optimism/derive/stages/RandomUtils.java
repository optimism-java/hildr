/*
 * Copyright 2023 q315xia@163.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

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
