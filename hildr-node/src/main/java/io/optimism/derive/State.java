/*
 * Copyright 2023 281165273grape@gmail.com
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

package io.optimism.derive;


import io.optimism.common.BlockInfo;
import io.optimism.common.Epoch;
import io.optimism.config.Config;
import io.optimism.l1.L1Info;
import java.math.BigInteger;
import java.util.TreeMap;

/**
 * The type State.
 *
 * @author grapebaba
 * @since 0.1.0
 */
public class State {

  private final TreeMap<String, L1Info> l1Info;

  private final TreeMap<BigInteger, String> l1Hashes;

  private BlockInfo safeHead;

  private Epoch safeEpoch;

  private BigInteger currentEpochNum;

  private final Config config;

  /**
   * Instantiates a new State.
   *
   * @param l1Info the L1 info
   * @param l1Hashes the L1 hashes
   * @param safeHead the safe head
   * @param safeEpoch the safe epoch
   * @param currentEpochNum the current epoch num
   * @param config the config
   */
  public State(
      TreeMap<String, L1Info> l1Info,
      TreeMap<BigInteger, String> l1Hashes,
      BlockInfo safeHead,
      Epoch safeEpoch,
      BigInteger currentEpochNum,
      Config config) {
    this.l1Info = l1Info;
    this.l1Hashes = l1Hashes;
    this.safeHead = safeHead;
    this.safeEpoch = safeEpoch;
    this.currentEpochNum = currentEpochNum;
    this.config = config;
  }

  /**
   * Create state.
   *
   * @param finalizedHead the finalized head
   * @param finalizedEpoch the finalized epoch
   * @param config the config
   * @return the state
   */
  public static State create(BlockInfo finalizedHead, Epoch finalizedEpoch, Config config) {
    return new State(
        new TreeMap<>(), new TreeMap<>(), finalizedHead, finalizedEpoch, BigInteger.ZERO, config);
  }

  /**
   * L 1 info l 1 info.
   *
   * @param hash the hash
   * @return the l 1 info
   */
  public L1Info l1Info(String hash) {
    return l1Info.get(hash);
  }

  /**
   * L 1 info l 1 info.
   *
   * @param number the number
   * @return the l 1 info
   */
  public L1Info l1Info(BigInteger number) {
    return l1Info.get(l1Hashes.get(number));
  }

  /**
   * Epoch epoch.
   *
   * @param hash the hash
   * @return the epoch
   */
  public Epoch epoch(String hash) {
    L1Info l1Info = l1Info(hash);
    return new Epoch(
        l1Info.blockInfo().number(), l1Info.blockInfo().hash(), l1Info.blockInfo().timestamp());
  }

  /**
   * Epoch epoch.
   *
   * @param number the number
   * @return the epoch
   */
  public Epoch epoch(BigInteger number) {
    L1Info l1Info = l1Info(number);
    return new Epoch(
        l1Info.blockInfo().number(), l1Info.blockInfo().hash(), l1Info.blockInfo().timestamp());
  }

  /**
   * Is full boolean.
   *
   * @return the boolean
   */
  public boolean isFull() {
    return this.currentEpochNum.compareTo(this.safeEpoch.number().add(BigInteger.valueOf(1000L)))
        > 0;
  }

  /**
   * Update l 1 info.
   *
   * @param l1Info the l 1 info
   */
  public void updateL1Info(L1Info l1Info) {
    this.currentEpochNum = l1Info.blockInfo().number();
    this.l1Hashes.put(l1Info.blockInfo().number(), l1Info.blockInfo().hash());
    this.l1Info.put(l1Info.blockInfo().hash(), l1Info);

    this.prune();
  }

  /**
   * Purge.
   *
   * @param safeHead the safe head
   * @param safeEpoch the safe epoch
   */
  public void purge(BlockInfo safeHead, Epoch safeEpoch) {
    this.safeHead = safeHead;
    this.safeEpoch = safeEpoch;
    this.l1Info.clear();
    this.l1Hashes.clear();
    this.currentEpochNum = BigInteger.ZERO;
  }

  /**
   * Update safe head.
   *
   * @param safeHead the safe head
   * @param safeEpoch the safe epoch
   */
  public void updateSafeHead(BlockInfo safeHead, Epoch safeEpoch) {
    this.safeHead = safeHead;
    this.safeEpoch = safeEpoch;
  }

  /**
   * Gets safe head.
   *
   * @return the safe head
   */
  public BlockInfo getSafeHead() {
    return safeHead;
  }

  /**
   * Sets safe head.
   *
   * @param safeHead the safe head
   */
  public void setSafeHead(BlockInfo safeHead) {
    this.safeHead = safeHead;
  }

  /**
   * Gets safe epoch.
   *
   * @return the safe epoch
   */
  public Epoch getSafeEpoch() {
    return safeEpoch;
  }

  /**
   * Sets safe epoch.
   *
   * @param safeEpoch the safe epoch
   */
  public void setSafeEpoch(Epoch safeEpoch) {
    this.safeEpoch = safeEpoch;
  }

  /**
   * Gets current epoch num.
   *
   * @return the current epoch num
   */
  public BigInteger getCurrentEpochNum() {
    return currentEpochNum;
  }

  /**
   * Sets current epoch num.
   *
   * @param currentEpochNum the current epoch num
   */
  public void setCurrentEpochNum(BigInteger currentEpochNum) {
    this.currentEpochNum = currentEpochNum;
  }

  private void prune() {
    BigInteger pruneUntil = this.safeEpoch.number().subtract(config.chainConfig().seqWindowSize());
    while (this.l1Hashes.firstKey().compareTo(pruneUntil) < 0) {
      this.l1Info.remove(this.l1Hashes.firstEntry().getValue());
      this.l1Hashes.pollFirstEntry();
    }
  }
}
