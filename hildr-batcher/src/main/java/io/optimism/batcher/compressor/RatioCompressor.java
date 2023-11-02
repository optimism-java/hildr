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

import io.optimism.batcher.compressor.exception.CompressorException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.zip.Deflater;

/**
 * RatioCompressor class.
 *
 * @author thinkAfCod
 * @since 0.1.1
 */
public class RatioCompressor implements Compressor {

  private final CompressorConfig config;

  private final Deflater deflater;

  private final int inputThreshold;

  private final byte[] compressed;

  private volatile ByteArrayOutputStream bos;

  private boolean closed;

  private int pos;

  private int inputLength;

  RatioCompressor(final CompressorConfig config) {
    this.config = config;
    this.deflater = new Deflater(Deflater.BEST_COMPRESSION);
    this.inputThreshold = inputThreshold();
    this.bos = new ByteArrayOutputStream(this.inputThreshold);
    this.compressed = new byte[2048];
    this.pos = 0;
    this.inputLength = 0;
  }

  @Override
  public int write(byte[] p) {
    if (this.isFull()) {
      throw new CompressorException("the target amount of input data has been reached limit");
    }
    if (this.isClose()) {
      throw new CompressorException("the compressor has been closed");
    }
    this.inputLength += p.length;
    this.deflater.setInput(p);
    int len;
//    int compressedLength = 0;
    do {
      len = this.deflater.deflate(compressed, 0, compressed.length, Deflater.SYNC_FLUSH);
      if (len > 0) {
//        compressedLength += len;
        this.bos.write(compressed, 0, len);
      }
    } while (len > 0);
    this.deflater.reset();
    return p.length;
  }

  private boolean isClose() {
    return this.closed;
  }

  @Override
  public int read(byte[] p) {
    byte[] data = this.bos.toByteArray();
    int len = this.bos.size();
    if (pos > len) {
      return -1;
    }
    int readLen = p.length;
    int avail = len - pos;
    if (p.length > avail) {
      readLen = avail;
    }
    System.arraycopy(data, pos, p, 0, readLen);
    pos += readLen;
    return readLen;
  }

  @Override
  public void reset() {
    if (this.bos != null) {
      this.bos.reset();
    }
    this.deflater.reset();
    this.pos = 0;
    this.inputLength = 0;
  }

  @Override
  public int length() {
    return this.bos.size() - this.pos;
  }

  @Override
  public boolean isFull() {
    return this.inputLength >= this.inputThreshold;
  }

  @Override
  public void close() throws IOException {
    closed = true;
    this.deflater.finish();
    this.deflater.end();
  }

  private int inputThreshold() {
    return BigDecimal.valueOf(config.targetNumFrames())
        .multiply(BigDecimal.valueOf(config.targetFrameSize()))
        .divide(new BigDecimal(config.approxComprRatio()), 2, RoundingMode.HALF_UP)
        .intValue();
  }
}
