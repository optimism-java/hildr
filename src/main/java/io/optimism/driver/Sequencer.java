package io.optimism.driver;

import io.optimism.config.Config;
import io.optimism.exceptions.CriticalException;
import io.optimism.exceptions.ResetException;
import io.optimism.exceptions.SequencerException;
import io.optimism.exceptions.TemporaryException;
import io.optimism.types.ExecutionPayloadEnvelop;
import io.optimism.types.L2BlockRef;
import io.optimism.types.enums.BlockInsertion;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The sequencer class.
 * @author thinkAfCod
 * @since 0.4.1
 */
public class Sequencer implements ISequencer {

    private final Logger LOGGER = LoggerFactory.getLogger(Sequencer.class);

    private static final long SECOND_NANO = Duration.of(1, ChronoUnit.SECONDS).toNanos();

    private static final long SEALING_DURATION =
            Duration.of(50, ChronoUnit.SECONDS).getSeconds();

    private final Config.ChainConfig chainConfig;

    private EngineDriver<?> engineDriver;

    private AtomicLong nextActionTime;

    private final long blockTime;

    private final long maxSequencerDrift;

    /**
     * Instantiates a new Sequencer.
     *
     * @param engineDriver the engine driver
     * @param chainConfig the chain config
     */
    public Sequencer(final EngineDriver<?> engineDriver, final Config.ChainConfig chainConfig) {
        this.chainConfig = chainConfig;
        this.engineDriver = engineDriver;
        this.nextActionTime = new AtomicLong();
        this.blockTime = chainConfig.blockTime().longValue();
        this.maxSequencerDrift = chainConfig.maxSeqDrift().longValue();
        // todo gossip broadcast
    }

    @Override
    public ExecutionPayloadEnvelop runNextSequencerAction() {
        var now = this.nowNano();
        if (nextActionTime.get() < now) {
            return null;
        }
        // complete building block
        // check if a payload is building
        var buildings = this.engineDriver.buildingPayload();
        var buildingOnto = buildings.component1();
        var buildingId = buildings.component2();
        var safe = buildings.component3();
        // todo is gossiping check
        //        var isGossiping;
        ExecutionPayloadEnvelop envelop = null;
        if (StringUtils.isNotEmpty(buildingId)) {
            if (safe) {
                this.nextActionTime.set(now + this.blockTime * SECOND_NANO);
                return null;
            }
            envelop = this.completeBuildingBlock();
        } else {
            // start building block
            this.startBuildingBlock();
        }
        // after all update next action time
        var delay = this.planNextSequencerAction();
        this.nextActionTime.set(this.nowNano() + delay);
        return envelop;
    }

    @Override
    public long planNextSequencerAction() {
        var buildings = this.engineDriver.buildingPayload();
        var buildingOnto = buildings.component1();
        var buildingId = buildings.component2();
        var safe = buildings.component3();
        if (safe) {
            return this.blockTime;
        }
        var unsafeHead = this.engineDriver.getUnsafeHead();
        var now = this.nowNano();

        var delay = this.nextActionTime.get() - this.nowNano();
        if (delay > 0 && buildingOnto.hash().equalsIgnoreCase(unsafeHead.hash())) {
            return delay;
        }
        var payloadTime = unsafeHead.timestamp().longValue() + this.blockTime;
        var remainingTime = payloadTime - now;

        if (StringUtils.isNotEmpty(buildingId) && buildingOnto.hash().equalsIgnoreCase(unsafeHead.hash())) {
            return remainingTime < SEALING_DURATION ? 0 : remainingTime - SEALING_DURATION;
        }
        return remainingTime > this.blockTime ? remainingTime - this.blockTime : 0;
    }

    @Override
    public void startBuildingBlock() {
        try {
            var buildings = this.engineDriver.buildingPayload();
            LOGGER.info(
                    "sequencer started building new block",
                    "payload_id",
                    buildings.component2(),
                    "l2_parent_block",
                    buildings.component1());
        } catch (Exception e) {
            this.handleStartBuildingException(e);
        }
    }

    private void handleStartBuildingException(Exception e) {
        if (e instanceof CriticalException) {
            throw (CriticalException) e;
        }
        if (e instanceof ResetException) {
            LOGGER.error("sequencer failed to seal new block, requiring derivation reset", e);
            this.nextActionTime.set(this.nowNano() + this.blockTime * SECOND_NANO);
            this.cancelBuildingBlock();
            throw (ResetException) e;
        } else if (e instanceof TemporaryException) {
            LOGGER.error("sequencer temporarily failed to start building new block", e);
            this.nextActionTime.set(this.nowNano() + SECOND_NANO);
        } else {
            LOGGER.error("sequencer failed to start building new block with unclassified error", e);
            this.nextActionTime.set(this.nowNano() + SECOND_NANO);
            this.cancelBuildingBlock();
        }
    }

    @Override
    public ExecutionPayloadEnvelop completeBuildingBlock() {
        try {
            var res = this.engineDriver.confirmBuildingPayload();
            var envelope = res.component1();
            var errType = res.component2();
            if (errType != BlockInsertion.SUCCESS) {
                throw new SequencerException("failed to complete building block: error: " + errType.name());
            }
            return envelope;
        } catch (Exception e) {
            this.handleCompleteBuildingException(e);
            return null;
        }
    }

    private void handleCompleteBuildingException(Exception e) {
        if (e instanceof CriticalException) {
            throw (CriticalException) e;
        }
        if (e instanceof ResetException) {
            LOGGER.error("sequencer failed to seal new block, requiring derivation reset", e);
            this.nextActionTime.set(this.nowNano() + this.blockTime * SECOND_NANO);
            this.cancelBuildingBlock();
            throw (ResetException) e;
        } else if (e instanceof TemporaryException) {
            LOGGER.error("sequencer failed temporarily to seal new block", e);
            this.nextActionTime.set(this.nowNano() + SECOND_NANO);
        } else {
            LOGGER.error("sequencer failed to seal block with unclassified error", e);
            this.nextActionTime.set(this.nowNano() + SECOND_NANO);
            this.cancelBuildingBlock();
        }
    }

    @Override
    public L2BlockRef buildingOnto() {
        return this.engineDriver.buildingPayload().component1();
    }

    @Override
    public void cancelBuildingBlock() {
        this.engineDriver.cancelPayload(true);
    }

    private long nowNano() {
        return System.nanoTime();
    }
}
