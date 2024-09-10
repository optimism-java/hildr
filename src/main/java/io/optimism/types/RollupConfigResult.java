package io.optimism.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigInteger;
import java.util.Objects;

/**
 * Rollup config.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public class RollupConfigResult {
    /**
     * Genesis anchor point of the rollup.
     */
    @JsonProperty("genesis")
    private Genesis genesis;

    /**
     * Seconds per L2 block
     */
    @JsonProperty("blockTime")
    private BigInteger blockTime;

    /**
     * Sequencer batches may not be more than MaxSequencerDrift seconds after
     * the L1 timestamp of the sequencing window end.
     */
    @JsonProperty("max_sequencer_drift")
    private BigInteger maxSequencerDrift;

    /**
     * Number of epochs (L1 blocks) per sequencing window, including the epoch L1
     * origin block itself
     */
    @JsonProperty("seq_window_size")
    private BigInteger seqWindowSize;

    /**
     * Number of L1 blocks between when a channel can be opened and when it must
     * be closed by.
     */
    @JsonProperty("channel_timeout")
    private BigInteger channelTimeout;

    /**
     * Required to verify L1 signatures.
     */
    @JsonProperty("l1_chain_id")
    private BigInteger l1ChainId;

    /**
     * Required to identify the L2 network and create p2p signatures unique for this chain.
     */
    @JsonProperty("l2_chain_id")
    private BigInteger l2ChainId;

    /**
     * RegolithTime sets the activation time of the Regolith network-upgrade: a
     * pre-mainnet Bedrock change that addresses findings of the Sherlock contest related to
     * deposit attributes. "Regolith" is the loose deposited rock that sits on top of Bedrock.
     */
    @JsonProperty("regolith_time")
    private BigInteger regolithTime;

    /**
     * L1 address that batches are sent to.
     */
    @JsonProperty("batch_inbox_address")
    private String batchInboxAddress;

    /**
     * L1 deposit contract address.
     */
    @JsonProperty("deposit_contract_address")
    private String depositContractAddress;

    /**
     * L1 system config address.
     */
    @JsonProperty("l1_system_config_address")
    private String l1SystemConfigAddress;

    /**
     * Instantiates a new Rollup config result.
     */
    public RollupConfigResult() {}

    /**
     * Gets genesis.
     *
     * @return the genesis
     */
    public Genesis getGenesis() {
        return genesis;
    }

    /**
     * Sets genesis.
     *
     * @param genesis the genesis
     */
    public void setGenesis(Genesis genesis) {
        this.genesis = genesis;
    }

    /**
     * Gets block time.
     *
     * @return the block time
     */
    public BigInteger getBlockTime() {
        return blockTime;
    }

    /**
     * Sets block time.
     *
     * @param blockTime the block time
     */
    public void setBlockTime(BigInteger blockTime) {
        this.blockTime = blockTime;
    }

    /**
     * Gets max sequencer drift.
     *
     * @return the max sequencer drift
     */
    public BigInteger getMaxSequencerDrift() {
        return maxSequencerDrift;
    }

    /**
     * Sets max sequencer drift.
     *
     * @param maxSequencerDrift the max sequencer drift
     */
    public void setMaxSequencerDrift(BigInteger maxSequencerDrift) {
        this.maxSequencerDrift = maxSequencerDrift;
    }

    /**
     * Gets seq window size.
     *
     * @return the seq window size
     */
    public BigInteger getSeqWindowSize() {
        return seqWindowSize;
    }

    /**
     * Sets seq window size.
     *
     * @param seqWindowSize the seq window size
     */
    public void setSeqWindowSize(BigInteger seqWindowSize) {
        this.seqWindowSize = seqWindowSize;
    }

    /**
     * Gets channel timeout.
     *
     * @return the channel timeout
     */
    public BigInteger getChannelTimeout() {
        return channelTimeout;
    }

    /**
     * Sets channel timeout.
     *
     * @param channelTimeout the channel timeout
     */
    public void setChannelTimeout(BigInteger channelTimeout) {
        this.channelTimeout = channelTimeout;
    }

    /**
     * Gets l 1 chain id.
     *
     * @return the l 1 chain id
     */
    public BigInteger getL1ChainId() {
        return l1ChainId;
    }

    /**
     * Sets l 1 chain id.
     *
     * @param l1ChainId the l 1 chain id
     */
    public void setL1ChainId(BigInteger l1ChainId) {
        this.l1ChainId = l1ChainId;
    }

    /**
     * Gets l 2 chain id.
     *
     * @return the l 2 chain id
     */
    public BigInteger getL2ChainId() {
        return l2ChainId;
    }

    /**
     * Sets l 2 chain id.
     *
     * @param l2ChainId the l 2 chain id
     */
    public void setL2ChainId(BigInteger l2ChainId) {
        this.l2ChainId = l2ChainId;
    }

    /**
     * Gets regolith time.
     *
     * @return the regolith time
     */
    public BigInteger getRegolithTime() {
        return regolithTime;
    }

    /**
     * Sets regolith time.
     *
     * @param regolithTime the regolith time
     */
    public void setRegolithTime(BigInteger regolithTime) {
        this.regolithTime = regolithTime;
    }

    /**
     * Gets batch inbox address.
     *
     * @return the batch inbox address
     */
    public String getBatchInboxAddress() {
        return batchInboxAddress;
    }

    /**
     * Sets batch inbox address.
     *
     * @param batchInboxAddress the batch inbox address
     */
    public void setBatchInboxAddress(String batchInboxAddress) {
        this.batchInboxAddress = batchInboxAddress;
    }

    /**
     * Gets deposit contract address.
     *
     * @return the deposit contract address
     */
    public String getDepositContractAddress() {
        return depositContractAddress;
    }

    /**
     * Sets deposit contract address.
     *
     * @param depositContractAddress the deposit contract address
     */
    public void setDepositContractAddress(String depositContractAddress) {
        this.depositContractAddress = depositContractAddress;
    }

    /**
     * Gets l 1 system config address.
     *
     * @return the l 1 system config address
     */
    public String getL1SystemConfigAddress() {
        return l1SystemConfigAddress;
    }

    /**
     * Sets l 1 system config address.
     *
     * @param l1SystemConfigAddress the l 1 system config address
     */
    public void setL1SystemConfigAddress(String l1SystemConfigAddress) {
        this.l1SystemConfigAddress = l1SystemConfigAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RollupConfigResult that)) return false;
        return Objects.equals(genesis, that.genesis)
                && Objects.equals(blockTime, that.blockTime)
                && Objects.equals(maxSequencerDrift, that.maxSequencerDrift)
                && Objects.equals(seqWindowSize, that.seqWindowSize)
                && Objects.equals(channelTimeout, that.channelTimeout)
                && Objects.equals(l1ChainId, that.l1ChainId)
                && Objects.equals(l2ChainId, that.l2ChainId)
                && Objects.equals(regolithTime, that.regolithTime)
                && Objects.equals(batchInboxAddress, that.batchInboxAddress)
                && Objects.equals(depositContractAddress, that.depositContractAddress)
                && Objects.equals(l1SystemConfigAddress, that.l1SystemConfigAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                genesis,
                blockTime,
                maxSequencerDrift,
                seqWindowSize,
                channelTimeout,
                l1ChainId,
                l2ChainId,
                regolithTime,
                batchInboxAddress,
                l1SystemConfigAddress);
    }
}
