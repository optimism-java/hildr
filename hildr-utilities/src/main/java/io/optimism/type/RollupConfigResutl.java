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

package io.optimism.type;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigInteger;
import java.util.Objects;

/**
 * Rollup config.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public class RollupConfigResutl {
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
     *
     */
    @JsonProperty("regolith_time")
    private BigInteger regolithTime;

    /**
     * L1 address that batches are sent to.
     */
    @JsonProperty("batch_inbox_address")
    private String batchInboxAddress;

    /**
     * L1 Deposit Contract Address.
     */
    @JsonProperty("deposit_contract_address")
    private String depositContractAddress;

    public Genesis getGenesis() {
        return genesis;
    }

    public void setGenesis(Genesis genesis) {
        this.genesis = genesis;
    }

    public BigInteger getBlockTime() {
        return blockTime;
    }

    public void setBlockTime(BigInteger blockTime) {
        this.blockTime = blockTime;
    }

    public BigInteger getMaxSequencerDrift() {
        return maxSequencerDrift;
    }

    public void setMaxSequencerDrift(BigInteger maxSequencerDrift) {
        this.maxSequencerDrift = maxSequencerDrift;
    }

    public BigInteger getSeqWindowSize() {
        return seqWindowSize;
    }

    public void setSeqWindowSize(BigInteger seqWindowSize) {
        this.seqWindowSize = seqWindowSize;
    }

    public BigInteger getChannelTimeout() {
        return channelTimeout;
    }

    public void setChannelTimeout(BigInteger channelTimeout) {
        this.channelTimeout = channelTimeout;
    }

    public BigInteger getL1ChainId() {
        return l1ChainId;
    }

    public void setL1ChainId(BigInteger l1ChainId) {
        this.l1ChainId = l1ChainId;
    }

    public BigInteger getL2ChainId() {
        return l2ChainId;
    }

    public void setL2ChainId(BigInteger l2ChainId) {
        this.l2ChainId = l2ChainId;
    }

    public BigInteger getRegolithTime() {
        return regolithTime;
    }

    public void setRegolithTime(BigInteger regolithTime) {
        this.regolithTime = regolithTime;
    }

    public String getBatchInboxAddress() {
        return batchInboxAddress;
    }

    public void setBatchInboxAddress(String batchInboxAddress) {
        this.batchInboxAddress = batchInboxAddress;
    }

    public String getDepositContractAddress() {
        return depositContractAddress;
    }

    public void setDepositContractAddress(String depositContractAddress) {
        this.depositContractAddress = depositContractAddress;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RollupConfigResutl that = (RollupConfigResutl) o;
        return Objects.equals(genesis, that.genesis)
            && Objects.equals(blockTime, that.blockTime)
            && Objects.equals(maxSequencerDrift, that.maxSequencerDrift)
            && Objects.equals(seqWindowSize, that.seqWindowSize)
            && Objects.equals(channelTimeout, that.channelTimeout)
            && Objects.equals(l1ChainId, that.l1ChainId)
            && Objects.equals(l2ChainId, that.l2ChainId)
            && Objects.equals(regolithTime, that.regolithTime)
            && Objects.equals(batchInboxAddress, that.batchInboxAddress)
            && Objects.equals(depositContractAddress, that.depositContractAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(genesis, blockTime, maxSequencerDrift, seqWindowSize, channelTimeout,
            l1ChainId, l2ChainId, regolithTime, batchInboxAddress, depositContractAddress);
    }
}