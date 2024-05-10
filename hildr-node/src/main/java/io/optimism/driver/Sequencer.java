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

package io.optimism.driver;

import io.optimism.network.ExecutionPayloadEnvelop;
import io.optimism.type.L2BlockRef;
import java.time.Duration;

/**
 * The sequencer class.
 * @author thinkAfCod
 * @since 0.4.1
 */
public class Sequencer implements ISequencer {

    /**
     * Instantiates a new Sequencer.
     */
    public Sequencer() {}

    @Override
    public void startBuildingBlock() {}

    @Override
    public ExecutionPayloadEnvelop completeBuildingBlock() {
        return null;
    }

    @Override
    public Duration planNextSequencerAction() {
        return null;
    }

    @Override
    public ExecutionPayloadEnvelop runNextSequencerAction() {
        return null;
    }

    @Override
    public L2BlockRef buildingOnto() {
        return null;
    }

    @Override
    public void cancelBuildingBlock() {}
}
