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

package io.optimism.batcher.compressor;

import java.io.IOException;
import java.nio.CharBuffer;
import org.jetbrains.annotations.NotNull;

/**
 * ShadowCompressor class.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public class ShadowCompressor implements Compressor {

  /** Constructor of ShadowCompressor. */
  public ShadowCompressor() {}

  @Override
  public int write(byte[] p) {
    return 0;
  }

  @Override
  public int read(byte[] p) {
    return 0;
  }

  @Override
  public int read(@NotNull CharBuffer cb) throws IOException {
    return 0;
  }

  @Override
  public void reset() {}

  @Override
  public int length() {
    return 0;
  }

  @Override
  public void fullErr() {}

  @Override
  public void close() throws IOException {}

  @Override
  public void flush() throws IOException {}
}
