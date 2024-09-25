package io.optimism.spec.derive.stages;

import io.optimism.spec.derive.types.Frame;

public interface ChannelBankProvider {

  Frame nextFrame();

}
