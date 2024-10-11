package io.optimism.v2.derive.stages;

import io.optimism.v2.derive.types.Frame;

public interface ChannelBankProvider {

    Frame nextFrame();
}
