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

package io.optimism.telemetry;

import io.micrometer.core.instrument.Counter;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The EventMeter type.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public class EventMeter {

    private Counter total;

    private AtomicLong lastTime;

    /**
     * The EventMeter constructor.
     *
     * @param total The total counter
     * @param lastTime The last time record ref.
     */
    public EventMeter(Counter total, AtomicLong lastTime) {
        this.total = total;
        this.lastTime = lastTime;
    }

    /** Record event count and occur time. */
    public void record() {
        this.total.increment();
        this.lastTime.getAndSet(Instant.now().getEpochSecond());
    }
}
