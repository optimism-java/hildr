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
