package io.optimism.type;

import java.math.BigInteger;
import java.util.Objects;

/**
 * @author thinkAfCod
 * @since 0.1.1
 */
public class SpecConfig {
    //{
    //  "DEPOSIT_CONTRACT_ADDRESS": "0x00000000219ab540356cBB839Cbe05303d7705Fa",
    //  "DEPOSIT_NETWORK_ID": "1",
    //  "DOMAIN_AGGREGATE_AND_PROOF": "0x06000000",
    //  "INACTIVITY_PENALTY_QUOTIENT": "67108864",
    //  "INACTIVITY_PENALTY_QUOTIENT_ALTAIR": "50331648"
    //}
    public String depositContractAddress;

    public String depositNetworkId;

    public String domainAggregateAndProof;

    public String inactivityPenaltyQuotient;

    public String inactivityPenaltyQuotientAltair;

    public String getDepositContractAddress() {
        return depositContractAddress;
    }

    public void setDepositContractAddress(String depositContractAddress) {
        this.depositContractAddress = depositContractAddress;
    }

    public BigInteger getDepositNetworkId() {
        return new BigInteger(depositNetworkId);
    }

    public void setDepositNetworkId(String depositNetworkId) {
        this.depositNetworkId = depositNetworkId;
    }

    public BigInteger getDomainAggregateAndProof() {
        return new BigInteger(domainAggregateAndProof);
    }

    public void setDomainAggregateAndProof(String domainAggregateAndProof) {
        this.domainAggregateAndProof = domainAggregateAndProof;
    }

    public BigInteger getInactivityPenaltyQuotient() {
        return new BigInteger(inactivityPenaltyQuotient);
    }

    public void setInactivityPenaltyQuotient(String inactivityPenaltyQuotient) {
        this.inactivityPenaltyQuotient = inactivityPenaltyQuotient;
    }

    public BigInteger getInactivityPenaltyQuotientAltair() {
        return new BigInteger(inactivityPenaltyQuotientAltair);
    }

    public String getInactivityPenaltyQuotientAltairRaw() {
        return inactivityPenaltyQuotientAltair;
    }

    public void setInactivityPenaltyQuotientAltair(String inactivityPenaltyQuotientAltair) {
        this.inactivityPenaltyQuotientAltair = inactivityPenaltyQuotientAltair;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpecConfig that = (SpecConfig) o;
        return Objects.equals(depositContractAddress, that.depositContractAddress)
                && Objects.equals(depositNetworkId, that.depositNetworkId)
                && Objects.equals(domainAggregateAndProof, that.domainAggregateAndProof)
                && Objects.equals(inactivityPenaltyQuotient, that.inactivityPenaltyQuotient)
                && Objects.equals(inactivityPenaltyQuotientAltair, that.inactivityPenaltyQuotientAltair);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                depositContractAddress,
                depositNetworkId,
                domainAggregateAndProof,
                inactivityPenaltyQuotient,
                inactivityPenaltyQuotientAltair);
    }
}
