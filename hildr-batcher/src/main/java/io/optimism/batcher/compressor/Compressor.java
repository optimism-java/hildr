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

import java.io.Closeable;

/**
 * Tx data bytes compressor interface.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public interface Compressor extends Closeable {

  /**
   * write uncompressed data which will be compressed. Should return CompressorFullException if the
   * compressor is full and no more data should be written.
   *
   * @param p uncompressed data
   * @return length of compressed data
   */
  int write(byte[] p);

  /**
   * read compressed data; should only be called after Close.
   *
   * @param p read buffer bytes to this byte array
   * @return length of read compressed data.
   */
  int read(byte[] p);

  /** reset all written data. */
  void reset();

  /**
   * returns an estimate of the current length of the compressed data; calling Flush will. increase
   * the accuracy at the expense of a poorer compression ratio.
   *
   * @return an estimate of the current length of the compressed data
   */
  int length();

  /**
   * returns CompressorFullException if the compressor is known to be full. Note that calls to Write
   * will fail if an error is returned from this method, but calls to Write can still return
   * CompressorFullErr even if this does not.
   *
   * @return return true if compressed data length reached the limit, otherwise return false
   */
  boolean isFull();
}
