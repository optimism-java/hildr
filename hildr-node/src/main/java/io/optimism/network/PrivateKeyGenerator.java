package io.optimism.network;

import io.libp2p.core.crypto.KeyKt;
import io.libp2p.core.crypto.KeyType;
import io.libp2p.core.crypto.PrivKey;

/**
 * The type PrivateKeyGenerator.
 *
 * @author grapebaba
 * @since 0.1.1
 */
public class PrivateKeyGenerator {
    /** Instantiates a new Private key generator. */
    public PrivateKeyGenerator() {}

    /**
     * Generate priv key.
     *
     * @return the priv key
     */
    public static PrivKey generate() {
        return KeyKt.generateKeyPair(KeyType.SECP256K1).component1();
    }
}
