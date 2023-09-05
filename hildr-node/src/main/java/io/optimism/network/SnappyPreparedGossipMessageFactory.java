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

package io.optimism.network;

import org.apache.tuweni.bytes.Bytes;
import tech.pegasys.teku.networking.p2p.gossip.PreparedGossipMessage;
import tech.pegasys.teku.networking.p2p.gossip.PreparedGossipMessageFactory;
import tech.pegasys.teku.spec.config.NetworkingSpecConfig;

/**
 * The type SnappyPreparedGossipMessageFactory.
 *
 * @author grapebaba
 * @since 0.1.1
 */
public class SnappyPreparedGossipMessageFactory implements PreparedGossipMessageFactory {

    /** Instantiates a new SnappyPreparedGossipMessageFactory. */
    public SnappyPreparedGossipMessageFactory() {}

    @Override
    public PreparedGossipMessage create(
            final String topic, final Bytes data, final NetworkingSpecConfig networkingConfig) {
        return new SnappyPreparedGossipMessage(topic, data);
    }
}
