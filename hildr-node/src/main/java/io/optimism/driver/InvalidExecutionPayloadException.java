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

package io.optimism.driver;

/**
 * The type InvalidExecutionPayloadException.
 *
 * @author grapebaba
 * @since 0.1.0
 */
public class InvalidExecutionPayloadException extends RuntimeException {

    /** Instantiates a new Invalid execution payload exception. */
    public InvalidExecutionPayloadException() {
        super("invalid execution payload");
    }

    /**
     * Instantiates a new Invalid execution payload exception.
     *
     * @param message the message
     */
    public InvalidExecutionPayloadException(String message) {
        super(message);
    }
}
