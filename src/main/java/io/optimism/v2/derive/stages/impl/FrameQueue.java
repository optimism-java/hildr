package io.optimism.v2.derive.stages.impl;

import io.optimism.config.Config;
import io.optimism.v2.derive.exception.PipelineProviderException;
import io.optimism.v2.derive.stages.ChannelBankProvider;
import io.optimism.v2.derive.stages.FrameQueueProvider;
import io.optimism.v2.derive.stages.OriginAdvancer;
import io.optimism.v2.derive.stages.OriginProvider;
import io.optimism.v2.derive.stages.ResettableStage;
import io.optimism.v2.derive.types.BlockInfo;
import io.optimism.v2.derive.types.Frame;
import io.optimism.v2.derive.types.SystemConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FrameQueue implements ChannelBankProvider, OriginProvider, OriginAdvancer, ResettableStage {

    private static final int QUEUE_SIZE = 1024;

    private final FrameQueueProvider prev;

    private final Config.ChainConfig rollupConfig;

    private List<Frame> queue;

    /**
     * The frame queue constructor.
     *
     * @param prev The previous stage in the pipeline
     * @param rollupConfig The rollup configuration
     */
    public FrameQueue(FrameQueueProvider prev, Config.ChainConfig rollupConfig) {
        this.prev = prev;
        this.rollupConfig = rollupConfig;
        this.queue = new ArrayList<>(QUEUE_SIZE);
    }

    /**
     * loads more frames into the queue
     */
    public void loadFrames() {
        if (!this.queue.isEmpty()) {
            return;
        }
        var data = this.prev.next();
        List<Frame> frames = Frame.parseFrames(data);
        this.queue.addAll(frames);
        var origin = this.origin();
        if (origin == null) {
            throw new PipelineProviderException("Missing origin");
        }
        this.prune(origin);
    }

    /**
     * prunes frames if Holocene is active
     *
     * @param origin the l1 origin block
     */
    public void prune(BlockInfo origin) {
        if (!rollupConfig.isHolocene(origin.timestamp())) {
            return;
        }
        int i = 0;
        while (i < this.queue.size()) {
            final var prevFrame = this.queue.get(i);
            var nextFrame = this.queue.get(i + 1);
            var extendsChannel = prevFrame.channelId().equals(nextFrame.channelId());
            if (extendsChannel && prevFrame.frameNumber() + 1 != nextFrame.frameNumber()) {
                this.queue.remove(i + 1);
                continue;
            }
            if (extendsChannel && prevFrame.isLastFrame()) {
                this.queue.remove(i + 1);
                continue;
            }
            if (!extendsChannel && !nextFrame.frameNumber().equals(0)) {
                this.queue.remove(i + 1);
                continue;
            }
            if (!extendsChannel
                    && !prevFrame.isLastFrame()
                    && nextFrame.frameNumber().equals(0)) {
                this.queue = this.queue.stream()
                        .filter(f -> f.channelId().equals(prevFrame.channelId()))
                        .collect(Collectors.toList());
                continue;
            }
            i += 1;
        }
    }

    @Override
    public Frame nextFrame() {
        this.loadFrames();
        if (this.queue.isEmpty()) {
            throw new PipelineProviderException("Not enough data");
        }
        return this.queue.removeFirst();
    }

    @Override
    public void advanceOrigin() {
        this.prev.advanceOrigin();
    }

    @Override
    public BlockInfo origin() {
        return this.prev.origin();
    }

    @Override
    public void reset(BlockInfo base, SystemConfig config) {
        this.prev.reset(base, config);
        this.queue = new ArrayList<>(QUEUE_SIZE);
    }
}
