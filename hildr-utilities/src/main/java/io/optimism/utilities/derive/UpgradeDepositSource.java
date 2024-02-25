package io.optimism.utilities.derive;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import org.web3j.crypto.Hash;
import org.web3j.utils.Numeric;

/**
 * The UpgradeDepositSource class.
 * @author thinkAfCod
 * @since 0.2.7
 */
public class UpgradeDepositSource {

    private static final BigInteger UPGRADE_DEPOSIT_SOURCE_DOMAIN = BigInteger.TWO;

    private final String intent;

    /**
     * The UpgradeDepositSource constructor.
     * @param intent The intent identifies the upgrade-tx uniquely, in a human-readable way.
     */
    public UpgradeDepositSource(String intent) {
        this.intent = intent;
    }

    public String sourceHash() {
        byte[] domainInput = new byte[32 * 2];
        byte[] paddedDomain = Numeric.toBytesPadded(UPGRADE_DEPOSIT_SOURCE_DOMAIN, 8);
        System.arraycopy(paddedDomain, 0, domainInput, 24, 8);
        byte[] intentHash = Hash.sha3(this.intent.getBytes(StandardCharsets.UTF_8));
        System.arraycopy(intentHash, 0, domainInput, 32, 32);
        return Numeric.toHexString(Hash.sha3(domainInput));
    }
}
