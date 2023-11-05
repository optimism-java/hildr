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

package io.optimism.batcher.loader;

/**
 * BlockLoaderException class. Throws this when occurs error while executing BlockLoader.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public class BlockLoaderException extends RuntimeException {

    /**
     * Instantiates a new BlockLoaderException.
     *
     * @param message the message
     */
    public BlockLoaderException(String message) {
        super(message);
    }

    /**
     * Instantiates a new BlockLoaderException.
     *
     * @param message the message
     * @param cause the cause
     */
    public BlockLoaderException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new BlockLoaderException.
     *
     * @param cause the cause
     */
    public BlockLoaderException(Throwable cause) {
        super(cause);
    }
}
